package com.example.matchmovie.enumentity

enum class Screen {
    LoginPage,

    // Contiene la barra di ricerca + la lista dei risultati ottenuti (sottoforma di lista)
    SearchScreen,

    // Contiene i dettagli di un film selezionato dall'utente, ottenuto dalla ricerca
    FilmDetailsScreen,
    MyFilmScreen,
}