package com.americanpizzabar.food.data.remote

import com.americanpizzabar.food.data.remote.dto.ClaudeRequest
import com.americanpizzabar.food.data.remote.dto.ClaudeResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface ClaudeApiService {
    @POST("messages")
    suspend fun sendMessage(@Body request: ClaudeRequest): ClaudeResponse
}
