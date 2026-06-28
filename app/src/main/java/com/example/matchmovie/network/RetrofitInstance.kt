package com.example.matchmovie.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {


    // Utilizzo di un timeout di OkHttpClient personalizzato, dato che la richiesta al
    // modello AI impiega più del temppo di timeout di default
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder().apply {
                AuthToken.token?.let { header("Authorization", "Bearer $it") }
            }.build()
            chain.proceed(request)
        }
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val api: TmdbMovieApi by lazy {
        Retrofit.Builder()

            // URL di base del server in Flask
            .baseUrl("http://10.0.2.2:5001/")
            .client(client)

            // Imposto come ConverterFactory Moshi
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(TmdbMovieApi::class.java)
    }
}
