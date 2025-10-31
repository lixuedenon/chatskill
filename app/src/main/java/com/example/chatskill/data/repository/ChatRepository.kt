// 路径: app/src/main/java/com/example/chatskill/data/repository/ChatRepository.kt
package com.example.chatskill.data.repository

import android.content.Context
import com.example.chatskill.data.api.NetworkClient
import com.example.chatskill.data.model.*
import com.example.chatskill.util.ApiKeyManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChatRepository(private val context: Context) {

    private val aiService = NetworkClient.aiService
    private val conversationHistory = mutableListOf<AIMessage>()

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