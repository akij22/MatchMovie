package com.example.matchmovie.network.dto



// Classe che riceve la risposta dall'API
data class MovieResponseDto (
    val page: Int,

    // Lista di film ottenuti dalla API response
    val results: List<SingleMovieResultDto>,
    val total_pages: Int,
    val total_results: Int
)