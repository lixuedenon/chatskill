// 文件路径: app/src/main/java/com/example/chatskill/data/model/PersonalityType.kt | 类型: enum class

package com.example.chatskill.data.model

enum class PersonalityType(
    val displayName: String,
    val description: String,
    val keywords: List<String>
) {
    GENTLE(
        displayName = "温柔型",
        description = "说话轻声细语，善解人意，关心他人",
        keywords = listOf("温柔", "体贴", "细心")
    ),
    LIVELY(
        displayName = "活泼型",
        description = "开朗热情，爱笑爱闹，充满活力",
        keywords = listOf("活泼", "开朗", "热情")
    ),
    INTELLECTUAL(
        displayName = "知性型",
        description = "理性成熟，思维清晰，有深度",
        keywords = listOf("理性", "成熟", "知性")
    ),
    CUTE(
        displayName = "可爱型",
        description = "软萌可爱，略带撒娇，天真烂漫",
        keywords = listOf("可爱", "软萌", "天真")
    ),
    CONFIDENT(
        displayName = "自信型",
        description = "自信大方，直率坦诚，略带霸气",
        keywords = listOf("自信", "直率", "大方")
    );

    companion object {
        fun fromDisplayName(name: String): PersonalityType? {
            return values().find { it.displayName == name }
        }
    }
}