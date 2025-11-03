// 路径: app/src/main/java/com/example/chatskill/util/ToastManager.kt
// 类型: object

package com.example.chatskill.util

import android.content.Context
import android.widget.Toast

object ToastManager {

    private var currentToast: Toast? = null

    fun showViolationWarning(context: Context, violationCount: Int) {
        currentToast?.cancel()

        val message = when (violationCount) {
            1 -> "⚠️ 警告：对方似乎有点不高兴..."
            2 -> "🚨 严重警告：对方已经很不耐烦了！"
            3 -> "❌ 对话失败：对方不想再聊了"
            else -> return
        }

        currentToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        currentToast?.show()
    }

    fun showAffinityDrop(context: Context) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, "💔 对方似乎有点不高兴...", Toast.LENGTH_SHORT)
        currentToast?.show()
    }
}