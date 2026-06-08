package com.example.matchmovie.network

import com.example.matchmovie.network.dto.ChatRequestDto
import com.example.matchmovie.network.dto.ChatResponseDto
import com.example.matchmovie.network.dto.GenreResponseDto
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
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


    // Funzione per l'invio del prompt al backend
    // E' di tipo POST, riceve come body un oggetto contenente il prompt dell'utente
    @POST("chat")
    suspend fun sendPrompt(
        @Body request: ChatRequestDto
    ): ChatResponseDto

    @GET("genres")
    suspend fun getGenres(): GenreResponseDto

}
