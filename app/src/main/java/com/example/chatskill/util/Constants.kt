// 路径: app/src/main/java/com/example/chatskill/util/Constants.kt
// 类型: object

package com.example.chatskill.util

object Constants {

    // 颜色常量
    object Colors {
        const val MALE_THEME = 0xFF4CAF50L
        const val FEMALE_THEME = 0xFFE91E63L
        const val NEUTRAL_GRAY = 0xFF9E9E9EL
    }

    // 消息相关
    object Message {
        const val MAX_INPUT_LENGTH = 500
        const val TYPING_DELAY = 1000L
    }

    // API相关
    object Api {
        const val BASE_URL = "https://api.anthropic.com/"
        const val TIMEOUT = 30000L
    }

    // 对话限制
    object Conversation {
        const val MAX_ROUNDS = 50
        const val REVIEW_THRESHOLD = 5
        const val MAX_VIOLATIONS = 3
        const val WARNING_THRESHOLD_ROUNDS = 45
    }

    // Toast文案
    object ToastMessages {
        const val WARNING_1 = "⚠️ 警告：对方似乎有点不高兴..."
        const val WARNING_2 = "🚨 严重警告：对方已经很不耐烦了！"
        const val WARNING_3 = "❌ 对话失败：对方不想再聊了"
        const val AFFINITY_DROP = "💔 对方似乎有点不高兴..."
    }
}