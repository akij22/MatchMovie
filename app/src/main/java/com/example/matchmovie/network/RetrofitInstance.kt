package com.example.matchmovie.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: TmdbMovieApi by lazy {
        Retrofit.Builder()

            // URL di base del server in Flask
            .baseUrl("http://10.0.2.2:5001/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbMovieApi::class.java)
    }
}
