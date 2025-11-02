package com.example.chatskill.data.model

data class Message(
    val id: String = System.currentTimeMillis().toString(),
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val avatarUrl: String? = null,
    val status: MessageStatus = MessageStatus.SENT,
    val affinityChange: Int = 0,
    val affinityReason: String = "",
    val currentAffinity: Int = 50,
    val aiMood: String = "",
    val isTermination: Boolean = false
)

enum class MessageStatus {
    SENDING,
    SENT,
    FAILED
}