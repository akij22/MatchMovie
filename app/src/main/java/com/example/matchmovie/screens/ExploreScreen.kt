package com.example.matchmovie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.matchmovie.components.ExploreMovieCard
import com.example.matchmovie.components.RatingDialog
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.model.loadTopRatedUserMovies
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.SingleMovieResultDto
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieMutedButton
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.matchmovie.R
import com.example.matchmovie.components.InfoMessage
import com.example.matchmovie.components.StatusMessage

@Composable
fun ExploreScreen(
    dao: FilmDAO,
    currentUser: User
) {
    var recommendedMovies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    // Contatore "globale" al Composable per avanzare la Card principale (al click di X o cuore)
    var currentMovieIndex by remember { mutableIntStateOf(0) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Film in attesa di rating dopo il click sul cuore
    var pendingMovie by remember { mutableStateOf<SingleMovieResultDto?>(null) }
    // Rating temporaneo selezionato dall'utente nel dialog
    var pendingRating by remember { mutableIntStateOf(0) }



    LaunchedEffect(currentUser._id) {
        try {

            // Recupero dei film consigliati da mostrare nella schermata Explore
            recommendedMovies = loadRecommendedMovies(
                dao = dao,
                userId = currentUser._id
            // Mescolo la lista ad ogni caricamwento, in modo da renderla più variabile
            ).shuffled()


        } catch (e: Exception) {
            errorMessage = "Unable to load recommended movies, please try again."
        }
    }

    // film corrente da mostrare nella Card
    val currentMovie = recommendedMovies.getOrNull(currentMovieIndex)

    // Dialog di rating: mostrato quando l'utente clicca sul cuore per fargli assegnare il rating del rispettivo film
    pendingMovie?.let { movie ->
        RatingDialog(
            movieTitle = movie.title,
            rating = pendingRating,
            onRatingChange = { pendingRating = it },
            onConfirm = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.insert(
                            UserMovie(
                                userId = currentUser._id,
                                tmdbMovieId = movie.id,
                                title = movie.title,
                                description = movie.overview.orEmpty(),
                                image = movie.poster_path,
                                bio = "",
                                userRating = pendingRating.coerceAtLeast(0),
                                mood = movie.mood,
                                release_date = movie.release_date
                            )
                        )
                    }
                    pendingMovie = null
                    pendingRating = 0
                    currentMovieIndex++
                }
            },
            onSkip = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        dao.insert(
                            UserMovie(
                                userId = currentUser._id,
                                tmdbMovieId = movie.id,
                                title = movie.title,
                                description = movie.overview.orEmpty(),
                                image = movie.poster_path,
                                bio = "",
                                userRating = 0,
                                mood = movie.mood,
                                release_date = movie.release_date
                            )
                        )
                    }
                    pendingMovie = null
                    pendingRating = 0
                    currentMovieIndex++
                }
            },
            onDismiss = {
                pendingMovie = null
                pendingRating = 0
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchMovieBackground)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            errorMessage != null -> StatusMessage(errorMessage.orEmpty())

            /* Se currentMovie == null, può essere che
            * - l'utente non abbia film salvati --> mostro popularMovies
            * - i recommendedMovies sono terminati --> mostro avviso del termine
            * */
            currentMovie == null && recommendedMovies.isEmpty() -> StatusMessage("Loading movies...")
            currentMovie == null -> InfoMessage(
                imageRes = R.drawable.end_list,
                message = "No more movies to explore"
            )
            else -> {
                ExploreMovieCard(
                    movie = currentMovie,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExploreActionButton(
                        text = "×",
                        backgroundColor = MatchMovieMutedButton,
                        contentColor = MatchMovieMutedText,
                        modifier = Modifier.weight(1f),
                        onClick = { currentMovieIndex++ }
                    )

                    Spacer(modifier = Modifier.size(24.dp))

                    ExploreActionButton(
                        text = "♥",
                        backgroundColor = MatchMoviePrimary,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            // Apro il popup di rating invece di salvare subito
                            pendingRating = 0
                            pendingMovie = currentMovie
                        }
                    )
                }
            }
        }
    }
}


/*
* Funzione per:
* 1. il recupero dei film salvati dall'utente
* 2. Elaborazione per estrarre, mediante apposita API, possibili film da consigliare
* */
private suspend fun loadRecommendedMovies(
    dao: FilmDAO,
    userId: Int
): List<SingleMovieResultDto> {
    return withContext(Dispatchers.IO) {

        // Recupero dei film salvati dall'utente
        val savedMovies = dao.getMoviesByUser(userId)

        // Ottengo gli ids dei film salvati
        val savedMovieIds = savedMovies.map { movie -> movie.tmdbMovieId }.toSet()

        /*
        * 1. Ordino i film per rating in ordine decrescente
        * 2. Prendo i primi 5
        * 3. Ottengo i loro ids (con `.map`)
        * */
        val seedMovieIds = loadTopRatedUserMovies(
            dao = dao,
            userId = userId
        )
            .map { movie -> movie.tmdbMovieId }

        if (seedMovieIds.isEmpty()) {

            // Se l'utente non ha film memorizzati, uso come fallback i film popolari
            RetrofitInstance.api.getPopularMovies()
                .results
                .filterNot { movie -> movie.id in savedMovieIds }
        } else {

            // Per ogni ids all'interno di `seedMovieIds`, chiamo l'endpoint per
            // il recupero dei film consigliati e unisco tutte le liste risultanti in una sola
            seedMovieIds
                .flatMap { movieId ->
                    RetrofitInstance.api.getRecommendedMovies(movieId).results
                }
                .filterNot { movie -> movie.id in savedMovieIds }
                .distinctBy { movie -> movie.id }
        }
    }
}

@Composable
private fun ExploreActionButton(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        modifier = modifier.size(80.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

