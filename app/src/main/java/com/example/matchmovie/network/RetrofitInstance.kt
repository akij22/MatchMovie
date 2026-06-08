package com.example.matchmovie.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {


    // Utilizzo di un timeout di OkHttpClient personalizzato, dato che la richiesta al
    // modello AI impiega più del temppo di timeout di default
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: TmdbMovieApi by lazy {
        Retrofit.Builder()

            // URL di base del server in Flask
            .baseUrl("http://10.0.2.2:5001/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbMovieApi::class.java)
    }
}
