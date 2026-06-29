package com.example.matchmovie.network.dto


data class ChatRequestDto (
    val messagePrompt: String,
    val userContext: UserContextDto? = null
)

