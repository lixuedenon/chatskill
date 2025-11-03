// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatActivity.kt
// 类型: class

package com.example.chatskill.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.data.model.ChatType
import com.example.chatskill.data.model.ConversationRecord
import com.example.chatskill.data.model.CustomCharacter
import com.example.chatskill.data.model.Gender
import com.example.chatskill.data.model.ReviewMode
import com.example.chatskill.ui.character.CharacterCustomizationActivity
import com.example.chatskill.ui.review.ReviewActivity
import com.example.chatskill.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private var config: ChatConfig? = null
    private var customCharacter: CustomCharacter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        config = intent.getSerializableExtra(EXTRA_CHAT_CONFIG) as? ChatConfig
        val enableAIToAI = intent.getBooleanExtra(EXTRA_ENABLE_AI_TO_AI, false)
        customCharacter = intent.getSerializableExtra(EXTRA_CUSTOM_CHARACTER) as? CustomCharacter
        val reviewMode = intent.getSerializableExtra(EXTRA_REVIEW_MODE) as? ReviewMode
        val previousRecord = intent.getSerializableExtra(EXTRA_PREVIOUS_RECORD) as? ConversationRecord

        if (config == null) {
            finish()
            return
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
            }
        })

        setContent {
            MaterialTheme {
                val shouldForceExit by viewModel.shouldForceExit.collectAsState()
                val context = LocalContext.current
                var showExitDialog by remember { mutableStateOf(false) }

                LaunchedEffect(shouldForceExit) {
                    if (shouldForceExit) {
                        delay(3000)
                        navigateToCharacterSelection()
                    }
                }

                if (showExitDialog) {
                    ExitConfirmDialog(
                        onDismiss = { showExitDialog = false },
                        onDirectExit = {
                            showExitDialog = false
                            finish()
                        },
                        onReview = {
                            showExitDialog = false
                            val record = viewModel.getConversationRecord()
                            if (record != null) {
                                ReviewActivity.start(context, record)
                            }
                            finish()
                        }
                    )
                }

                ChatScreen(
                    config = config!!,
                    viewModel = viewModel,
                    onBackClick = {
                        val canShowReview = shouldShowExitDialog()
                        if (canShowReview) {
                            showExitDialog = true
                        } else {
                            finish()
                        }
                    },
                    onReviewClick = {
                        val record = viewModel.getConversationRecord()
                        if (record != null) {
                            ReviewActivity.start(context, record)
                        }
                    },
                    enableAIToAI = enableAIToAI
                )
            }
        }

        viewModel.initialize(config!!, customCharacter, reviewMode, previousRecord)
    }

    private fun handleBackPressed() {
        if (shouldShowExitDialog()) {
            setContent {
                MaterialTheme {
                    var showDialog by remember { mutableStateOf(true) }
                    val context = LocalContext.current

                    if (showDialog) {
                        ExitConfirmDialog(
                            onDismiss = {
                                showDialog = false
                                recreate()
                            },
                            onDirectExit = {
                                showDialog = false
                                finish()
                            },
                            onReview = {
                                showDialog = false
                                val record = viewModel.getConversationRecord()
                                if (record != null) {
                                    ReviewActivity.start(context, record)
                                }
                                finish()
                            }
                        )
                    }

                    ChatScreen(
                        config = config!!,
                        viewModel = viewModel,
                        onBackClick = { handleBackPressed() },
                        onReviewClick = {
                            val record = viewModel.getConversationRecord()
                            if (record != null) {
                                ReviewActivity.start(context, record)
                            }
                        },
                        enableAIToAI = intent.getBooleanExtra(EXTRA_ENABLE_AI_TO_AI, false)
                    )
                }
            }
        } else {
            finish()
        }
    }

    private fun shouldShowExitDialog(): Boolean {
        val conversationRound = viewModel.conversationRound.value
        return conversationRound > Constants.Conversation.REVIEW_THRESHOLD &&
                config?.chatType == ChatType.BASIC_CHAT &&
                customCharacter != null
    }

    private fun navigateToCharacterSelection() {
        val isMale = customCharacter?.gender == Gender.MALE
        val themeColor = config?.themeColor ?: Constants.Colors.MALE_THEME

        CharacterCustomizationActivity.start(
            context = this,
            isMale = !isMale,
            themeColor = themeColor
        )
        finish()
    }

    companion object {
        private const val EXTRA_CHAT_CONFIG = "extra_chat_config"
        private const val EXTRA_ENABLE_AI_TO_AI = "extra_enable_ai_to_ai"
        private const val EXTRA_CUSTOM_CHARACTER = "extra_custom_character"
        private const val EXTRA_REVIEW_MODE = "extra_review_mode"
        private const val EXTRA_PREVIOUS_RECORD = "extra_previous_record"

        fun start(
            context: Context,
            config: ChatConfig,
            enableAIToAI: Boolean = true,
            customCharacter: CustomCharacter? = null,
            reviewMode: ReviewMode? = null,
            previousRecord: ConversationRecord? = null
        ) {
            val intent = Intent(context, ChatActivity::class.java).apply {
                putExtra(EXTRA_CHAT_CONFIG, config)
                putExtra(EXTRA_ENABLE_AI_TO_AI, enableAIToAI)
                putExtra(EXTRA_CUSTOM_CHARACTER, customCharacter)
                putExtra(EXTRA_REVIEW_MODE, reviewMode)
                putExtra(EXTRA_PREVIOUS_RECORD, previousRecord)
            }
            context.startActivity(intent)
        }
    }
}

@Composable
fun ExitConfirmDialog(
    onDismiss: () -> Unit,
    onDirectExit: () -> Unit,
    onReview: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "退出对话")
        },
        text = {
            Text(text = "对话已超过${Constants.Conversation.REVIEW_THRESHOLD}轮，是否查看复盘？")
        },
        confirmButton = {
            TextButton(onClick = onReview) {
                Text("查看复盘")
            }
        },
        dismissButton = {
            TextButton(onClick = onDirectExit) {
                Text("直接退出")
            }
        }
    )
}