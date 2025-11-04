// 文件路径: app/src/main/java/com/example/chatskill/data/model/EducationLevel.kt
// 类型: enum class

package com.example.chatskill.data.model

enum class EducationLevel(
    val displayName: String,
    val knowledgeLevel: String,
    val speakingStyle: String
) {
    MIDDLE_SCHOOL(
        displayName = "初中",
        knowledgeLevel = "基础知识，课本为主",
        speakingStyle = "简单直接，口语化强，网络用语多"
    ),
    HIGH_SCHOOL(
        displayName = "高中",
        knowledgeLevel = "高中知识体系，开始有深度思考",
        speakingStyle = "口语为主，偶尔书面语，流行文化敏感"
    ),
    UNDERGRADUATE(
        displayName = "大学",
        knowledgeLevel = "专业知识，社会认知提升",
        speakingStyle = "灵活切换口语书面语，逻辑性增强"
    ),
    GRADUATE(
        displayName = "研究生",
        knowledgeLevel = "深度专业知识，独立思考能力，学术时间占比大",
        speakingStyle = "书面语较多，表达精准成熟，对流行文化可能了解有限"
    ),
    DOCTORATE(
        displayName = "博士（后）",
        knowledgeLevel = "顶尖专业知识，深度学术研究，生活重心在学术",
        speakingStyle = "学术化表达，专业领域精通，生活技能和流行文化可能欠缺"
    );

    companion object {
        fun fromDisplayName(name: String): EducationLevel? {
            return values().find { it.displayName == name }
        }

        // 判断该学历是否可以选择"上学"状态
        fun canBeStudying(education: EducationLevel): Boolean {
            return education == UNDERGRADUATE ||
                   education == GRADUATE ||
                   education == DOCTORATE
        }
    }
}