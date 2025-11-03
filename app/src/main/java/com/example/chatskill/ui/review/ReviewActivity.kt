// 路径: app/src/main/java/com/example/chatskill/ui/review/ReviewActivity.kt
// 类型: class

package com.example.chatskill.ui.review

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.chatskill.data.model.ConversationRecord

class ReviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val record = intent.getSerializableExtra(EXTRA_RECORD) as? ConversationRecord

        if (record == null) {
            finish()
            return
        }

        setContent {
            ReviewScreen(
                record = record,
                onBackClick = { finish() },
                onRestartSimilar = {
                    navigateToChatWithMode(record, com.example.chatskill.data.model.ReviewMode.SIMILAR)
                },
                onRestartNatural = {
                    navigateToChatWithMode(record, com.example.chatskill.data.model.ReviewMode.NATURAL)
                },
                onChangeCharacter = {
                    navigateToCharacterSelection(record)
                },
                onReturnHome = {
                    navigateToHome()
                }
            )
        }
    }

    private fun navigateToChatWithMode(record: ConversationRecord, mode: com.example.chatskill.data.model.ReviewMode) {
        val config = com.example.chatskill.data.model.ChatConfig.forCustomCharacter(
            character = record.character,
            themeColor = if (record.character.gender == com.example.chatskill.data.model.Gender.FEMALE) {
                com.example.chatskill.util.Constants.Colors.MALE_THEME
            } else {
                com.example.chatskill.util.Constants.Colors.FEMALE_THEME
            }
        )

        com.example.chatskill.ui.chat.ChatActivity.start(
            context = this,
            config = config,
            enableAIToAI = false,
            customCharacter = record.character,
            reviewMode = mode,
            previousRecord = record
        )
        finish()
    }

    private fun navigateToCharacterSelection(record: ConversationRecord) {
        val isMale = record.character.gender == com.example.chatskill.data.model.Gender.MALE
        val themeColor = if (isMale) {
            com.example.chatskill.util.Constants.Colors.FEMALE_THEME
        } else {
            com.example.chatskill.util.Constants.Colors.MALE_THEME
        }

        com.example.chatskill.ui.character.CharacterCustomizationActivity.start(
            context = this,
            isMale = !isMale,
            themeColor = themeColor
        )
        finish()
    }

    private fun navigateToHome() {
        val intent = Intent(this, com.example.chatskill.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
        finish()
    }

    companion object {
        private const val EXTRA_RECORD = "conversation_record"

        fun start(context: Context, record: ConversationRecord) {
            val intent = Intent(context, ReviewActivity::class.java).apply {
                putExtra(EXTRA_RECORD, record)
            }
            context.startActivity(intent)
        }
    }
}