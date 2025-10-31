// 路径: app/src/main/java/com/example/chatskill/data/model/AIRequest.kt
package com.example.chatskill.data.model

data class AIRequest(
    val model: String = "gpt-3.5-turbo",
    val messages: List<AIMessage>,
    val max_tokens: Int = 1024,
    val temperature: Double = 0.7,
    val stream: Boolean = false
)

data class AIMessage(
    val role: String,
    val content: String
)

