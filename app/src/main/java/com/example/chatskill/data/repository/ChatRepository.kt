// 路径: app/src/main/java/com/example/chatskill/data/repository/ChatRepository.kt
// 文件名: ChatRepository.kt
// 类型: 【创建】class
package com.example.chatskill.data.repository

import com.example.chatskill.data.api.NetworkClient
import com.example.chatskill.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class ChatRepository {

    private val aiService = NetworkClient.aiService

    // 存储对话历史（用于发送给AI）
    private val conversationHistory = mutableListOf<AIMessage>()

    /**
     * 发送消息到AI（真实API版本）
     */
    suspend fun sendMessageToAI(
        message: String,
        systemPrompt: String?
    ): Flow<Message> = flow {
        try {
            // 添加用户消息到历史
            conversationHistory.add(AIMessage(role = "user", content = message))

            // 构建请求（包含系统提示词和历史对话）
            val request = AIRequest(
                messages = buildMessages(systemPrompt),
                max_tokens = 1024,
                temperature = 0.7
            )

            // 调用API
            val response = withContext(Dispatchers.IO) {
                aiService.sendMessage(request)
            }

            if (response.isSuccessful && response.body() != null) {
                val aiResponse = response.body()!!
                val aiText = aiResponse.content.firstOrNull()?.text ?: "抱歉，我没有回复。"

                // 添加AI回复到历史
                conversationHistory.add(AIMessage(role = "assistant", content = aiText))

                // 返回消息
                emit(Message(
                    content = aiText,
                    isUser = false,
                    status = MessageStatus.SENT
                ))
            } else {
                emit(Message(
                    content = "抱歉，服务暂时不可用，请稍后重试。",
                    isUser = false,
                    status = MessageStatus.FAILED
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emit(Message(
                content = "网络错误：${e.message}",
                isUser = false,
                status = MessageStatus.FAILED
            ))
        }
    }

    /**
     * AI对AI对话模式
     */
    suspend fun aiToAIConversation(
        ai1Prompt: String,
        ai2Prompt: String,
        initialMessage: String,
        rounds: Int = 5
    ): Flow<Message> = flow {
        var currentMessage = initialMessage
        var isAI1Turn = true

        repeat(rounds * 2) {
            val systemPrompt = if (isAI1Turn) ai1Prompt else ai2Prompt
            val aiName = if (isAI1Turn) "AI-1" else "AI-2"

            // 发送消息
            sendMessageToAI(currentMessage, systemPrompt).collect { message ->
                // 标记是哪个AI
                val markedMessage = message.copy(
                    content = "[$aiName]: ${message.content}"
                )
                emit(markedMessage)
                currentMessage = message.content
            }

            // 切换AI
            isAI1Turn = !isAI1Turn

            // 延迟一下，避免太快
            kotlinx.coroutines.delay(1000)
        }
    }

    /**
     * 构建消息列表（包含系统提示词）
     */
    private fun buildMessages(systemPrompt: String?): List<AIMessage> {
        val messages = mutableListOf<AIMessage>()

        // 如果有系统提示词，作为第一条消息
        if (!systemPrompt.isNullOrEmpty()) {
            messages.add(AIMessage(role = "user", content = systemPrompt))
            messages.add(AIMessage(role = "assistant", content = "明白了，我会按照你的要求进行对话。"))
        }

        // 添加对话历史
        messages.addAll(conversationHistory)

        return messages
    }

    /**
     * 清空对话历史
     */
    fun clearHistory() {
        conversationHistory.clear()
    }

    /**
     * 获取历史消息（本地存储，将来实现）
     */
    fun getHistoryMessages(chatType: String): List<Message> {
        return emptyList()
    }

    /**
     * 保存消息到本地（将来实现）
     */
    suspend fun saveMessage(message: Message) {
        // TODO: 保存到数据库
    }
}