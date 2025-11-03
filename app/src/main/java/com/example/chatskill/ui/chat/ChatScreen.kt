// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatScreen.kt
// 类型: composable

package com.example.chatskill.ui.chat

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.ui.chat.components.ChatInputBar
import com.example.chatskill.ui.chat.components.MessageList
import com.example.chatskill.util.ApiKeyManager
import com.example.chatskill.util.ToastManager

private const val TAG = "ChatScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    config: ChatConfig,
    viewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onReviewClick: () -> Unit = {},
    enableAIToAI: Boolean = false
) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val canShowReview by viewModel.canShowReview.collectAsState()
    val isMaxRoundReached by viewModel.isMaxRoundReached.collectAsState()
    val showToastWarning by viewModel.showToastWarning.collectAsState()
    val shouldForceExit by viewModel.shouldForceExit.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val density = LocalDensity.current
    val imeHeightPx = WindowInsets.ime.getBottom(density)
    val imeHeightDp = with(density) { imeHeightPx.toDp() }

    LaunchedEffect(showToastWarning) {
        showToastWarning?.let { violationCount ->
            ToastManager.showViolationWarning(context, violationCount)
            viewModel.clearToastWarning()
        }
    }

    LaunchedEffect(config) {
        if (!ApiKeyManager.hasApiKey(context)) {
            showApiKeyDialog = true
        }
    }

    LaunchedEffect(imeHeightPx) {
        Log.d(TAG, "🎹 键盘高度变化: ${imeHeightPx}px = ${imeHeightDp}")
        Log.d(TAG, "📱 键盘状态: ${if (imeHeightPx > 0) "弹起" else "收起"}")
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            onDismiss = { showApiKeyDialog = false },
            onSave = { apiKey ->
                ApiKeyManager.saveApiKey(context, apiKey)
                showApiKeyDialog = false
            }
        )
    }

    Scaffold(
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

                        if (canShowReview) {
                            DropdownMenuItem(
                                text = { Text("对话复盘") },
                                onClick = {
                                    onReviewClick()
                                    showMenu = false
                                }
                            )
                        }

                        if (enableAIToAI) {
                            DropdownMenuItem(
                                text = { Text("AI对AI对话") },
                                onClick = {
                                    showMenu = false
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("设置 API Key") },
                            onClick = {
                                showApiKeyDialog = true
                                showMenu = false
                            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA))
                .statusBarsPadding()
                .padding(paddingValues)
                .imePadding()
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                Log.d(TAG, "👆 点击空白区域，收起键盘")
                                focusManager.clearFocus()
                            }
                        )
                    }
            ) {
                MessageList(
                    messages = messages,
                    themeColor = config.getThemeColor(),
                    isLoading = isLoading,
                    imeHeight = imeHeightPx
                )
            }

            if (!isMaxRoundReached && !shouldForceExit) {
                ChatInputBar(
                    value = inputText,
                    onValueChange = { viewModel.onInputTextChange(it) },
                    onSendClick = {
                        Log.d(TAG, "📤 发送消息: $inputText")
                        viewModel.sendMessage()
                    },
                    themeColor = config.getThemeColor(),
                    placeholder = config.placeholder
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.LightGray.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = if (shouldForceExit) "对话已终止" else "对话已结束",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ApiKeyDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔑 设置 API Key",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "请输入你的 OpenAI API Key",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it.trim()
                        showError = false
                    },
                    label = { Text("API Key") },
                    placeholder = { Text("sk-...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError
                )

                if (showError) {
                    Text(
                        text = "API Key 不能为空",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "获取 API Key: https://platform.openai.com/api-keys",
                    fontSize = 12.sp,
                    color = Color.Blue,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmedKey = apiKey.trim()
                            if (trimmedKey.isBlank()) {
                                showError = true
                            } else {
                                onSave(trimmedKey)
                            }
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}