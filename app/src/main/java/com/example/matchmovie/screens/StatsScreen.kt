package com.example.matchmovie.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.matchmovie.components.LoadingScreen
import com.example.matchmovie.components.MovieDaoItem
import com.example.matchmovie.components.ProfileStatCard
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.enumentity.MovieMood
import com.example.matchmovie.ui.theme.MatchMovieAccent
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedButton
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import com.example.matchmovie.ui.theme.MatchMovieSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Schermata di report in stile "Spotify Wrapped" con le statistiche dell'utente.
 * Condivide la palette, gli header e i componenti (ProfileStatCard / MovieDaoItem)
 * con ProfileScreen per garantire coerenza visiva tra le due schermate.
 */
@Composable
fun StatsScreen(
    user: User?,
    dao: FilmDAO,
    onBackClick: () -> Unit
) {


    BackHandler {
        onBackClick()
    }

    var savedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var moviesByMood by remember { mutableStateOf<Map<MovieMood, List<UserMovie>>>(emptyMap()) }
    var topRatedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var recentlyAddedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String>("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(user?._id) {
        val currentUser = user ?: return@LaunchedEffect

        isLoading = true
        try {

            val loadedMovies = withContext(Dispatchers.IO) {
                dao.getMoviesByUser(currentUser._id)
            }


            savedMovies = loadedMovies
            moviesByMood = loadedMovies
                .filter { it.mood != MovieMood.NOT_SPECIFIED }
                .groupBy { it.mood }
            topRatedMovies = loadedMovies

                .sortedWith(
                    compareByDescending<UserMovie> { it.userRating }
                        .thenByDescending { it._id }
                )
                .take(5)
            recentlyAddedMovies = loadedMovies
                .sortedByDescending { it._id }
                .take(5)

        } catch (e: Exception) {
            savedMovies = emptyList()
            moviesByMood = emptyMap()
            topRatedMovies = emptyList()
            recentlyAddedMovies = emptyList()
            errorMessage = "Unable to load local information, please try again"
        } finally {
            isLoading = false
        }
    }

    val totalMovies = savedMovies.count()
    val averageRating = savedMovies
        .takeIf { it.isNotEmpty() }
        ?.map { it.userRating }
        ?.average()
        ?.let { String.format("%.1f", it) }
        ?: "0.0"

    val ratingCounts = remember(savedMovies) {
        IntArray(6).also { counts ->
            savedMovies.forEach { counts[it.userRating.coerceIn(0, 5)]++ }
        }
    }

    val moodEntries = remember(moviesByMood, totalMovies) {
        moviesByMood.entries
            .sortedByDescending { it.value.size }
            .map { entry -> entry.key to entry.value.size }
    }

    val favoriteMood = moodEntries.firstOrNull()?.first?.prettyName() ?: "Not Specified"
    val favoriteMoodCount = moodEntries.firstOrNull()?.second ?: 0

    val highestRatedMovie = topRatedMovies.firstOrNull()?.title ?: "-"
    val firstAddedMovie = savedMovies.minByOrNull { it._id }?.title ?: "-"
    val latestAddedMovie = recentlyAddedMovies.firstOrNull()?.title ?: "-"

    val wrappedYear = remember { Calendar.getInstance().get(Calendar.YEAR).toString() }

    val hasData = savedMovies.isNotEmpty()

    if (isLoading) {
        LoadingScreen(
            message = "Loading stats...",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
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
                    text = "Your Wrapped",
                    color = MatchMoviePrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$wrappedYear · MatchMovie",
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            WrappedHero(
                userName = user?.name.orEmpty(),
                totalMovies = totalMovies,
                averageRating = averageRating,
                favoriteMood = favoriteMood,
                hasData = hasData
            )
        }

        if (hasData) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileStatCard(
                            icon = "★",
                            value = "$averageRating/5",
                            label = "AVG Rating",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            icon = "◈",
                            value = favoriteMood,
                            label = "Favourite Mood",
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
                            label = "Highest Rating",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            icon = "↺",
                            value = firstAddedMovie,
                            label = "First Film Added",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileStatCard(
                            icon = "☰",
                            value = totalMovies.toString(),
                            label = "Movies Saved",
                            modifier = Modifier.weight(1f)
                        )
                        ProfileStatCard(
                            icon = "↷",
                            value = latestAddedMovie,
                            label = "Latest Film Added",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (moodEntries.isNotEmpty()) {
            item {
                WrappedSectionCard(title = "Mood distribution") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        moodEntries.forEach { (mood, count) ->
                            MoodBar(
                                moodName = mood.prettyName(),
                                count = count,
                                total = totalMovies,
                                color = moodColor(mood)
                            )
                        }
                    }
                }
            }
        }

        if (hasData) {
            item {
                WrappedSectionCard(title = "Rating distribution") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (stars in 5 downTo 1) {
                            RatingBar(
                                stars = stars,
                                count = ratingCounts[stars],
                                total = totalMovies
                            )
                        }
                    }
                }
            }
        }

        if (topRatedMovies.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Top ${topRatedMovies.size} Films",
                        color = MatchMovieLightText,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    topRatedMovies.take(3).chunked(2).forEach { rowMovies ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowMovies.forEach { movie ->
                                MovieDaoItem(
                                    movie = movie,
                                    onMovieClick = {},
                                    onDeleteClick = {},
                                    compact = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowMovies.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
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
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatchMovieMutedButton,
                    contentColor = MatchMovieLightText
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Profile")
            }
        }
    }
}
}

@Composable
private fun WrappedHero(
    userName: String,
    totalMovies: Int,
    averageRating: String,
    favoriteMood: String,
    hasData: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MatchMoviePrimary,
                        MatchMovieSecondary,
                        MatchMovieBackground
                    )
                )
            )
            .padding(28.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = userName.ifBlank { "Your" }.uppercase(),
                color = MatchMovieLightText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Movie Wrapped",
                color = MatchMovieLightText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (hasData) {
                Text(
                    text = totalMovies.toString(),
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "films in your collection",
                    color = MatchMovieLightText.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeroStat(
                        label = "AVG rating",
                        value = "$averageRating/5",
                        modifier = Modifier.weight(1f)
                    )
                    HeroStat(
                        label = "top mood",
                        value = favoriteMood,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = "No films saved yet.\nAdd some to unwrap your year.",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .padding(12.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = MatchMovieLightText.copy(alpha = 0.75f),
            style = MaterialTheme.typography.labelSmall,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = MatchMovieLightText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun WrappedSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MatchMovieCard)
            .border(1.dp, MatchMovieMutedText.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = title,
            color = MatchMovieLightText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        content()
    }
}

@Composable
private fun MoodBar(
    moodName: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) count.toFloat() / total else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = moodName,
                color = MatchMovieLightText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$count · ${String.format("%.0f", percentage * 100)}%",
                color = MatchMovieMutedText,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MatchMovieMutedButton)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun RatingBar(
    stars: Int,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) count.toFloat() / total else 0f
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$stars★",
            color = MatchMovieSecondary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MatchMovieMutedButton)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MatchMovieSecondary)
            )
        }
        Text(
            text = count.toString(),
            color = MatchMovieMutedText,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}

private fun MovieMood.prettyName(): String =
    name.split('_').joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercase() }
    }

private fun moodColor(mood: MovieMood): Color = when (mood) {
    MovieMood.HAPPY        -> MatchMovieSecondary
    MovieMood.FUNNY        -> Color(0xFFFFD23F)
    MovieMood.SAD          -> Color(0xFF5B7CFA)
    MovieMood.ROMANTIC     -> MatchMoviePrimary
    MovieMood.COZY         -> Color(0xFFE8915A)
    MovieMood.DARK         -> Color(0xFF5A4A6E)
    MovieMood.MIND_BLOWING -> Color(0xFF8A5BF5)
    MovieMood.SCARY        -> Color(0xFF6E1423)
    MovieMood.ACTION       -> Color(0xFFFF5A3C)
    MovieMood.RELAXED      -> MatchMovieAccent
    MovieMood.NOT_SPECIFIED -> MatchMovieMutedText
}