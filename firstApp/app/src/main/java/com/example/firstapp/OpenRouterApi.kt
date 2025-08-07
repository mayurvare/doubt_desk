package com.example.firstapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    fun getAIAnswer(
        @Header("Authorization") apiKey: String,
        @Header("HTTP-Referer") referer: String = "https://yourapp.com",
        @Header("X-Title") title: String = "AI Assistant",
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: OpenRouterRequest
    ): Call<OpenRouterResponse>
}