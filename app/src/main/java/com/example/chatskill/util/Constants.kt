// 路径: app/src/main/java/com/example/chatskill/util/Constants.kt
// 文件名: Constants.kt
// 类型: 【创建】object
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
}