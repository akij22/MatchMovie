package com.example.matchmovie.network.dto


// Data class per mappare la risposta dell'endpoint `/tv/{series_id}` (dettagli della serie TV)
data class TvSeriesDetailsDto(
    val id: Int,
    val name: String? = null,
    val number_of_seasons: Int? = null,
    val number_of_episodes: Int? = null,
    val seasons: List<TvSeasonSummaryDto> = emptyList()
)

// Riepilogo di una singola stagione (senza la lista degli episodi)
data class TvSeasonSummaryDto(
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    val season_number: Int = 0,
    val episode_count: Int? = null,
    val poster_path: String? = null,
    val air_date: String? = null
) {
    val posterUrl: String?
        get() = poster_path?.let { "https://image.tmdb.org/t/p/w300$it" }
}

// Data class per mappare la risposta dell'endpoint `/tv/{series_id}/season/{season_number}`
// Contiene la lista completa degli episodi della stagione selezionata
data class TvSeasonDetailsDto(
    val id: Int? = null,
    val name: String? = null,
    val overview: String? = null,
    val season_number: Int = 0,
    val air_date: String? = null,
    val poster_path: String? = null,
    val episodes: List<TvEpisodeDto> = emptyList()
)

// Data class per mappare un singolo episodio di una stagione
data class TvEpisodeDto(
    val id: Int,
    val name: String? = null,
    val overview: String? = null,
    val episode_number: Int? = null,
    val season_number: Int? = null,
    val air_date: String? = null,
    val still_path: String? = null,
    val runtime: Int? = null,
    val vote_average: Double? = null
) {
    val stillUrl: String?
        get() = still_path?.let { "https://image.tmdb.org/t/p/w300$it" }
}
