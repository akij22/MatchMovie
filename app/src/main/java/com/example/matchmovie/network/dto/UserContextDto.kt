package com.example.matchmovie.network.dto

import com.example.matchmovie.enumentity.MovieMood

/**
 * Rappresenta il contesto dell'utente loggato da inviare al backend.
 * I dati sono letti dal database locale Room e permettono al modello AI
 * di personalizzare le risposte in base ai film salvati e alle statistiche.
 */
data class UserContextDto(
    val userId: Int,
    val name: String?,
    val totalSavedMovies: Int,
    val averageRating: Double,
    val favoriteMood: MovieMood?,
    val topRatedMovies: List<ContextMovieDto>,
    val recentlyAddedMovies: List<ContextMovieDto>,
    val allSavedMovies: List<ContextMovieDto>
) {
    data class ContextMovieDto(
        val title: String,
        val rating: Int,
        val mood: MovieMood
    )
}
