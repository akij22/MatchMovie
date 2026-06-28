package com.example.matchmovie.model

import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.enumentity.MovieMood
import com.example.matchmovie.network.dto.SingleMovieResultDto

data class MovieDetailsUi(
    val id: Int,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val originalLanguage: String?,
    val mood: MovieMood,
    val userRating: Int? = null
)


// Funzione per trasformare un `SingleMovieResultsDto` in un oggetto di tipo `MovieDetailsUI`
fun SingleMovieResultDto.toMovieDetailsUi(): MovieDetailsUi {
    return MovieDetailsUi(
        id = id,
        title = title,
        overview = overview,
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average,
        originalLanguage = original_language,
        mood = mood
    )
}


// Funzione per trasformare un `UserMovie` in un oggetto di tipo `MovieDetailsUI`
// Ciò viene fatto per garantire che esso sia visualizzabile all'interno di `FilmDetailsScreen`, insieme
// a `SingleMovieResponseDto` ed evitando di creare 2 schermate differenti
fun UserMovie.toMovieDetailsUi(): MovieDetailsUi {
    return MovieDetailsUi(
        id = tmdbMovieId,
        title = title,
        overview = description,
        posterPath = image,
        backdropPath = null,
        releaseDate = release_date,
        voteAverage = null,
        originalLanguage = null,
        mood = mood,
        userRating = userRating
    )
}
