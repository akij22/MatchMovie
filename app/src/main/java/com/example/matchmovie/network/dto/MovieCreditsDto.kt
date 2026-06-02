package com.example.matchmovie.network.dto


// Data class per mappare "cast" ottenuto dall'API
data class MovieCreditsDto(
    val id: Int,
    val cast: List<MovieCastMemberDto>,
    val crew: List<MovieCrewMemberDto>
)

data class MovieCastMemberDto(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?,
    val order: Int?
) {
    val role: String
        get() = character.orEmpty()

    val imageUrl: String?

        // Costruisco l'url di base + aggiungo quello specifico per
        // ottenere la foto del membro corrente
        // In questo modo "sovrascrivo" il metodo getter, in modo che restituisca sempre questo url base
        get() = profile_path?.let { "https://image.tmdb.org/t/p/w185$it" }
}

// Data class per mappare "crew" ottenuto dall'API
data class MovieCrewMemberDto(
    val id: Int,
    val name: String,
    val job: String?,
    val department: String?,
    val profile_path: String?,
) {
    val imageUrl: String?
        get() = profile_path?.let { "https://image.tmdb.org/t/p/w185$it" }

}
