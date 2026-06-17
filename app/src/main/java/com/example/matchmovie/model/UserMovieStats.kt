package com.example.matchmovie.model

import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.UserMovie


// Funzione per il recupero dei top <limit> film per rating (descrescente)
suspend fun loadTopRatedUserMovies(
    dao: FilmDAO,
    userId: Int,
    limit: Int = 5
): List<UserMovie> {
    return dao.getMoviesByUser(userId)
        .sortedWith(
            compareByDescending<UserMovie> { movie -> movie.userRating }
                .thenByDescending { movie -> movie._id }
        )
        .take(limit)
}
