package com.example.matchmovie.network.dto

data class ChatResponseDto (
    val messageReply: String,

    // La response deve contenere anche una lista di film consigliati, formattati nell'apposito oggetto
    val recommendedMovies: List<SingleMovieResultDto> = emptyList()
)
