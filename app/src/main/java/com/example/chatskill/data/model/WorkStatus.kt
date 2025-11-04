// 文件路径: app/src/main/java/com/example/chatskill/data/model/WorkStatus.kt
// 类型: enum class

package com.example.chatskill.data.model

enum class WorkStatus(
    val displayName: String,
    val description: String
) {
    STUDYING(
        displayName = "上学",
        description = "在校学生，专注学业"
    ),
    UNEMPLOYED(
        displayName = "失业",
        description = "待业中，寻找机会"
    ),
    EMPLOYED(
        displayName = "在职",
        description = "在职工作，有收入来源"
    );

    companion object {
        fun fromDisplayName(name: String): WorkStatus? {
            return values().find { it.displayName == name }
        }
    }
}