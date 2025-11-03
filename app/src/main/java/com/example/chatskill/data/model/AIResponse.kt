// 路径: app/src/main/java/com/example/chatskill/data/model/AIResponse.kt
// 类型: data class

package com.example.chatskill.data.model

data class AIResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val index: Int,
    val message: MessageContent,
    val finish_reason: String?
)

data class MessageContent(
    val role: String,
    val content: String
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class AIStructuredResponse(
    val response: String,
    val affinity_change: Int,
    val affinity_reason: String,
    val current_affinity: Int,
    val current_mood: String,
    val should_continue: Boolean,
    val warning_count: Int,
    val violation_detected: Boolean = false,
    val violation_type: String = "none"
)