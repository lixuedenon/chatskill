package com.example.chatskill.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatskill.data.model.*
import com.example.chatskill.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatRepository(application.applicationContext)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _affinity = MutableStateFlow(50)
    val affinity: StateFlow<Int> = _affinity.asStateFlow()

    private val _warningCount = MutableStateFlow(0)
    val warningCount: StateFlow<Int> = _warningCount.asStateFlow()

    private val _conversationRound = MutableStateFlow(0)
    val conversationRound: StateFlow<Int> = _conversationRound.asStateFlow()

    private val _canShowReview = MutableStateFlow(false)
    val canShowReview: StateFlow<Boolean> = _canShowReview.asStateFlow()

    private val _isMaxRoundReached = MutableStateFlow(false)
    val isMaxRoundReached: StateFlow<Boolean> = _isMaxRoundReached.asStateFlow()

    private var chatConfig: ChatConfig? = null
    private var customCharacter: CustomCharacter? = null
    private val conversationId = UUID.randomUUID().toString()
    private val messageDetails = mutableListOf<MessageDetail>()

    fun initialize(config: ChatConfig, character: CustomCharacter? = null) {
        chatConfig = config
        customCharacter = character
        loadHistoryMessages()
    }

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty() || _isLoading.value || _isMaxRoundReached.value) return

        _inputText.value = ""

        val userMessage = Message(
            content = text,
            isUser = true,
            status = MessageStatus.SENT
        )
        _messages.value = _messages.value + userMessage

        _conversationRound.value += 1
        val currentRound = _conversationRound.value

        if (currentRound >= 5 && chatConfig?.chatType == ChatType.BASIC_CHAT && customCharacter != null) {
            _canShowReview.value = true
        }

        if (currentRound >= 50) {
            handleMaxRoundReached()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (customCharacter != null) {
                    repository.sendMessageWithAffinity(
                        message = text,
                        systemPrompt = chatConfig?.systemPrompt,
                        currentAffinity = _affinity.value,
                        warningCount = _warningCount.value,
                        conversationRound = currentRound,
                        characterName = customCharacter!!.name  // 修复：添加characterName参数
                    ).collect { structuredResponse ->
                        _affinity.value = structuredResponse.current_affinity

                        if (structuredResponse.affinity_change <= -15) {
                            _warningCount.value += 1
                        }

                        val aiMessage = Message(
                            content = structuredResponse.response,
                            isUser = false,
                            status = MessageStatus.SENT,
                            affinityChange = structuredResponse.affinity_change,
                            affinityReason = structuredResponse.affinity_reason,
                            currentAffinity = structuredResponse.current_affinity,
                            aiMood = structuredResponse.current_mood,
                            isTermination = !structuredResponse.should_continue
                        )
                        _messages.value = _messages.value + aiMessage

                        val messageDetail = MessageDetail(
                            round = currentRound,
                            userMessage = text,
                            aiResponse = structuredResponse.response,
                            affinityChange = structuredResponse.affinity_change,
                            affinityReason = structuredResponse.affinity_reason,
                            currentAffinity = structuredResponse.current_affinity,
                            aiMood = structuredResponse.current_mood,
                            timestamp = System.currentTimeMillis()
                        )
                        messageDetails.add(messageDetail)

                        if (!structuredResponse.should_continue || _warningCount.value >= 3) {
                            _isMaxRoundReached.value = true
                        }

                        _isLoading.value = false
                    }
                } else {
                    repository.sendMessageToAI(text, chatConfig?.systemPrompt).collect { aiMessage ->
                        _messages.value = _messages.value + aiMessage
                        _isLoading.value = false
                    }
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

    private fun handleMaxRoundReached() {
        val terminationMessage = Message(
            content = "今天聊得很开心呢~ 不过时间不早了，我们下次再聊吧！",
            isUser = false,
            status = MessageStatus.SENT,
            isTermination = true
        )
        _messages.value = _messages.value + terminationMessage
        _isMaxRoundReached.value = true
        _isLoading.value = false
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
        _affinity.value = 50
        _warningCount.value = 0
        _conversationRound.value = 0
        _canShowReview.value = false
        _isMaxRoundReached.value = false
        messageDetails.clear()
        repository.clearHistory()
    }

    fun getConversationRecord(): ConversationRecord? {
        val character = customCharacter ?: return null

        val affinityHistory = messageDetails.map { detail ->
            AffinityPoint(detail.round, detail.currentAffinity)
        }

        val positiveCount = messageDetails.count { it.affinityChange > 0 }
        val negativeCount = messageDetails.count { it.affinityChange < 0 }
        val averageAffinity = if (messageDetails.isNotEmpty()) {
            messageDetails.map { it.currentAffinity }.average()
        } else 50.0

        return ConversationRecord(
            conversationId = conversationId,
            character = character,
            startTime = messageDetails.firstOrNull()?.timestamp ?: System.currentTimeMillis(),
            endTime = System.currentTimeMillis(),
            totalRounds = _conversationRound.value,
            messages = messageDetails,
            affinityHistory = affinityHistory,
            finalAffinity = _affinity.value,
            averageAffinity = averageAffinity,
            warningCount = _warningCount.value,
            positiveCount = positiveCount,
            negativeCount = negativeCount
        )
    }
}