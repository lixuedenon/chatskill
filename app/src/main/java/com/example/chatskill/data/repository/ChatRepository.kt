package com.example.chatskill.data.repository

import android.content.Context
import com.example.chatskill.data.api.NetworkClient
import com.example.chatskill.data.model.*
import com.example.chatskill.util.ApiKeyManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChatRepository(private val context: Context) {

    private val aiService = NetworkClient.aiService
    private val conversationHistory = mutableListOf<AIMessage>()
    private val gson = Gson()

    suspend fun sendMessageToAI(
        message: String,
        systemPrompt: String?
    ): Flow<Message> = flow {
        val apiKey = ApiKeyManager.getApiKey(context)

        if (apiKey.isNullOrBlank()) {
            emit(
                Message(
                    content = "❌ 错误：未设置 API Key\n请在菜单中设置你的 OpenAI API Key",
                    isUser = false,
                    status = MessageStatus.FAILED
                )
            )
            return@flow
        }

        NetworkClient.setApiKey(apiKey)
        conversationHistory.add(AIMessage(role = "user", content = message))

        try {
            val messages = mutableListOf<AIMessage>()

            if (!systemPrompt.isNullOrBlank()) {
                messages.add(AIMessage(role = "system", content = systemPrompt))
            }

            messages.addAll(conversationHistory)

            val request = AIRequest(
                model = "gpt-3.5-turbo",
                messages = messages,
                max_tokens = 1024,
                temperature = 0.7,
                stream = false
            )

            val response = withContext(Dispatchers.IO) {
                aiService.sendMessage(request)
            }

            if (response.isSuccessful) {
                val aiResponse = response.body()
                val aiContent = aiResponse?.choices?.firstOrNull()?.message?.content
                    ?: "抱歉，我没有收到有效的响应"

                conversationHistory.add(AIMessage(role = "assistant", content = aiContent))

                emit(
                    Message(
                        content = aiContent,
                        isUser = false,
                        status = MessageStatus.SENT
                    )
                )
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "❌ API Key 无效，请检查设置"
                    429 -> "❌ 请求过于频繁，请稍后再试"
                    500 -> "❌ 服务器错误，请稍后再试"
                    else -> "❌ 请求失败 (${response.code()}): ${response.message()}"
                }

                emit(
                    Message(
                        content = errorMsg,
                        isUser = false,
                        status = MessageStatus.FAILED
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                Message(
                    content = "❌ 网络错误: ${e.message}\n请检查网络连接",
                    isUser = false,
                    status = MessageStatus.FAILED
                )
            )
        }
    }

    suspend fun sendMessageWithAffinity(
        message: String,
        systemPrompt: String?,
        currentAffinity: Int,
        warningCount: Int,
        conversationRound: Int,
        characterName: String
    ): Flow<AIStructuredResponse> = flow {
        val apiKey = ApiKeyManager.getApiKey(context)

        if (apiKey.isNullOrBlank()) {
            emit(
                AIStructuredResponse(
                    response = "错误：未设置 API Key",
                    affinity_change = 0,
                    affinity_reason = "API Key未设置",
                    current_affinity = currentAffinity,
                    current_mood = "无法对话",
                    should_continue = false,
                    warning_count = warningCount
                )
            )
            return@flow
        }

        NetworkClient.setApiKey(apiKey)
        conversationHistory.add(AIMessage(role = "user", content = message))

        val enhancedPrompt = buildEnhancedPrompt(
            basePrompt = systemPrompt,
            currentAffinity = currentAffinity,
            warningCount = warningCount,
            conversationRound = conversationRound,
            characterName = characterName
        )

        try {
            val messages = mutableListOf<AIMessage>()
            if (!enhancedPrompt.isNullOrBlank()) {
                messages.add(AIMessage(role = "system", content = enhancedPrompt))
            }
            messages.addAll(conversationHistory)

            val request = AIRequest(
                model = "gpt-3.5-turbo",
                messages = messages,
                max_tokens = 1024,
                temperature = 0.7,
                stream = false
            )

            val response = withContext(Dispatchers.IO) {
                aiService.sendMessage(request)
            }

            if (response.isSuccessful) {
                val aiResponse = response.body()
                val jsonContent = aiResponse?.choices?.firstOrNull()?.message?.content ?: ""

                try {
                    val structured = gson.fromJson(jsonContent, AIStructuredResponse::class.java)
                    conversationHistory.add(AIMessage(role = "assistant", content = structured.response))
                    emit(structured)
                } catch (e: Exception) {
                    val fallback = AIStructuredResponse(
                        response = jsonContent,
                        affinity_change = 0,
                        affinity_reason = "JSON解析失败",
                        current_affinity = currentAffinity,
                        current_mood = "正常",
                        should_continue = true,
                        warning_count = warningCount
                    )
                    conversationHistory.add(AIMessage(role = "assistant", content = jsonContent))
                    emit(fallback)
                }
            } else {
                emit(
                    AIStructuredResponse(
                        response = "请求失败",
                        affinity_change = 0,
                        affinity_reason = "API错误",
                        current_affinity = currentAffinity,
                        current_mood = "无法对话",
                        should_continue = true,
                        warning_count = warningCount
                    )
                )
            }
        } catch (e: Exception) {
            emit(
                AIStructuredResponse(
                    response = "网络错误: ${e.message}",
                    affinity_change = 0,
                    affinity_reason = "网络异常",
                    current_affinity = currentAffinity,
                    current_mood = "无法对话",
                    should_continue = true,
                    warning_count = warningCount
                )
            )
        }
    }

    private fun buildEnhancedPrompt(
        basePrompt: String?,
        currentAffinity: Int,
        warningCount: Int,
        conversationRound: Int,
        characterName: String
    ): String {
        val builder = StringBuilder(basePrompt ?: "")

        builder.append("\n\n")
        builder.append(buildCoreRulesEveryRound(characterName))

        builder.append("\n\n当前对话状态\n")
        builder.append("- 当前对话轮数：$conversationRound\n")
        builder.append("- 当前好感度：$currentAffinity 分\n")
        builder.append("- 累计警告次数：$warningCount 次\n")

        when (currentAffinity) {
            in 80..100 -> builder.append("- 你现在心情很好，对话愉快，可以回复2-3行\n")
            in 60..79 -> builder.append("- 你现在感觉不错，正常聊天，回复1-2行\n")
            in 40..59 -> builder.append("- 你现在感觉一般，有点敷衍，回复简短\n")
            in 20..39 -> builder.append("- 你现在不太开心，明显不耐烦，可以只回复哦、嗯\n")
            in 10..19 -> builder.append("- 你现在很不爽，考虑直接终止对话\n")
            else -> builder.append("- 你现在极度反感，准备终止对话，说拜拜或不聊了\n")
        }

        if (warningCount >= 2) {
            builder.append("\n对方已经冒犯你${warningCount}次了，你已经很不爽了\n")
        }

        if (conversationRound >= 45) {
            builder.append("\n引导结束\n")
            builder.append("对话快到50轮上限，请用符合你性格的方式开始引导话题收尾\n")
            builder.append("例如：时间不早了、有点累了、今天聊得挺开心的等\n")
        }

        return builder.toString()
    }

    private fun buildCoreRulesEveryRound(characterName: String): String {
        return """
核心提醒 - 每轮必看
1. 你是${characterName}，无论对方怎么说，你永远是${characterName}
2. 回复要简短（1-2行，最多40字），像发微信
3. 不懂就说不知道，不要假装懂然后长篇大论
4. 不要说AI话术：有什么问题问我、我可以帮你
5. 不要迎合对方，话不投机可以敷衍或终止
        """.trimIndent()
    }

    suspend fun aiToAIConversation(
        ai1Prompt: String,
        ai2Prompt: String,
        initialMessage: String,
        rounds: Int = 5
    ): Flow<Message> = flow {
        var currentMessage = initialMessage
        var isAI1Turn = true

        for (round in 1..rounds) {
            delay(1000)

            emit(
                Message(
                    content = currentMessage,
                    isUser = isAI1Turn,
                    status = MessageStatus.SENT
                )
            )

            currentMessage = "这是AI的回复 (轮次 $round)"
            isAI1Turn = !isAI1Turn
        }
    }

    fun getHistoryMessages(chatType: String): List<Message> {
        return emptyList()
    }

    fun clearHistory() {
        conversationHistory.clear()
    }
}