package com.example.matchmovie.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: TmdbMovieApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbMovieApi::class.java)
    }
}
