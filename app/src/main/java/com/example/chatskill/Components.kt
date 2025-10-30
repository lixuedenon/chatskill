// 路径: app/src/main/java/com/example/chatskill/Components.kt
// 文件名: Components.kt
// 类型: File (包含 Composable 函数)
package com.example.chatskill

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModuleButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    isLast: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(bottom = if (isLast) 0.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        )
    ) {
        Text(text = text, fontSize = 18.sp)
    }
}