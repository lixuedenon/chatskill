// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatViewModel.kt
package com.example.chatskill.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatskill.data.model.ChatConfig
import com.example.chatskill.data.model.Message
import com.example.chatskill.data.model.MessageStatus
import com.example.chatskill.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application.applicationContext)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private var chatConfig: ChatConfig? = null

    fun initialize(config: ChatConfig) {
        chatConfig = config
        loadHistoryMessages()
    }

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isLoading.value) return

        _inputText.value = ""

        val userMessage = Message(
            content = text,
            isUser = true,
            status = MessageStatus.SENT
        )
        _messages.value = _messages.value + userMessage

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.sendMessageToAI(text, chatConfig?.systemPrompt).collect { aiMessage ->
                    _messages.value = _messages.value + aiMessage
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                val errorMessage = Message(
                    content = "抱歉，发送失败了：${e.message}",
                    isUser = false,
                    status = MessageStatus.FAILED
                )
                _messages.value = _messages.value + errorMessage
                _isLoading.value = false
            }
        }
    }

    fun startAIToAIConversation() {
        if (_isLoading.value) return

        val initialMessage = "你好，我们开始聊天吧！"

        _isLoading.value = true
        viewModelScope.launch {
            try {
                repository.aiToAIConversation(
                    ai1Prompt = "你是一个热情开朗的人，喜欢聊天。",
                    ai2Prompt = "你是一个理性冷静的人，喜欢思考。",
                    initialMessage = initialMessage,
                    rounds = 5
                ).collect { message ->
                    _messages.value = _messages.value + message
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
            }
        }
    }

    private fun loadHistoryMessages() {
        viewModelScope.launch {
            chatConfig?.let { config ->
                val history = repository.getHistoryMessages(config.chatType.name)
                _messages.value = history
            }
        }
    }

    fun clearMessages() {
        _messages.value = emptyList()
        repository.clearHistory()
    }
}
