package com.example.matchmovie.model

import com.example.matchmovie.enumentity.MessageSender

data class ChatMessage (
    val message: String,
    val whoSent: MessageSender
)