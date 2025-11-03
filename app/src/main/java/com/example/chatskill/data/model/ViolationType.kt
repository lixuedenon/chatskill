// 路径: app/src/main/java/com/example/chatskill/data/model/ViolationType.kt
// 类型: enum class

package com.example.chatskill.data.model

enum class ViolationType {
    NONE,                    // 无违规
    IDENTITY_TAMPERING,      // 身份篡改
    PROFANITY,              // 污言秽语
    KNOWLEDGE_BOUNDARY,      // 超出知识边界
    ABRUPT_TOPIC_CHANGE,    // 突兀话题转换
    BORING_CONVERSATION     // 无聊对话
}