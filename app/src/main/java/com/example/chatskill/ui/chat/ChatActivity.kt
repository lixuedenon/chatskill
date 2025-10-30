// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatActivity.kt
// 文件名: ChatActivity.kt
// 操作: 【完整替换】
package com.example.chatskill.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.example.chatskill.data.model.ChatConfig

class ChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = intent.getSerializableExtra(EXTRA_CHAT_CONFIG) as? ChatConfig
        val enableAIToAI = intent.getBooleanExtra(EXTRA_ENABLE_AI_TO_AI, false)

        if (config == null) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                ChatScreen(
                    config = config,
                    viewModel = viewModel,
                    onBackClick = { finish() },
                    enableAIToAI = enableAIToAI
                )
            }
        }
    }

    companion object {
        private const val EXTRA_CHAT_CONFIG = "extra_chat_config"
        private const val EXTRA_ENABLE_AI_TO_AI = "extra_enable_ai_to_ai"

        fun start(context: Context, config: ChatConfig, enableAIToAI: Boolean = true) {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_CHAT_CONFIG, config)
                putExtra(EXTRA_ENABLE_AI_TO_AI, enableAIToAI)
            }
            context.startActivity(intent)
        }
    }
}