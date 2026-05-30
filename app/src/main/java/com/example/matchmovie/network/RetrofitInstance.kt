package com.example.matchmovie.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val bearer_token = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5MWY2MWQwZGVlYTNiNDllODQwNmIyZjNlOWUyYzM0NCIsIm5iZiI6MTc3OTg5OTQ1NC40NjEsInN1YiI6IjZhMTcxYzNlYTczN2Y1ZmVhZTI3Yjc0ZiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.naye9pdNi5uvTnoI6UIKYnAbS8BZD91nuAUizuvYiSI"


    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()

                // Autenticazione mediante Bearer Token
                .addHeader("Authorization", "Bearer $bearer_token")
                .addHeader("accept", "application/json")
                .build()

            chain.proceed(request)
        }
        .build()

    val api: TmdbMovieApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbMovieApi::class.java)
    }
}
