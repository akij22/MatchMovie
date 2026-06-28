package com.example.matchmovie.network

/**
 * Holder in-memory del JWT attivo.
 * Viene impostato al login e ripristinato all'avvio se esiste un utente loggato in locale.
 */
object AuthToken {
    @Volatile
    var token: String? = null
}
