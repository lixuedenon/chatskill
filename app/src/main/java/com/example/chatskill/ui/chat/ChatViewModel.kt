// 路径: app/src/main/java/com/example/chatskill/ui/chat/ChatViewModel.kt
// 类型: class

package com.example.chatskill.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatskill.data.model.*
import com.example.chatskill.data.repository.ChatRepository
import com.example.chatskill.util.Constants
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

    private val _violationScore = MutableStateFlow(0.0f)
    val violationScore: StateFlow<Float> = _violationScore.asStateFlow()

    private val _shouldForceExit = MutableStateFlow(false)
    val shouldForceExit: StateFlow<Boolean> = _shouldForceExit.asStateFlow()

    private val _showToastWarning = MutableStateFlow<Int?>(null)
    val showToastWarning: StateFlow<Int?> = _showToastWarning.asStateFlow()

    private var chatConfig: ChatConfig? = null
    private var customCharacter: CustomCharacter? = null
    private var characterBackground: CharacterBackground? = null
    private var reviewMode: ReviewMode? = null
    private var previousRecord: ConversationRecord? = null
    private val conversationId = UUID.randomUUID().toString()
    private val messageDetails = mutableListOf<MessageDetail>()

    // 上下文记录（最近3轮）- 使用 ContextRecord
    private val recentContextList = mutableListOf<ContextRecord>()

    // 是否已发送初始提示词
    private var hasInitialPromptSent = false

    // 记录上一次违规类型
    private var lastViolationType: ViolationType = ViolationType.NONE
    private var sameTypeViolationCount = 0

    // 记录身份篡改次数
    private var identityTamperingCount = 0

    // 记录上次违规的轮数
    private var lastViolationRound = 0

    // 未来5轮指令的起始轮数
    private var nextInstructionStartRound = 0

    fun initialize(
        config: ChatConfig,
        character: CustomCharacter? = null,
        background: CharacterBackground? = null,
        mode: ReviewMode? = null,
        record: ConversationRecord? = null
    ) {
        chatConfig = config
        customCharacter = character
        characterBackground = background
        reviewMode = mode
        previousRecord = record

        if (mode != null && record != null) {
            repository.loadPreviousConversation(record)
        }

        loadHistoryMessages()
    }

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun clearToastWarning() {
        _showToastWarning.value = null
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

        if (currentRound >= Constants.Conversation.REVIEW_THRESHOLD &&
            chatConfig?.chatType == ChatType.BASIC_CHAT &&
            customCharacter != null) {
            _canShowReview.value = true
        }

        if (currentRound >= Constants.Conversation.MAX_ROUNDS) {
            handleMaxRoundReached()
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                if (customCharacter != null && characterBackground != null) {
                    // 构建初始提示词（只在第1轮发送）
                    val initialPrompt = if (!hasInitialPromptSent) {
                        hasInitialPromptSent = true
                        customCharacter!!.getInitialPrompt(characterBackground!!)
                    } else {
                        null
                    }

                    // 构建动态提示词（每轮发送）
                    val dynamicPrompt = buildDynamicPrompt(currentRound)

                    repository.sendMessageWithAffinity(
                        message = text,
                        initialPrompt = initialPrompt,
                        dynamicPrompt = dynamicPrompt,
                        currentAffinity = _affinity.value,
                        warningCount = _warningCount.value,
                        isFirstMessage = currentRound == 1
                    ).collect { structuredResponse ->
                        _affinity.value = structuredResponse.current_affinity

                        val violationType = parseViolationType(structuredResponse.violation_type)

                        // 处理违规检测
                        if (structuredResponse.violation_detected && violationType != ViolationType.NONE) {
                            handleViolation(violationType, currentRound)

                            if (_violationScore.value >= 3.0f) {
                                handleViolationForceExit(isIdentityTampering = violationType == ViolationType.IDENTITY_TAMPERING)
                                return@collect
                            }
                        } else {
                            // 无违规时，检查是否可以"原谅"
                            if (currentRound - lastViolationRound > 10 && _violationScore.value > 0) {
                                _violationScore.value = (_violationScore.value - 0.5f).coerceAtLeast(0f)
                            }
                        }

                        if (structuredResponse.affinity_change <= -15 && _violationScore.value == 0f) {
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
                            isTermination = !structuredResponse.should_continue,
                            violationType = violationType
                        )
                        _messages.value = _messages.value + aiMessage

                        // 记录上下文（最近3轮）- 使用 ContextRecord
                        val topic = extractTopic(text, structuredResponse.response)
                        val context = ContextRecord(
                            round = currentRound,
                            topic = topic,
                            mood = structuredResponse.current_mood,
                            violationType = violationType
                        )
                        recentContextList.add(context)

                        // 只保留最近3轮
                        if (recentContextList.size > 3) {
                            recentContextList.removeAt(0)
                        }

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

                        if (!structuredResponse.should_continue) {
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

    // 构建动态提示词
    private fun buildDynamicPrompt(currentRound: Int): String {
        val character = customCharacter ?: return ""

        // 生成未来5轮指令（每5轮更新一次）
        val nextRoundsInstruction = if (currentRound >= nextInstructionStartRound) {
            nextInstructionStartRound = currentRound + 5
            buildNextRoundsInstruction(currentRound)
        } else {
            ""
        }

        return character.getDynamicPrompt(
            currentRound = currentRound,
            currentAffinity = _affinity.value,
            currentMood = getMoodDescription(),
            recentContext = recentContextList.toList(),
            nextRoundsInstruction = nextRoundsInstruction
        )
    }

    // 生成未来5轮指令
    private fun buildNextRoundsInstruction(currentRound: Int): String {
        val endRound = currentRound + 4
        val builder = StringBuilder()

        builder.append("# 接下来5轮（第${currentRound}-${endRound}轮）\n")

        // 根据当前状态生成指令
        when {
            // 刚生气
            _affinity.value < 40 && recentContextList.any { it.violationType != ViolationType.NONE } -> {
                builder.append("- 你现在生气，至少3轮才能消气\n")
                builder.append("- 回复要短，带着不爽的语气\n")
                builder.append("- 如果对方再违规 -> 更生气\n")
                builder.append("- 如果对方道歉 -> 可以稍微缓和，但还是不太开心\n")
            }

            // 话题刚跳转
            recentContextList.lastOrNull()?.violationType == ViolationType.ABRUPT_TOPIC_CHANGE -> {
                builder.append("- 话题刚跳转，保持困惑至少2轮\n")
                builder.append("- 不要立刻接受新话题\n")
                builder.append("- 可以问：我们不是在聊XX吗\n")
            }

            // 正常状态
            else -> {
                builder.append("- 保持自然聊天\n")
                builder.append("- 记住：每次回复都要不一样\n")
                builder.append("- 1行，最多30字\n")
            }
        }

        return builder.toString()
    }

    // 获取情绪描述
    private fun getMoodDescription(): String {
        return when {
            _violationScore.value >= 2.0f -> "很生气"
            _violationScore.value >= 1.0f -> "有点生气"
            _affinity.value >= 70 -> "开心"
            _affinity.value >= 40 -> "正常"
            _affinity.value >= 20 -> "不太开心"
            else -> "很不爽"
        }
    }

    // 提取话题
    private fun extractTopic(userMessage: String, aiResponse: String): String {
        return when {
            userMessage.contains("工作") || aiResponse.contains("工作") -> "工作"
            userMessage.contains("爱好") || aiResponse.contains("爱好") -> "爱好"
            userMessage.contains("喜欢") || aiResponse.contains("喜欢") -> "兴趣"
            userMessage.length < 10 -> "闲聊"
            else -> "日常对话"
        }
    }

    // 处理违规行为
    private fun handleViolation(violationType: ViolationType, currentRound: Int) {
        val baseScore = when (violationType) {
            ViolationType.IDENTITY_TAMPERING -> 1.0f
            ViolationType.PROFANITY -> 1.0f
            ViolationType.KNOWLEDGE_BOUNDARY -> 0.5f
            ViolationType.ABRUPT_TOPIC_CHANGE -> 0.5f
            ViolationType.BORING_CONVERSATION -> 0.3f
            else -> 0f
        }

        val scoreToAdd = if (violationType == lastViolationType) {
            sameTypeViolationCount++
            when (sameTypeViolationCount) {
                1 -> baseScore * 1.5f
                else -> baseScore * 2.0f
            }
        } else {
            sameTypeViolationCount = 0
            baseScore
        }

        _violationScore.value += scoreToAdd
        lastViolationType = violationType
        lastViolationRound = currentRound

        if (violationType == ViolationType.IDENTITY_TAMPERING) {
            identityTamperingCount++
        }

        val warningLevel = when {
            _violationScore.value >= 3.0f -> 3
            _violationScore.value >= 2.0f -> 2
            _violationScore.value >= 1.0f -> 1
            else -> 0
        }

        if (warningLevel > 0) {
            _showToastWarning.value = warningLevel
        }
    }

    private fun parseViolationType(typeString: String): ViolationType {
        return when (typeString.lowercase()) {
            "identity_tampering" -> ViolationType.IDENTITY_TAMPERING
            "profanity" -> ViolationType.PROFANITY
            "knowledge_boundary" -> ViolationType.KNOWLEDGE_BOUNDARY
            "abrupt_topic_change" -> ViolationType.ABRUPT_TOPIC_CHANGE
            "boring_conversation" -> ViolationType.BORING_CONVERSATION
            else -> ViolationType.NONE
        }
    }

    private fun handleViolationForceExit(isIdentityTampering: Boolean = false) {
        val terminationContent = if (isIdentityTampering && identityTamperingCount >= 3) {
            "对方已拉黑你并退出对话"
        } else {
            "对方已经不想再聊了..."
        }

        val terminationMessage = Message(
            content = terminationContent,
            isUser = false,
            status = MessageStatus.SENT,
            isTermination = true
        )
        _messages.value = _messages.value + terminationMessage
        _isMaxRoundReached.value = true
        _isLoading.value = false
        _shouldForceExit.value = true
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
        _violationScore.value = 0.0f
        _shouldForceExit.value = false
        lastViolationType = ViolationType.NONE
        sameTypeViolationCount = 0
        identityTamperingCount = 0
        lastViolationRound = 0
        nextInstructionStartRound = 0
        hasInitialPromptSent = false
        recentContextList.clear()
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