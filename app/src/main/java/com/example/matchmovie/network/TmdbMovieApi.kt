package com.example.matchmovie.network

import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbMovieApi {

    @GET("movies/search")
    suspend fun searchMovies (
        @Query("query") query: String,
    ): MovieResponseDto

    @GET("movies/popular")
    suspend fun getPopularMovies (): MovieResponseDto


    // API per il recupero del cast e del regista
    @GET("movies/{movie_id}/credits")

    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
    ): MovieCreditsDto
}
