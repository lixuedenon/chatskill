// 路径: app/src/main/java/com/example/chatskill/util/Extensions.kt
// 文件名: Extensions.kt
// 类型: 【创建】File
package com.example.chatskill.util

import java.text.SimpleDateFormat
import java.util.*

// 时间格式化扩展
fun Long.toTimeString(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

// 字符串扩展
fun String.isValidMessage(): Boolean {
    return this.trim().isNotEmpty() && this.length <= Constants.Message.MAX_INPUT_LENGTH
}