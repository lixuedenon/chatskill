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
                onBackClick = { finish() }
            )
        }
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