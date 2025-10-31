// 路径: app/src/main/java/com/example/chatskill/ui/chat/components/MessageList.kt
package com.example.chatskill.ui.chat.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatskill.data.model.Message

private const val TAG = "MessageList"

@Composable
fun MessageList(
    messages: List<Message>,
    themeColor: Color,
    isLoading: Boolean = false,
    imeHeight: Int = 0,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, imeHeight) {
        Log.d(TAG, "📝 消息数量: ${messages.size}, 键盘高度: ${imeHeight}px")

        if (messages.isNotEmpty()) {
            val lastIndex = messages.size - 1
            Log.d(TAG, "🎯 准备滚动到索引: $lastIndex")

            if (imeHeight > 0) {
                listState.scrollToItem(lastIndex)
                Log.d(TAG, "⚡ 键盘升起中 - 立即滚动（无动画）")
            } else {
                listState.animateScrollToItem(lastIndex)
                Log.d(TAG, "✅ 正常滚动（带动画）")
            }

            Log.d(TAG, "📊 当前可见范围: ${listState.firstVisibleItemIndex} - ${listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1}")
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (messages.isEmpty() && !isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "💬",
                    fontSize = 64.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "开始对话吧",
                    color = Color.Gray,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "输入你的第一条消息",
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    bottom = 16.dp
                )
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageItem(
                        message = message,
                        themeColor = themeColor
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = themeColor,
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}