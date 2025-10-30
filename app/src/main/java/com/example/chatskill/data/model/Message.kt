// 路径: app/src/main/java/com/example/chatskill/data/model/Message.kt
// 文件名: Message.kt
// 类型: 【创建】data class
package com.example.chatskill.data.model

data class Message(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val avatarUrl: String? = null,
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENDING,    // 发送中
    SENT,       // 已发送
    FAILED      // 发送失败
}