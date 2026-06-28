package com.example.matchmovie.model

import com.example.matchmovie.enumentity.MessageSender
import com.example.matchmovie.network.dto.SingleMovieResultDto

data class ChatMessage (
    val message: String,
    val whoSent: MessageSender,
    val recommendedMovies: List<SingleMovieResultDto> = emptyList()
)
