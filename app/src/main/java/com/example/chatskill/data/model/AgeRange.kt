// 文件路径: app/src/main/java/com/example/chatskill/data/model/AgeRange.kt | 类型: enum class

package com.example.chatskill.data.model

enum class AgeRange(val displayName: String, val min: Int, val max: Int) {
    TEEN("18-22岁", 18, 22),
    YOUNG("22-26岁", 22, 26),
    MATURE("26-30岁", 26, 30),
    ESTABLISHED("30-35岁", 30, 35),
    SENIOR("35-40岁", 35, 40),
    MIDDLE_AGE("40-50岁", 40, 50),
    ELDER("50岁以上", 50, 99);

    fun getMiddleAge(): Int = (min + max) / 2

    companion object {
        fun fromDisplayName(name: String): AgeRange? {
            return values().find { it.displayName == name }
        }
    }
}