package com.example.matchmovie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.components.ProfileStatCard
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.enumentity.MovieMood
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedButton
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    dao: FilmDAO
) {

    var savedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var movieMoods by remember { mutableStateOf<List<MovieMood>>(emptyList()) }
    var moviesByMood by remember { mutableStateOf<Map<MovieMood, List<UserMovie>>>(emptyMap()) }
    var topRatedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var recentlyAddedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String>("") }

    LaunchedEffect(user?._id) {
        val currentUser = user ?: return@LaunchedEffect

        try {
            val loadedMovies = withContext(Dispatchers.IO) {
                dao.getMoviesByUser(currentUser._id)
            }
            savedMovies = loadedMovies

            // Lista dei mood associati ai film salvati dall'utente
            movieMoods = loadedMovies.map { movie -> movie.mood }

            // Liste separate per mood
            moviesByMood = loadedMovies.groupBy { movie -> movie.mood }

            // Primi 5 film per rating decrescente
            topRatedMovies = loadedMovies
                .sortedWith(
                    compareByDescending<UserMovie> { movie -> movie.userRating }
                        .thenByDescending { movie -> movie._id }
                )
                .take(5)

            // Ultimi 5 film aggiunti: _id cresce con gli insert nel DB locale
            recentlyAddedMovies = loadedMovies
                .sortedByDescending { movie -> movie._id }
                .take(5)
        } catch (e: Exception) {
            savedMovies = emptyList()
            movieMoods = emptyList()
            moviesByMood = emptyMap()
            topRatedMovies = emptyList()
            recentlyAddedMovies = emptyList()
            errorMessage = "Unable to load local information, please try again"
        }
        

    }

    val averageRating = savedMovies
        .takeIf { movies -> movies.isNotEmpty() }
        ?.map { movie -> movie.userRating }
        ?.average()
        ?.let { rating -> String.format("%.1f/5", rating) }
        ?: "0.0/5"

    val favoriteMood = moviesByMood
        .maxByOrNull { entry -> entry.value.size }
        ?.key
        ?.name
        ?: "-"

    val highestRatedMovie = topRatedMovies.firstOrNull()?.title ?: "-"
    val firstAddedMovie = savedMovies.minByOrNull { movie -> movie._id }?.title ?: "-"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchMovieBackground),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profile",
                    color = MatchMoviePrimary,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MatchMoviePrimary,
                                    MatchMovieBackground
                                )
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.profileImage.isNullOrBlank()) {
                        AsyncImage(
                            model = user?.profileImage,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, MatchMovieBackground, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MatchMovieMutedButton)
                                .border(4.dp, MatchMovieBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = MatchMovieLightText,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = user?.name.orEmpty(),
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = user?.email.orEmpty(),
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodyMedium
                )

                user?.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = bio,
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    )
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Statistiche",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleLarge
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileStatCard(
                        icon = "★",
                        value = averageRating,
                        label = "Voto Medio",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        icon = "◈",
                        value = favoriteMood,
                        label = "Mood Preferito",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProfileStatCard(
                        icon = "▣",
                        value = highestRatedMovie,
                        label = "Rating piu alto",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        icon = "↺",
                        value = firstAddedMovie,
                        label = "Primo film aggiunto",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        errorMessage.takeIf { it.isNotBlank() }?.let { error ->
            item {
                Text(
                    text = error,
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatchMovieMutedButton,
                    contentColor = MatchMovieLightText
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
