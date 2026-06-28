package com.example.matchmovie.enumentity

enum class Screen {
    LoginPage,

    ChatScreen,

    ProfileScreen,

    MyListScreen,

    // Contiene la barra di ricerca + la lista dei risultati ottenuti (sottoforma di lista)
    HomeScreen,

    ExploreScreen,

    // Contiene i dettagli di un film selezionato dall'utente, ottenuto dalla ricerca
    FilmDetailsScreen,

    // Report in stile "Spotify Wrapped" con le statistiche dell'utente
    StatsScreen,
}
