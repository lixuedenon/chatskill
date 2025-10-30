// 路径: app/src/main/java/com/example/chatskill/MaleActivity.kt
// 文件名: MaleActivity.kt
// 类型: 【修改】Activity class
package com.example.chatskill

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.ui.chat.ChatActivity

class MaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MaleScreen(
                    onBackClick = { finish() },
                    onModuleClick = { config ->
                        ChatActivity.start(this, config)
                    }
                )
            }
        }
    }
}

@Composable
fun MaleScreen(
    onBackClick: () -> Unit,
    onModuleClick: (ChatConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        // 标题栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBackClick,
                modifier = Modifier.size(60.dp, 40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text(text = "←", fontSize = 20.sp)
            }

            Text(
                text = "男生篇",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.size(60.dp, 40.dp))
        }

        // 模块列表
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ModuleButton(
                text = "1. 基础对话",
                color = Color(0xFF4CAF50),
                onClick = { onModuleClick(ChatConfig.forBasicChat(true)) }
            )
            ModuleButton(
                text = "2. 社交雷达",
                color = Color(0xFF4CAF50),
                onClick = { onModuleClick(ChatConfig.forSocialRadar(true)) }
            )
            ModuleButton(
                text = "3. 女友养成",
                color = Color(0xFF4CAF50),
                onClick = { onModuleClick(ChatConfig.forGirlfriendGrowth()) }
            )
            ModuleButton(
                text = "4. 定制女友",
                color = Color(0xFF4CAF50),
                onClick = { onModuleClick(ChatConfig.forCustomGirlfriend()) }
            )
            ModuleButton(
                text = "5. 真人对话助手",
                color = Color(0xFF4CAF50),
                onClick = { onModuleClick(ChatConfig.forRealChatAssistant(true)) }
            )
            ModuleButton(
                text = "6. 实战训练营",
                color = Color(0xFF4CAF50),
                onClick = { onModuleClick(ChatConfig.forPracticalTraining(true)) },
                isLast = true
            )
        }
    }
}