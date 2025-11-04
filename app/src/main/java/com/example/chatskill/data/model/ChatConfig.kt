// 路径: app/src/main/java/com/example/chatskill/data/model/ChatConfig.kt
// 类型: data class

package com.example.chatskill.data.model

import java.io.Serializable

data class ChatConfig(
    val chatType: ChatType,
    val title: String,
    val themeColor: Long,
    val systemPrompt: String,
    val avatarUrl: String?,
    val enableVoice: Boolean,
    val placeholder: String
) : Serializable {

    companion object {
        fun forBasicChat(): ChatConfig {
            return ChatConfig(
                chatType = ChatType.BASIC_CHAT,
                title = "基础对话",
                themeColor = 0xFF4CAF50,
                systemPrompt = """
                    你是一个友好、乐于助人的AI助手。
                    请用简洁、清晰的方式回答用户的问题。
                    保持礼貌和专业。
                """.trimIndent(),
                avatarUrl = null,
                enableVoice = false,
                placeholder = "输入消息..."
            )
        }

        fun forRolePlay(): ChatConfig {
            return ChatConfig(
                chatType = ChatType.ROLE_PLAY,
                title = "角色扮演",
                themeColor = 0xFFE91E63,
                systemPrompt = """
                    你正在进行角色扮演对话。
                    请根据设定的角色性格和背景来回应。
                    保持角色的一致性。
                """.trimIndent(),
                avatarUrl = null,
                enableVoice = false,
                placeholder = "继续对话..."
            )
        }

        fun forLanguageLearning(): ChatConfig {
            return ChatConfig(
                chatType = ChatType.LANGUAGE_LEARNING,
                title = "语言学习",
                themeColor = 0xFF2196F3,
                systemPrompt = """
                    你是一位语言学习教练。
                    请帮助用户练习和提高语言能力。
                    提供有建设性的反馈和纠正。
                """.trimIndent(),
                avatarUrl = null,
                enableVoice = true,
                placeholder = "练习对话..."
            )
        }

        fun forCustomCharacter(
            character: CustomCharacter,
            background: CharacterBackground,
            themeColor: Long
        ): ChatConfig {
            return ChatConfig(
                chatType = ChatType.BASIC_CHAT,
                title = character.name,
                themeColor = themeColor,
                systemPrompt = character.getInitialPrompt(background),
                avatarUrl = null,
                enableVoice = false,
                placeholder = "输入消息..."
            )
        }
    }
}

// 重要：enum 定义在 ChatConfig 类的外部
enum class ChatType {
    BASIC_CHAT,
    ROLE_PLAY,
    LANGUAGE_LEARNING
}