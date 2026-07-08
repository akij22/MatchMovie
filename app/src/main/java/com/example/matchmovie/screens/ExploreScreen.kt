package com.example.matchmovie.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedButton
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import com.example.matchmovie.ui.theme.MatchMovieSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.matchmovie.R
import com.example.matchmovie.components.InfoMessage
import com.example.matchmovie.components.LoadingScreen
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

    // Offset orizzontale della card durante lo swipe (in px): negativo = sinistra, positivo = destra
    val swipeOffset = remember { Animatable(0f) }
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(LocalDensity.current) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.22f

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
    val totalMovies = recommendedMovies.size
    val progress = if (totalMovies == 0) 0f else {
        (currentMovieIndex + 1).coerceAtMost(totalMovies).toFloat() / totalMovies
    }

    fun skipMovie() {
        coroutineScope.launch {
            swipeOffset.animateTo(
                targetValue = -screenWidthPx * 1.5f,
                animationSpec = tween(durationMillis = 360)
            )
            currentMovieIndex++
            swipeOffset.snapTo(0f)
        }
    }

    fun likeMovie(movie: SingleMovieResultDto) {
        coroutineScope.launch {
            swipeOffset.animateTo(
                targetValue = screenWidthPx * 1.5f,
                animationSpec = tween(durationMillis = 360)
            )
            pendingRating = 0
            pendingMovie = movie
        }
    }

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
                    swipeOffset.snapTo(0f)
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
                    swipeOffset.snapTo(0f)
                }
            },
            onDismiss = {
                pendingMovie = null
                pendingRating = 0
                coroutineScope.launch {
                    swipeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0E151C),
                        MatchMovieBackground,
                        Color(0xFF151B21)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ExploreHeader(
                currentIndex = currentMovieIndex,
                totalMovies = totalMovies,
                progress = progress
            )

            Spacer(modifier = Modifier.height(18.dp))

            when {
                errorMessage != null -> StatusMessage(
                    message = errorMessage.orEmpty(),
                    modifier = Modifier.weight(1f)
                )

                /* Se currentMovie == null, può essere che
                * - l'utente non abbia film salvati --> mostro popularMovies
                * - i recommendedMovies sono terminati --> mostro avviso del termine
                * */
                currentMovie == null && recommendedMovies.isEmpty() -> LoadingScreen(
                    message = "Finding movies for you...",
                    modifier = Modifier.weight(1f)
                )
                currentMovie == null -> Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    InfoMessage(
                        imageRes = R.drawable.end_list,
                        message = "No more movies to explore"
                    )
                }
                else -> {
                    BoxWithConstraints(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        val cardMaxWidth = if (maxWidth < maxHeight * 2f / 3f) {
                            maxWidth
                        } else {
                            maxHeight * 2f / 3f
                        }

                        Box(
                            modifier = Modifier
                                .width(cardMaxWidth)
                                .graphicsLayer {
                                    translationX = swipeOffset.value
                                    rotationZ =
                                        (swipeOffset.value / screenWidthPx.coerceAtLeast(1f)) * 14f
                                    transformOrigin = TransformOrigin(0.5f, 1f)
                                }
                                .pointerInput(currentMovie.id) {
                                    detectHorizontalDragGestures(
                                        onDragEnd = {
                                            when {
                                                swipeOffset.value <= -swipeThreshold -> skipMovie()
                                                swipeOffset.value >= swipeThreshold -> likeMovie(currentMovie)
                                                else -> coroutineScope.launch {
                                                    swipeOffset.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec = tween(durationMillis = 220)
                                                    )
                                                }
                                            }
                                        },
                                        onHorizontalDrag = { change, dragAmount ->
                                            change.consume()
                                            coroutineScope.launch {
                                                swipeOffset.snapTo(swipeOffset.value + dragAmount)
                                            }
                                        }
                                    )
                                }
                        ) {
                            ExploreMovieCard(
                                movie = currentMovie,
                                modifier = Modifier.fillMaxWidth()
                            )

                            ExploreCardActions(
                                canInteract = !swipeOffset.isRunning,
                                onSkip = { skipMovie() },
                                onLike = { likeMovie(currentMovie) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 18.dp)
                            )
                        }
                    }
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
private fun ExploreHeader(
    currentIndex: Int,
    totalMovies: Int,
    progress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Explore",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Swipe through hand-picked movie ideas",
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        text = if (totalMovies == 0) "..." else "${(currentIndex + 1).coerceAtMost(totalMovies)}/$totalMovies",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                border = BorderStroke(1.dp, Color(0x22FFFFFF))
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = MatchMoviePrimary,
            trackColor = MatchMovieSurface
        )
    }
}

@Composable
private fun ExploreCardActions(
    canInteract: Boolean,
    onSkip: () -> Unit,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(34.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExploreActionButton(
            symbol = "×",
            label = "Skip",
            backgroundColor = MatchMovieMutedButton,
            contentColor = MatchMovieMutedText,
            enabled = canInteract,
            onClick = onSkip
        )

        ExploreActionButton(
            symbol = "♥",
            label = "Save",
            backgroundColor = MatchMoviePrimary,
            contentColor = Color.White,
            enabled = canInteract,
            onClick = onLike
        )
    }
}

@Composable
private fun ExploreActionButton(
    symbol: String,
    label: String,
    backgroundColor: Color,
    contentColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = CircleShape,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = backgroundColor,
                contentColor = contentColor,
                disabledContainerColor = MatchMovieCard,
                disabledContentColor = MatchMovieMutedText
            ),
            modifier = Modifier.size(66.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = symbol,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = label,
            color = MatchMovieMutedText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
