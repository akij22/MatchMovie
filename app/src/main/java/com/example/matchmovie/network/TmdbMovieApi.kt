package com.example.matchmovie.network

import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbMovieApi {

    @GET("search/movie")
    suspend fun searchMovies (
        @Query("query") query: String,
    ): MovieResponseDto


    // API per il recupero del cast e del regista
    @GET("movie/{movie_id}/credits")

    suspend fun getMovieCredits(
        @Path("movie_id") movieId: Int,
    ): MovieCreditsDto
}
