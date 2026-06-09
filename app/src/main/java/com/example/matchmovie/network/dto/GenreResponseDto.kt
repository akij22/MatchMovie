package com.example.matchmovie.network.dto

data class GenreResponseDto (
    val genres: List<SingleGenreDto>
)


data class SingleGenreDto (
    val id: Int,
    val name: String
)
