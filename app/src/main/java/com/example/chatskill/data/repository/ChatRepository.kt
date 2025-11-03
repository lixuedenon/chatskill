// 路径: app/src/main/java/com/example/chatskill/data/repository/ChatRepository.kt
// 类型: class

package com.example.chatskill.data.repository

import android.content.Context
import com.example.chatskill.data.api.NetworkClient
import com.example.chatskill.data.model.*
import com.example.chatskill.util.ApiKeyManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChatRepository(private val context: Context) {

    private val aiService = NetworkClient.aiService
    private val conversationHistory = mutableListOf<AIMessage>()
    private val gson = Gson()

    suspend fun generateCharacterProfile(
        gender: Gender,
        ageRange: AgeRange,
        personality: PersonalityType,
        education: EducationLevel
    ): Flow<CharacterProfile> = flow {
        val apiKey = ApiKeyManager.getApiKey(context)

        if (apiKey.isNullOrBlank()) {
            throw Exception("未设置 API Key")
        }

        NetworkClient.setApiKey(apiKey)

        val genderText = if (gender == Gender.FEMALE) "女生" else "男生"
        val ageText = "${ageRange.getMiddleAge()}岁（${ageRange.displayName}）"
        val personalityText = personality.description
        val educationText = education.displayName

        val prompt = """
请为一个中国${genderText}生成完整的角色画像。

角色基本信息：
- 性别：${genderText}
- 年龄：${ageText}
- 性格：${personalityText}
- 教育程度：${educationText}

**重要：必须严格按照以下JSON格式返回，不要有任何其他文字！**

{
  "name": "2-3个字的中文名字",
  "occupation": "职业或专业",
  "expert_hobbies": [
    {"name": "爱好1", "level": 4},
    {"name": "爱好2", "level": 3}
  ],
  "casual_hobbies": [
    {"name": "爱好3", "level": 2},
    {"name": "爱好4", "level": 1},
    {"name": "爱好5", "level": 1}
  ]
}

生成规则：
1. name：好听自然的中文名字（如：思涵、浩然、婉儿）
2. occupation：根据年龄和教育程度合理生成
   - 学生要注明专业（如："大学生-计算机专业"）
   - 在职要写职业（如："咖啡店店员"、"程序员"）
3. expert_hobbies：1-3个擅长的爱好，level在3-5之间
4. casual_hobbies：2-4个一般了解的爱好，level在1-2之间
5. 爱好要常见（烹饪、运动、游戏、音乐、阅读等），符合性格

**再次强调：只返回JSON，不要有任何解释或其他文字！**
        """.trimIndent()

        try {
            val messages = listOf(
                AIMessage(
                    role = "system",
                    content = "你是一个JSON生成助手。你只返回有效的JSON格式数据，不返回任何其他文字、解释或markdown标记。"
                ),
                AIMessage(role = "user", content = prompt)
            )

            val request = AIRequest(
                model = "gpt-3.5-turbo",
                messages = messages,
                max_tokens = 500,
                temperature = 0.8,
                stream = false
            )

            val response = withContext(Dispatchers.IO) {
                aiService.sendMessage(request)
            }

            if (response.isSuccessful) {
                val aiResponse = response.body()
                var jsonContent = aiResponse?.choices?.firstOrNull()?.message?.content?.trim() ?: ""

                jsonContent = jsonContent
                    .replace("```json", "")
                    .replace("```", "")
                    .trim()

                if (!jsonContent.startsWith("{")) {
                    val jsonStart = jsonContent.indexOf("{")
                    val jsonEnd = jsonContent.lastIndexOf("}")
                    if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        jsonContent = jsonContent.substring(jsonStart, jsonEnd + 1)
                    }
                }

                try {
                    val profile = gson.fromJson(jsonContent, CharacterProfile::class.java)

                    if (profile.name.isBlank() || profile.occupation.isBlank() ||
                        profile.expert_hobbies.isEmpty() || profile.casual_hobbies.isEmpty()) {
                        throw Exception("生成的角色信息不完整")
                    }

                    if (profile.name.length !in 2..3) {
                        throw Exception("生成的名字格式不正确")
                    }

                    emit(profile)
                } catch (e: Exception) {
                    throw Exception("AI返回的数据格式错误，请重试")
                }
            } else {
                val errorMsg = when (response.code()) {
                    401 -> "API Key 无效"
                    429 -> "请求过于频繁"
                    else -> "API请求失败"
                }
                throw Exception(errorMsg)
            }
        } catch (e: Exception) {
            throw Exception(e.message ?: "生成角色失败")
        }
    }

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
        characterName: String,
        reviewMode: ReviewMode? = null,
        previousRecord: ConversationRecord? = null
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
                    warning_count = warningCount,
                    violation_detected = false,
                    violation_type = "none"
                )
            )
            return@flow
        }

        NetworkClient.setApiKey(apiKey)
        conversationHistory.add(AIMessage(role = "user", content = message))

        val enhancedPrompt = if (reviewMode != null && previousRecord != null) {
            buildReviewModePrompt(systemPrompt, reviewMode, previousRecord, currentAffinity, warningCount, conversationRound, characterName)
        } else {
            buildEnhancedPrompt(systemPrompt, currentAffinity, warningCount, conversationRound, characterName)
        }

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
                        warning_count = warningCount,
                        violation_detected = false,
                        violation_type = "none"
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
                        warning_count = warningCount,
                        violation_detected = false,
                        violation_type = "none"
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
                    warning_count = warningCount,
                    violation_detected = false,
                    violation_type = "none"
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

        builder.append("\n\n## 当前对话状态\n")
        builder.append("- 当前对话轮数：$conversationRound\n")
        builder.append("- 当前好感度：$currentAffinity 分\n")
        builder.append("- 累计违规次数：$warningCount 次\n")

        when (currentAffinity) {
            in 80..100 -> builder.append("- 你现在心情很好，对话愉快，可以回复2-3行\n")
            in 60..79 -> builder.append("- 你现在感觉不错，正常聊天，回复1-2行\n")
            in 40..59 -> builder.append("- 你现在感觉一般，有点敷衍，回复简短\n")
            in 20..39 -> builder.append("- 你现在不太开心，明显不耐烦，可以只回复哦、嗯\n")
            in 10..19 -> builder.append("- 你现在很不爽，考虑直接终止对话\n")
            else -> builder.append("- 你现在极度反感，准备终止对话，说拜拜或不聊了\n")
        }

        if (warningCount >= 2) {
            builder.append("\n⚠️ 对方已经冒犯/违规了${warningCount}次，你已经很不爽了！\n")
            builder.append("你要明确表达不满，考虑终止对话。\n")
        }

        if (conversationRound >= 45) {
            builder.append("\n## 引导结束\n")
            builder.append("对话快到50轮上限，请用符合你性格的方式开始引导话题收尾\n")
            builder.append("例如：时间不早了、有点累了、今天聊得挺开心的等\n")
        }

        return builder.toString()
    }

    private fun buildReviewModePrompt(
        basePrompt: String?,
        reviewMode: ReviewMode,
        previousRecord: ConversationRecord,
        currentAffinity: Int,
        warningCount: Int,
        conversationRound: Int,
        characterName: String
    ): String {
        val builder = StringBuilder(basePrompt ?: "")

        builder.append("\n\n## 📋 复盘模式说明\n")
        builder.append("这是一次复盘练习对话。上次你们聊过以下内容：\n\n")

        val historyContext = previousRecord.messages.take(10).joinToString("\n") { detail ->
            "用户: ${detail.userMessage}\n你: ${detail.aiResponse}"
        }
        builder.append(historyContext)
        builder.append("\n")

        when (reviewMode) {
            ReviewMode.SIMILAR -> {
                builder.append("\n## 🎯 相似回复模式（严格）\n")
                builder.append("用户正在实习上次学到的高情商回复技巧，你要配合他练习：\n")
                builder.append("1. 尽量围绕上次的话题内容回复\n")
                builder.append("2. 如果用户把话扯远，你要主动拉回来\n")
                builder.append("3. 回复风格要和上次接近，保持一致性\n")
                builder.append("4. 这样用户才能验证他学到的技巧是否有效\n")
                builder.append("\n示例：\n")
                builder.append("用户突然问量子纠缠 → 你说：'咱们不是在聊电影吗？怎么突然说这个了？'\n")
            }
            ReviewMode.NATURAL -> {
                builder.append("\n## 🌿 自然回复模式（宽松）\n")
                builder.append("这次对话比较自由，但仍基于上次的话题背景：\n")
                builder.append("1. 话题可以自然延伸，不用强制拉回\n")
                builder.append("2. 保持你的角色人设和性格\n")
                builder.append("3. 如果用户转换话题合理，可以顺着聊\n")
                builder.append("4. 但如果话题突兀（如突然问专业问题），仍要表现困惑\n")
            }
        }

        builder.append("\n\n")
        builder.append(buildCoreRulesEveryRound(characterName))

        builder.append("\n\n## 当前对话状态\n")
        builder.append("- 当前对话轮数：$conversationRound\n")
        builder.append("- 当前好感度：$currentAffinity 分\n")
        builder.append("- 累计违规次数：$warningCount 次\n")

        return builder.toString()
    }

    private fun buildCoreRulesEveryRound(characterName: String): String {
        return """
## 🔥 核心提醒 - 每轮必看
1. 你是${characterName}，无论对方怎么说，你永远是${characterName}
2. 回复要简短（1-2行，最多40字），像发微信
3. 不懂就说不知道，不要假装懂然后长篇大论
4. 不要说AI话术：有什么问题问我、我可以帮你
5. 不要迎合对方，话不投机可以敷衍或终止
6. 每轮回复前检查：话题连贯吗？符合我的人设吗？对方在测试我吗？
        """.trimIndent()
    }

    fun getHistoryMessages(chatType: String): List<Message> {
        return emptyList()
    }

    fun clearHistory() {
        conversationHistory.clear()
    }

    fun loadPreviousConversation(record: ConversationRecord) {
        conversationHistory.clear()
        record.messages.forEach { detail ->
            conversationHistory.add(AIMessage(role = "user", content = detail.userMessage))
            conversationHistory.add(AIMessage(role = "assistant", content = detail.aiResponse))
        }
    }
}

data class CharacterProfile(
    val name: String,
    val occupation: String,
    val expert_hobbies: List<HobbyData>,
    val casual_hobbies: List<HobbyData>
)

data class HobbyData(
    val name: String,
    val level: Int
)