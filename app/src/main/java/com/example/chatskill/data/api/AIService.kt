// 路径: app/src/main/java/com/example/chatskill/data/api/AIService.kt
package com.example.chatskill.data.api

import com.example.chatskill.data.model.AIRequest
import com.example.chatskill.data.model.AIResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AIService {
    @POST("chat/completions")
    suspend fun sendMessage(@Body request: AIRequest): Response<AIResponse>
}