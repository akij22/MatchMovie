package com.example.matchmovie.network

import com.example.matchmovie.network.dto.MovieResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbMovieApi {

    @GET("search/movie")
    suspend fun searchMovies (
        @Query("query") query: String,
    ): MovieResponseDto


}
