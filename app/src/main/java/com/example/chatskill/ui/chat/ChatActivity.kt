// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatActivity.kt
// 类型: class

package com.example.chatskill.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.example.chatskill.data.model.*
import com.example.chatskill.ui.character.CharacterCustomizationActivity
import com.example.chatskill.ui.review.ReviewActivity
import kotlinx.coroutines.delay

class ChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = intent.getSerializableExtra("config") as? ChatConfig
        val customCharacter = intent.getSerializableExtra("customCharacter") as? CustomCharacter
        val characterBackground = intent.getSerializableExtra("characterBackground") as? CharacterBackground
        val reviewMode = intent.getSerializableExtra("reviewMode") as? ReviewMode
        val previousRecord = intent.getSerializableExtra("previousRecord") as? ConversationRecord

        if (config == null) {
            finish()
            return
        }

        viewModel.initialize(
            config = config,
            character = customCharacter,
            background = characterBackground,
            mode = reviewMode,
            record = previousRecord
        )

        // 处理返回按钮
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (shouldShowExitDialog()) {
                    handleShowReview()
                } else {
                    finish()
                }
            }
        })

        setContent {
            val shouldForceExit by viewModel.shouldForceExit.collectAsState()

            // 监听强制退出
            LaunchedEffect(shouldForceExit) {
                if (shouldForceExit) {
                    delay(3000)
                    navigateToCharacterSelection(config)
                }
            }

            ChatScreen(
                viewModel = viewModel,
                config = config,
                onBackClick = {
                    if (shouldShowExitDialog()) {
                        handleShowReview()
                    } else {
                        finish()
                    }
                }
            )
        }
    }

    private fun shouldShowExitDialog(): Boolean {
        val conversationRound = viewModel.conversationRound.value
        val canShowReview = viewModel.canShowReview.value
        return conversationRound > 5 && canShowReview
    }

    private fun handleShowReview() {
        val config = intent.getSerializableExtra("config") as? ChatConfig
        val record = viewModel.getConversationRecord()
        if (record != null && config != null) {
            ReviewActivity.start(
                context = this,
                record = record  // 修改：参数名改为 record
            )
        }
        finish()
    }

    private fun navigateToCharacterSelection(config: ChatConfig) {
        val customCharacter = viewModel.getConversationRecord()?.character
        if (customCharacter != null) {
            val isMale = customCharacter.gender == Gender.MALE
            CharacterCustomizationActivity.start(
                context = this,
                isMale = !isMale,
                themeColor = config.themeColor
            )
        }
        finish()
    }

    companion object {
        fun start(
            context: Context,
            config: ChatConfig,
            enableAIToAI: Boolean = false,
            customCharacter: CustomCharacter? = null,
            characterBackground: CharacterBackground? = null,
            reviewMode: ReviewMode? = null,
            previousRecord: ConversationRecord? = null
        ) {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra("config", config)
                putExtra("enableAIToAI", enableAIToAI)
                putExtra("customCharacter", customCharacter)
                putExtra("characterBackground", characterBackground)
                putExtra("reviewMode", reviewMode)
                putExtra("previousRecord", previousRecord)
            }
            context.startActivity(intent)
        }
    }
}