package com.example.matchmovie.network

import com.example.matchmovie.network.dto.ChatRequestDto
import com.example.matchmovie.network.dto.ChatResponseDto
import com.example.matchmovie.network.dto.AuthUserDto
import com.example.matchmovie.network.dto.GenreResponseDto
import com.example.matchmovie.network.dto.LoginRequestDto
import com.example.matchmovie.network.dto.LoginResponseDto
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieResponseDto
import com.example.matchmovie.network.dto.RegisterRequestDto
import com.example.matchmovie.network.dto.RegisterResponseDto
import com.example.matchmovie.network.dto.TvSeriesResponseDto
import com.example.matchmovie.network.dto.TvSeriesDetailsDto
import com.example.matchmovie.network.dto.TvSeasonDetailsDto
import com.example.matchmovie.network.dto.TrailerKeyDto
import com.example.matchmovie.network.dto.UpdateProfileRequestDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface TmdbMovieApi {

    @GET("movies/search")
    suspend fun searchMovies (
        @Query("query") query: String,
    ): MovieResponseDto

    @GET("movies/popular")
    suspend fun getPopularMovies (): MovieResponseDto

    @GET("movies/upcoming")
    suspend fun getUpcomingMovies(): MovieResponseDto


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

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequestDto
    ): RegisterResponseDto

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): LoginResponseDto

    @GET("auth/me")
    suspend fun getCurrentUser(): AuthUserDto

    @PUT("auth/me")
    suspend fun updateCurrentUser(
        @Body request: UpdateProfileRequestDto
    ): AuthUserDto


    @GET("movies/{movie_id}/recommendations")
    suspend fun getRecommendedMovies(
        @Path("movie_id") movieId: Int
    ): MovieResponseDto

    @GET("movies/{movie_id}/videos")
    suspend fun getMovieTrailer(
        @Path("movie_id") movieId: Int,
    ): TrailerKeyDto

    @GET("tv-series/search")
    suspend fun searchTvSeries(
        @Query("query") query: String,
    ): TvSeriesResponseDto

    @GET("tv-series/popular")
    suspend fun getPopularTvSeries(): TvSeriesResponseDto

    @GET("tv-series/top-rated")
    suspend fun getTopRatedTvSeries(): TvSeriesResponseDto

    @GET("tv-series/{series_id}/credits")
    suspend fun getTvSeriesCredits(
        @Path("series_id") seriesId: Int,
    ): MovieCreditsDto

    @GET("tv-series/{series_id}/details")
    suspend fun getTvSeriesDetails(
        @Path("series_id") seriesId: Int,
    ): TvSeriesDetailsDto

    @GET("tv-series/{series_id}/season/{season_number}")
    suspend fun getTvSeriesSeason(
        @Path("series_id") seriesId: Int,
        @Path("season_number") seasonNumber: Int,
    ): TvSeasonDetailsDto

    @GET("tv-series/{series_id}/videos")
    suspend fun getTvSeriesTrailer(
        @Path("series_id") seriesId: Int,
    ): TrailerKeyDto

    @GET("tv-series/genres")
    suspend fun getTvSeriesGenres(): GenreResponseDto

}
