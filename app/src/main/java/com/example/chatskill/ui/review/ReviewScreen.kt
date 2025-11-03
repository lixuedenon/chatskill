// 路径: app/src/main/java/com/example/chatskill/ui/review/ReviewScreen.kt
// 类型: composable

package com.example.chatskill.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.chatskill.data.model.ConversationRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    record: ConversationRecord,
    onBackClick: () -> Unit,
    onRestartSimilar: () -> Unit = {},
    onRestartNatural: () -> Unit = {},
    onChangeCharacter: () -> Unit = {},
    onReturnHome: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("对话复盘") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF673AB7),
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
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "对话基本信息",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider()
                        InfoRow("对话对象", record.character.name)
                        InfoRow("年龄", "${record.character.ageRange.displayName}")
                        InfoRow("性格", record.character.personality.displayName)
                        InfoRow("教育程度", record.character.education.displayName)
                        InfoRow("总轮数", "${record.totalRounds}轮")
                        InfoRow("对话时长", formatDuration(record.startTime, record.endTime))
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "好感度分析",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Divider()
                        InfoRow("初始好感度", "50分")
                        InfoRow("最终好感度", "${record.finalAffinity}分",
                            valueColor = getAffinityColor(record.finalAffinity))
                        InfoRow("平均好感度", String.format("%.1f分", record.averageAffinity))
                        InfoRow("加分次数", "${record.positiveCount}次",
                            valueColor = Color(0xFF4CAF50))
                        InfoRow("减分次数", "${record.negativeCount}次",
                            valueColor = Color(0xFFF44336))
                        InfoRow("警告次数", "${record.warningCount}次")
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "详细对话分析",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "功能开发中...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "未来将展示：\n• 好感度曲线图\n• 逐句对话分析\n• 高情商回复建议",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onRestartSimilar,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF673AB7)
                            )
                        ) {
                            Text(
                                text = "重新对话\n(相似回复)",
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Button(
                            onClick = onRestartNatural,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9C27B0)
                            )
                        ) {
                            Text(
                                text = "重新对话\n(自然回复)",
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onChangeCharacter,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("更换对象")
                        }
                        OutlinedButton(
                            onClick = onReturnHome,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("返回首页")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

private fun formatDuration(startTime: Long, endTime: Long): String {
    val durationMinutes = (endTime - startTime) / 1000 / 60
    return if (durationMinutes < 1) {
        "不到1分钟"
    } else {
        "${durationMinutes}分钟"
    }
}

private fun getAffinityColor(affinity: Int): Color {
    return when (affinity) {
        in 80..100 -> Color(0xFF4CAF50)
        in 60..79 -> Color(0xFF8BC34A)
        in 40..59 -> Color(0xFFFFC107)
        in 20..39 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}