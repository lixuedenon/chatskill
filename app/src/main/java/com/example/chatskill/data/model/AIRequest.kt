// 路径: app/src/main/java/com/example/chatskill/data/model/AIRequest.kt
// 文件名: AIRequest.kt
// 类型: 【创建】data class
package com.example.chatskill.data.model

data class AIRequest(
    val model: String = "claude-3-5-sonnet-20241022",
    val messages: List<AIMessage>,
    val max_tokens: Int = 1024,
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

data class AIMessage(
    val role: String,  // "user" or "assistant"
    val content: String
)