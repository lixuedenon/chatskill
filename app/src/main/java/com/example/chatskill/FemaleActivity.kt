// 路径: app/src/main/java/com/example/chatskill/FemaleActivity.kt
// 文件名: FemaleActivity.kt
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

class FemaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FemaleScreen(
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
fun FemaleScreen(
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
                    containerColor = Color(0xFFE91E63)
                )
            ) {
                Text(text = "←", fontSize = 20.sp)
            }

            Text(
                text = "女生篇",
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
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forBasicChat(false)) }
            )
            ModuleButton(
                text = "2. 社交雷达",
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forSocialRadar(false)) }
            )
            ModuleButton(
                text = "3. 男友养成",
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forBoyfriendGrowth()) }
            )
            ModuleButton(
                text = "4. 定制男友",
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forCustomBoyfriend()) }
            )
            ModuleButton(
                text = "5. 真人对话助手",
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forRealChatAssistant(false)) }
            )
            ModuleButton(
                text = "6. 实战训练营",
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forPracticalTraining(false)) }
            )
            ModuleButton(
                text = "7. 反PUA模块",
                color = Color(0xFFE91E63),
                onClick = { onModuleClick(ChatConfig.forAntiPUA()) },
                isLast = true
            )
        }
    }
}