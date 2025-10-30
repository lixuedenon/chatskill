// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatScreen.kt
// 文件名: ChatScreen.kt
// 操作: 【完整替换】
package com.example.chatskill.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.ui.chat.components.ChatInputBar
import com.example.chatskill.ui.chat.components.MessageList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    config: ChatConfig,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    enableAIToAI: Boolean = false
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(config) {
        viewModel.initialize(config)
    }

    Scaffold(
        // 🔑 关键1：禁用 Scaffold 消费 WindowInsets
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = config.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "在线",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "菜单",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("清空对话") },
                            onClick = {
                                viewModel.clearMessages()
                                showMenu = false
                            }
                        )
                        if (enableAIToAI) {
                            DropdownMenuItem(
                                text = { Text("AI对AI对话") },
                                onClick = {
                                    viewModel.startAIToAIConversation()
                                    showMenu = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("设置") },
                            onClick = { showMenu = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = config.getThemeColor(),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        // 🔑 关键2：整个内容区域响应键盘
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.ime)  // 🔑 响应键盘
                .padding(paddingValues)
                .background(Color(0xFFFAFAFA))
        ) {
            // 🔑 关键3：消息列表用 weight 自适应
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { focusManager.clearFocus() }
                        )
                    }
            ) {
                MessageList(
                    messages = messages,
                    themeColor = config.getThemeColor(),
                    isLoading = isLoading
                )
            }

            // 输入栏
            ChatInputBar(
                value = inputText,
                onValueChange = { viewModel.onInputTextChange(it) },
                onSendClick = { viewModel.sendMessage() },
                themeColor = config.getThemeColor(),
                placeholder = config.placeholder
            )
        }
    }
}