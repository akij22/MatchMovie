package com.example.matchmovie.network.dto

import com.example.matchmovie.enumentity.MovieMood

data class SingleMovieResultDto (
    val id: Int,
    val title: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val genre_ids: List<Int>,
    val vote_average: Double,
    val original_language: String,
    val popularity: Double,

    // Assegno un mood di default
    val mood: MovieMood = MovieMood.NOT_SPECIFIED
)
