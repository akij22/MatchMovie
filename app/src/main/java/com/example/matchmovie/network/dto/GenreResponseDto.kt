package com.example.matchmovie.network.dto

data class GenreResponseDto (
    var genre: List<SingleGenreDto>
)


data class SingleGenreDto (
    val id: Int,
    val genre: String
)
