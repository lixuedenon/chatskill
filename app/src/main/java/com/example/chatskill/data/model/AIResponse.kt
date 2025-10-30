// 路径: app/src/main/java/com/example/chatskill/data/model/AIResponse.kt
// 文件名: AIResponse.kt
// 类型: 【创建】data class
package com.example.chatskill.data.model

data class AIResponse(
    val id: String,
    val type: String,
    val role: String,
    val content: List<ContentBlock>,
    val model: String,
    val stop_reason: String?
)

data class ContentBlock(
    val type: String,
    val text: String
)