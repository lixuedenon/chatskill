// 路径: app/src/main/java/com/example/chatskill/MainActivity.kt
// 文件名: MainActivity.kt
// 类型: Activity class
package com.example.chatskill

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.system.exitProcess

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen(
                    onMaleClick = {
                        startActivity(Intent(this, MaleActivity::class.java))
                    },
                    onFemaleClick = {
                        startActivity(Intent(this, FemaleActivity::class.java))
                    },
                    onExitClick = {
                        finish()
                        exitProcess(0)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    onMaleClick: () -> Unit,
    onFemaleClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "恋爱技能",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onMaleClick,
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            )
        ) {
            Text(text = "男生篇", fontSize = 18.sp)
        }

        Button(
            onClick = onFemaleClick,
            modifier = Modifier
                .width(200.dp)
                .height(60.dp)
                .padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE91E63)
            )
        ) {
            Text(text = "女生篇", fontSize = 18.sp)
        }

        Button(
            onClick = onExitClick,
            modifier = Modifier
                .width(200.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9E9E9E)
            )
        ) {
            Text(text = "退出", fontSize = 18.sp)
        }
    }
}