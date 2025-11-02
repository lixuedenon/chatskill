package com.example.chatskill.data.model

import androidx.compose.ui.graphics.Color
import java.io.Serializable

data class ChatConfig(
    val chatType: ChatType,
    val title: String,
    val themeColor: Long,
    val avatarUrl: String? = null,
    val systemPrompt: String? = null,
    val enableVoice: Boolean = false,
    val placeholder: String = "输入消息..."
) : Serializable {

    fun getThemeColor(): Color = Color(themeColor)

    companion object {
        fun forBasicChat(isMale: Boolean) = ChatConfig(
            chatType = ChatType.BASIC_CHAT,
            title = "基础对话",
            themeColor = if (isMale) 0xFF4CAF50 else 0xFFE91E63,
            systemPrompt = "你是一个恋爱对话助手，帮助用户提升对话技巧。"
        )

        fun forSocialRadar(isMale: Boolean) = ChatConfig(
            chatType = ChatType.SOCIAL_RADAR,
            title = "社交雷达",
            themeColor = if (isMale) 0xFF4CAF50 else 0xFFE91E63,
            systemPrompt = "分析对方的社交信号和潜在意图。"
        )

        fun forGirlfriendGrowth() = ChatConfig(
            chatType = ChatType.GIRLFRIEND_GROWTH,
            title = "女友养成",
            themeColor = 0xFF4CAF50,
            systemPrompt = "模拟女友角色，帮助用户学习恋爱相处技巧。"
        )

        fun forBoyfriendGrowth() = ChatConfig(
            chatType = ChatType.BOYFRIEND_GROWTH,
            title = "男友养成",
            themeColor = 0xFFE91E63,
            systemPrompt = "模拟男友角色，帮助用户学习恋爱相处技巧。"
        )

        fun forCustomGirlfriend() = ChatConfig(
            chatType = ChatType.CUSTOM_GIRLFRIEND,
            title = "定制女友",
            themeColor = 0xFF4CAF50,
            enableVoice = true,
            systemPrompt = "根据用户定制的人设进行对话。"
        )

        fun forCustomBoyfriend() = ChatConfig(
            chatType = ChatType.CUSTOM_BOYFRIEND,
            title = "定制男友",
            themeColor = 0xFFE91E63,
            enableVoice = true,
            systemPrompt = "根据用户定制的人设进行对话。"
        )

        fun forRealChatAssistant(isMale: Boolean) = ChatConfig(
            chatType = ChatType.REAL_CHAT_ASSISTANT,
            title = "真人对话助手",
            themeColor = if (isMale) 0xFF4CAF50 else 0xFFE91E63,
            systemPrompt = "实时分析真人对话，提供建议。"
        )

        fun forPracticalTraining(isMale: Boolean) = ChatConfig(
            chatType = ChatType.PRACTICAL_TRAINING,
            title = "实战训练营",
            themeColor = if (isMale) 0xFF4CAF50 else 0xFFE91E63,
            systemPrompt = "通过实战场景训练对话能力。"
        )

        fun forAntiPUA() = ChatConfig(
            chatType = ChatType.ANTI_PUA,
            title = "反PUA模块",
            themeColor = 0xFFE91E63,
            systemPrompt = "识别和应对PUA技巧，保护自己。"
        )

        fun forCustomCharacter(character: CustomCharacter, themeColor: Long): ChatConfig {
            return ChatConfig(
                chatType = ChatType.BASIC_CHAT,
                title = character.name,
                themeColor = themeColor,
                systemPrompt = character.toSystemPrompt(),
                avatarUrl = null,
                enableVoice = false,
                placeholder = "输入消息..."
            )
        }
    }
}