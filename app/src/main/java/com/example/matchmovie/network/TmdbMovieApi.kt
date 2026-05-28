package com.example.matchmovie.network

import retrofit2.http.GET

interface TmdbMovieApi {

    @GET("prices")

    // Viene restituita una Map, che associa al nome del Coin il suo prezzo attuale
    // Per ricercare uno specifico coin, posso eseguire prices[coin.name] ?: 0.0
    suspend fun getPrices(): Map<String, Double>

}
