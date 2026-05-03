package com.americanpizzabar.food.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClaudeRequest(
    val model: String,
    val messages: List<ClaudeMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 4096,
    val system: String? = null
)

data class ClaudeMessage(
    val role: String,
    val content: List<ClaudeContent>
)

sealed class ClaudeContent {
    data class Text(val type: String = "text", val text: String) : ClaudeContent()
    data class Image(
        val type: String = "image",
        val source: ImageSource
    ) : ClaudeContent()
}

data class ImageSource(
    val type: String,  // "base64" or "url"
    @SerializedName("media_type") val mediaType: String? = null,
    val data: String? = null,
    val url: String? = null
)
