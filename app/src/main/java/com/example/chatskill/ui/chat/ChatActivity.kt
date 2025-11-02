package com.example.chatskill.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.core.view.WindowCompat
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.data.model.CustomCharacter
import com.example.chatskill.ui.review.ReviewActivity

class ChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        val config = intent.getSerializableExtra(EXTRA_CHAT_CONFIG) as? ChatConfig
        val enableAIToAI = intent.getBooleanExtra(EXTRA_ENABLE_AI_TO_AI, false)
        val customCharacter = intent.getSerializableExtra(EXTRA_CUSTOM_CHARACTER) as? CustomCharacter

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
                    onReviewClick = {
                        val record = viewModel.getConversationRecord()
                        if (record != null) {
                            ReviewActivity.start(this, record)
                        }
                    },
                    enableAIToAI = enableAIToAI
                )
            }
        }

        viewModel.initialize(config, customCharacter)
    }

    companion object {
        private const val EXTRA_CHAT_CONFIG = "extra_chat_config"
        private const val EXTRA_ENABLE_AI_TO_AI = "extra_enable_ai_to_ai"
        private const val EXTRA_CUSTOM_CHARACTER = "extra_custom_character"

        fun start(
            context: Context,
            config: ChatConfig,
            enableAIToAI: Boolean = true,
            customCharacter: CustomCharacter? = null
        ) {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_CHAT_CONFIG, config)
                putExtra(EXTRA_ENABLE_AI_TO_AI, enableAIToAI)
                putExtra(EXTRA_CUSTOM_CHARACTER, customCharacter)
            }
            context.startActivity(intent)
        }
    }
}