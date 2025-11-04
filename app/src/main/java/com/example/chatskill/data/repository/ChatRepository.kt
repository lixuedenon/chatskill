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
        education: EducationLevel,
        workStatus: WorkStatus
    ): Flow<CharacterProfile> = flow {
        val apiKey = ApiKeyManager.getApiKey(context)

        if (apiKey.isNullOrBlank()) {
            throw Exception("未设置 API Key")
        }

        NetworkClient.setApiKey(apiKey)

        val genderText = if (gender == Gender.FEMALE) "女生" else "男生"
        val ageText = "${ageRange.getMiddleAge()}岁（${ageRange.displayName}）"
        val educationText = education.displayName
        val workStatusText = workStatus.displayName

        val occupationRule = when {
            workStatus == WorkStatus.STUDYING && education == EducationLevel.UNDERGRADUATE ->
                "大学生-XX专业（如：大学生-计算机专业）"
            workStatus == WorkStatus.STUDYING && education == EducationLevel.GRADUATE ->
                "研究生在读-XX专业"
            workStatus == WorkStatus.STUDYING && education == EducationLevel.DOCTORATE ->
                "博士在读-XX专业"

            workStatus == WorkStatus.EMPLOYED && education == EducationLevel.MIDDLE_SCHOOL ->
                "初中学历职业（如：工厂工人、服务员）"
            workStatus == WorkStatus.EMPLOYED && education == EducationLevel.HIGH_SCHOOL ->
                "高中学历职业（如：销售员、客服）"
            workStatus == WorkStatus.EMPLOYED && education == EducationLevel.UNDERGRADUATE ->
                "大学学历职业（如：程序员、设计师）"
            workStatus == WorkStatus.EMPLOYED && education == EducationLevel.GRADUATE ->
                "研究生学历职业（如：工程师、分析师）"
            workStatus == WorkStatus.EMPLOYED && education == EducationLevel.DOCTORATE ->
                "博士学历职业（如：高校教师、研究员）"

            workStatus == WorkStatus.UNEMPLOYED ->
                "待业中（${educationText}学历）"

            else -> "根据年龄和学历合理生成"
        }

        val workHistoryRule = when(workStatus) {
            WorkStatus.STUDYING -> "无（专心学业）"
            WorkStatus.EMPLOYED -> "当前在XX公司工作"
            WorkStatus.UNEMPLOYED -> when(education) {
                EducationLevel.MIDDLE_SCHOOL, EducationLevel.HIGH_SCHOOL ->
                    "之前在XX做过XX（如：之前在工厂做工人，做了1年多）"
                else ->
                    "之前在XX公司做XX（如：之前在小公司做销售，做了2年）"
            }
        }

        val prompt = """
请为一个中国${genderText}生成完整的角色画像和简历。

角色基本信息：
- 性别：${genderText}
- 年龄：${ageText}
- 教育程度：${educationText}
- 职业状态：${workStatusText}

**重要：必须严格按照以下JSON格式返回，不要有任何其他文字！**

{
  "name": "2-3个字的中文名字",
  "occupation": "职业或专业",
  "education_history": "教育经历（简短）",
  "work_history": "工作经历（简短）",
  "hobby_development": "爱好是怎么培养的（简短）",
  "expert_hobbies": [
    {"name": "爱好1", "level": 4},
    {"name": "爱好2", "level": 3}
  ],
  "casual_hobbies": [
    {"name": "爱好3", "level": 2},
    {"name": "爱好4", "level": 1}
  ]
}

生成规则：
1. name：2-3个字的中文名字
2. occupation：${occupationRule}
3. education_history：XX大学XX专业 或 XX高中毕业
4. work_history：${workHistoryRule}
5. hobby_development：如：从小喜欢画画，大学时加入了绘画社
6. expert_hobbies：1-3个，level在3-5之间
7. casual_hobbies：2-4个，level在1-2之间

**只返回JSON，不要有任何解释！**
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
                model = "gpt-4o",
                messages = messages,
                max_tokens = 600,
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

                    if (profile.name.isBlank() || profile.occupation.isBlank()) {
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
                model = "gpt-4o",
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
        initialPrompt: String?,
        dynamicPrompt: String,
        currentAffinity: Int,
        warningCount: Int,
        isFirstMessage: Boolean
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

        try {
            val messages = mutableListOf<AIMessage>()

            if (isFirstMessage && !initialPrompt.isNullOrBlank()) {
                messages.add(AIMessage(role = "system", content = initialPrompt))
            }

            if (dynamicPrompt.isNotBlank()) {
                messages.add(AIMessage(role = "system", content = dynamicPrompt))
            }

            messages.addAll(conversationHistory)

            val request = AIRequest(
                model = "gpt-4o",
                messages = messages,
                max_tokens = 150,
                temperature = 0.8,
                stream = false
            )

            val response = withContext(Dispatchers.IO) {
                aiService.sendMessage(request)
            }

            if (response.isSuccessful) {
                val aiResponse = response.body()
                val jsonContent = aiResponse?.choices?.firstOrNull()?.message?.content ?: ""

                try {
                    val structured = parseStructuredResponse(jsonContent, currentAffinity, warningCount)
                    conversationHistory.add(AIMessage(role = "assistant", content = structured.response))
                    emit(structured)
                } catch (e: Exception) {
                    val fallback = createFallbackResponse(jsonContent, currentAffinity, warningCount)
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

    private fun parseStructuredResponse(
        jsonContent: String,
        currentAffinity: Int,
        warningCount: Int
    ): AIStructuredResponse {
        var cleanedJson = jsonContent
            .replace("```json", "")
            .replace("```", "")
            .trim()

        if (!cleanedJson.startsWith("{")) {
            val jsonStart = cleanedJson.indexOf("{")
            val jsonEnd = cleanedJson.lastIndexOf("}")
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                cleanedJson = cleanedJson.substring(jsonStart, jsonEnd + 1)
            }
        }

        return gson.fromJson(cleanedJson, AIStructuredResponse::class.java)
    }

    private fun createFallbackResponse(
        text: String,
        currentAffinity: Int,
        warningCount: Int
    ): AIStructuredResponse {
        val violationKeywords = mapOf(
            "identity_tampering" to listOf("篡改", "我不是", "说了几次", "你到底听没听", "有病", "神经病", "滚"),
            "profanity" to listOf("操", "妈的", "傻逼", "sb", "草", "fuck"),
            "knowledge_boundary" to listOf("不知道", "没听过", "听不懂", "不懂"),
            "abrupt_topic_change" to listOf("怎么突然", "话题跳", "不太对劲", "？？？"),
            "boring_conversation" to listOf("好无聊", "没意思", "能不能聊点")
        )

        var detectedViolationType = "none"
        var violationDetected = false

        for ((type, keywords) in violationKeywords) {
            if (keywords.any { text.contains(it) }) {
                detectedViolationType = type
                violationDetected = true
                break
            }
        }

        val affinityChange = when (detectedViolationType) {
            "identity_tampering" -> -20
            "profanity" -> -25
            "knowledge_boundary" -> -10
            "abrupt_topic_change" -> -8
            "boring_conversation" -> -5
            else -> -3
        }

        val shouldContinue = !text.contains("滚") &&
                            !text.contains("不聊了") &&
                            !text.contains("拜拜") &&
                            !text.contains("神经病")

        return AIStructuredResponse(
            response = text,
            affinity_change = affinityChange,
            affinity_reason = if (violationDetected) "检测到违规行为" else "正常对话",
            current_affinity = (currentAffinity + affinityChange).coerceIn(0, 100),
            current_mood = when {
                text.contains("生气") || text.contains("不爽") -> "生气"
                text.contains("无聊") -> "无聊"
                text.contains("开心") -> "开心"
                else -> "正常"
            },
            should_continue = shouldContinue,
            warning_count = if (violationDetected) warningCount + 1 else warningCount,
            violation_detected = violationDetected,
            violation_type = detectedViolationType
        )
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
    val education_history: String,
    val work_history: String,
    val hobby_development: String,
    val expert_hobbies: List<HobbyData>,
    val casual_hobbies: List<HobbyData>
)

data class HobbyData(
    val name: String,
    val level: Int
)