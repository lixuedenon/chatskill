// 路径: app/src/main/java/com/example/chatskill/data/model/ConversationRecord.kt
// 类型: data class

package com.example.chatskill.data.model

import java.io.Serializable

data class ConversationRecord(
    val conversationId: String,
    val character: CustomCharacter,
    val startTime: Long,
    val endTime: Long = 0,
    val totalRounds: Int = 0,
    val messages: List<MessageDetail> = emptyList(),
    val affinityHistory: List<AffinityPoint> = emptyList(),
    val finalAffinity: Int = 50,
    val averageAffinity: Double = 50.0,
    val warningCount: Int = 0,
    val positiveCount: Int = 0,
    val negativeCount: Int = 0
) : Serializable

data class MessageDetail(
    val round: Int,
    val userMessage: String,
    val aiResponse: String,
    val affinityChange: Int,
    val affinityReason: String,
    val currentAffinity: Int,
    val aiMood: String,
    val timestamp: Long
) : Serializable

data class AffinityPoint(
    val round: Int,
    val affinity: Int
) : Serializable