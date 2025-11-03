// 路径: app/src/main/java/com/example/chatskill/ui/character/CharacterCustomizationActivity.kt
// 类型: class

package com.example.chatskill.ui.character

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.graphics.Color
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.data.model.ChatType
import com.example.chatskill.data.model.CustomCharacter
import com.example.chatskill.ui.chat.ChatActivity

class CharacterCustomizationActivity : ComponentActivity() {

    private val viewModel: CharacterCustomizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isMale = intent.getBooleanExtra(EXTRA_IS_MALE, true)
        val themeColorLong = intent.getLongExtra(EXTRA_THEME_COLOR, 0xFF4CAF50)
        val themeColor = Color(themeColorLong)

        setContent {
            CharacterCustomizationScreen(
                isMale = isMale,
                themeColor = themeColor,
                viewModel = viewModel,
                onBackClick = { finish() },
                onConfirm = { character ->
                    startChatWithCustomCharacter(character, themeColorLong)
                }
            )
        }
    }

    private fun startChatWithCustomCharacter(
        character: CustomCharacter,
        themeColor: Long
    ) {
        val config = ChatConfig(
            chatType = ChatType.BASIC_CHAT,
            title = character.name,
            themeColor = themeColor,
            systemPrompt = character.toSystemPrompt(),
            avatarUrl = null,
            enableVoice = false,
            placeholder = "输入消息..."
        )

        ChatActivity.start(
            context = this,
            config = config,
            enableAIToAI = false,
            customCharacter = character
        )
        finish()
    }

    companion object {
        private const val EXTRA_IS_MALE = "is_male"
        private const val EXTRA_THEME_COLOR = "theme_color"

        fun start(context: Context, isMale: Boolean, themeColor: Long) {
            val intent = Intent(context, CharacterCustomizationActivity::class.java).apply {
                putExtra(EXTRA_IS_MALE, isMale)
                putExtra(EXTRA_THEME_COLOR, themeColor)
            }
            context.startActivity(intent)
        }
    }
}