// 路径: app/src/main/java/com/example/chatskill/ui/FemaleActivity.kt
// 类型: class

package com.example.chatskill.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.data.model.ChatType
import com.example.chatskill.ui.character.CharacterCustomizationActivity
import com.example.chatskill.ui.chat.ChatActivity

class FemaleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FemaleScreen(
                onBackClick = { finish() }
            )
        }
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, FemaleActivity::class.java)
            context.startActivity(intent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FemaleScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val themeColor = 0xFFE91E63L

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选择对话场景") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE91E63),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "选择一个场景开始对话",
                fontSize = 16.sp,
                color = Color.Gray
            )

            ModuleButton(
                text = "2. 社交雷达",
                color = Color(0xFFE91E63),
                onClick = {
                    ChatActivity.start(
                        context = context,
                        config = ChatConfig(
                            chatType = ChatType.BASIC_CHAT,
                            title = "社交雷达",
                            themeColor = themeColor,
                            systemPrompt = "你是一个社交情境分析助手。",
                            avatarUrl = null,
                            enableVoice = false,
                            placeholder = "描述你的社交困扰..."
                        ),
                        enableAIToAI = false
                    )
                }
            )

            ModuleButton(
                text = "3. 男友养成",
                color = Color(0xFFE91E63),
                onClick = {
                    ChatActivity.start(
                        context = context,
                        config = ChatConfig(
                            chatType = ChatType.ROLE_PLAY,
                            title = "男友养成",
                            themeColor = themeColor,
                            systemPrompt = "你是一个理想的男友角色。",
                            avatarUrl = null,
                            enableVoice = false,
                            placeholder = "和我聊聊天吧..."
                        ),
                        enableAIToAI = false
                    )
                }
            )

            ModuleButton(
                text = "4. 自定义男友",
                color = Color(0xFFE91E63),
                onClick = {
                    CharacterCustomizationActivity.start(
                        context = context,
                        isMale = true,
                        themeColor = themeColor
                    )
                }
            )

            ModuleButton(
                text = "5. 真实聊天助手",
                color = Color(0xFFE91E63),
                onClick = {
                    ChatActivity.start(
                        context = context,
                        config = ChatConfig(
                            chatType = ChatType.BASIC_CHAT,
                            title = "真实聊天助手",
                            themeColor = themeColor,
                            systemPrompt = "你是一个聊天分析助手。",
                            avatarUrl = null,
                            enableVoice = false,
                            placeholder = "贴入聊天记录..."
                        ),
                        enableAIToAI = false
                    )
                }
            )

            ModuleButton(
                text = "6. 实用技能",
                color = Color(0xFFE91E63),
                onClick = {
                    ChatActivity.start(
                        context = context,
                        config = ChatConfig(
                            chatType = ChatType.BASIC_CHAT,
                            title = "实用技能",
                            themeColor = themeColor,
                            systemPrompt = "你是一个生活技能导师。",
                            avatarUrl = null,
                            enableVoice = false,
                            placeholder = "我想学习..."
                        ),
                        enableAIToAI = false
                    )
                }
            )

            ModuleButton(
                text = "7. 反PUA",
                color = Color(0xFFE91E63),
                onClick = {
                    ChatActivity.start(
                        context = context,
                        config = ChatConfig(
                            chatType = ChatType.BASIC_CHAT,
                            title = "反PUA助手",
                            themeColor = themeColor,
                            systemPrompt = "你是一个反PUA专家。",
                            avatarUrl = null,
                            enableVoice = false,
                            placeholder = "描述你的情况..."
                        ),
                        enableAIToAI = false
                    )
                }
            )
        }
    }
}

@Composable
private fun ModuleButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}