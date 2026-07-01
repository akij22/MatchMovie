package com.example.matchmovie.network.dto

import com.example.matchmovie.enumentity.MovieMood

data class TvSeriesResponseDto(
    val page: Int,
    val results: List<SingleTvSeriesResultDto>,
    val total_pages: Int,
    val total_results: Int
)

data class SingleTvSeriesResultDto(
    val id: Int,
    val name: String,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val genre_ids: List<Int> = emptyList(),
    val vote_average: Double,
    val original_language: String,
    val popularity: Double,
    val mood: MovieMood = MovieMood.NOT_SPECIFIED
)

fun SingleTvSeriesResultDto.toMovieCardDto(): SingleMovieResultDto {
    return SingleMovieResultDto(
        id = id,
        title = name,
        overview = overview,
        poster_path = poster_path,
        backdrop_path = backdrop_path,
        release_date = first_air_date,
        genre_ids = genre_ids,
        vote_average = vote_average,
        original_language = original_language,
        popularity = popularity,
        mood = mood
    )
}
