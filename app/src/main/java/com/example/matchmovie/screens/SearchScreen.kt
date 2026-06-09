package com.example.matchmovie.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.SingleMovieResultDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.matchmovie.components.MovieResultItem
import com.example.matchmovie.components.PopularMovieCard
import com.example.matchmovie.enumentity.MovieMood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SearchScreen(dao: FilmDAO, onMovieSelected: suspend (SingleMovieResultDto) -> Unit) {
    var movies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    // State per memorizzare i film più popolari mediante apposito endpoint
    var popularMovies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    var upComingMovies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingPopularMovies by remember { mutableStateOf(false) }
    var isLoadingUpcomingMovies by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var popularMoviesError by remember { mutableStateOf<String?>(null) }
    var upcomingMoviesError by remember { mutableStateOf<String?>(null) }
    var filmString by remember { mutableStateOf("") }
    var hasSubmittedSearch by remember { mutableStateOf(false) }

    // Mappa <ID Genre, Genre String>
    var genresById by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()

    val todayIso = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }


    // Dato un genere, lo mappo in un Mood specifico
    fun mapGenreToMood(genre: String): MovieMood {
        return when (genre) {
            "Comedy" -> MovieMood.FUNNY
            "Romance" -> MovieMood.ROMANTIC
            "Horror" -> MovieMood.SCARY
            "Thriller", "Crime", "Mystery", "War" -> MovieMood.DARK
            "Action", "Adventure" -> MovieMood.ACTION
            "Science Fiction", "Fantasy" -> MovieMood.MIND_BLOWING
            "Drama", "History", "Documentary" -> MovieMood.SAD
            "Animation", "Family", "Music" -> MovieMood.HAPPY
            else -> MovieMood.RELAXED
        }
    }


    // Data una lista di ID di generi APPARTENENTI AD UN FILM, ottengo il genere in formato stringa mediante la mappa `genresById`
    // Dopodichè, passo il genere in formato stringa alla funzione `mapGenreToMood` per ottenere il rispettivo Mood
    fun mapMovieGenresToMood(

        // Lista di ID di generi appartenenti al film
        FilmGenreIds: List<Int>,
        genreNamesById: Map<Int, String> = genresById
    ): MovieMood {
        return FilmGenreIds
            .mapNotNull { genreId -> genreNamesById[genreId] }

            // Dato il genere in formato stringa, applico il mood corrispondente
            .map { genre -> mapGenreToMood(genre) }

            // Prendo il primo mood trovato valido
            .firstOrNull()
            ?: MovieMood.NOT_SPECIFIED
    }

    // Data una lista di films e lo state `genresById`, assegno ad ogni film
    // il suo mood mediante la mappatura genere --> mood
    fun applyMoodToMovies(
        films: List<SingleMovieResultDto>,
        genreNamesById: Map<Int, String> = genresById
    ): List<SingleMovieResultDto> {
        return films.map { movie ->
            movie.copy(

                // Il mood del singolo film corrisponde alla mappatura Genre --> MovieMood
                mood = mapMovieGenresToMood(movie.genre_ids, genreNamesById)
            )
        }
    }

    // Recupero dei generi mediante API del backend
    suspend fun obtainGenres(): Map<Int, String> {
        return try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getGenres()
            }

            // Mappatura da genere a mood
            // Memorizzo i mood di un film all'interno di una map di MovieMood
            val loadedGenresById = response.genres.associate { genre ->
                genre.id to genre.name
            }
            genresById = loadedGenresById
            loadedGenresById
        } catch (e: Exception) {
            Log.e("SearchScreen", "Unable to load movie genres", e)
            emptyMap()
        }
    }

    suspend fun obtainFamousFilms(genreNamesById: Map<Int, String> = genresById) {
        isLoadingPopularMovies = true
        popularMoviesError = null

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getPopularMovies()
            }

            // Applico il mood ottenuto alla lista di film popolari della SearchScreen
            popularMovies = applyMoodToMovies(response.results, genreNamesById)

        } catch (e: Exception) {
            Log.e("SearchScreen", "Unable to load popular movies", e)
            popularMoviesError = "Unable to load popular movies"
        } finally {
            isLoadingPopularMovies = false
        }
    }


    // Funzione per il recupero dei film in uscita
    suspend fun obtainUpcomingFilms(genreNamesById: Map<Int, String> = genresById) {
        isLoadingUpcomingMovies = true
        upcomingMoviesError = null

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getUpcomingMovies()
            }

            // Applico il mood ottenuto alla lista di film in arrivo della SearchScreen
            upComingMovies = applyMoodToMovies(response.results, genreNamesById)

        } catch (e: Exception) {
            Log.e("SearchScreen", "Unable to load upcoming movies", e)
            upcomingMoviesError = "Unable to load upcoming movies"
        } finally {
            isLoadingUpcomingMovies = false
        }
    }
    

    // Funzione per ottenere i film ricercati mediante Query dell'utente
    suspend fun obtainSearchedFilms() {
        if (filmString.isBlank()) {
            movies = emptyList()
            refreshError = null
            return
        }

        isRefreshing = true
        refreshError = null

        try {
            val genreNamesById = genresById.ifEmpty {
                obtainGenres()
            }

            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.searchMovies(filmString)
            }

            // Assegno ai film ricercati il mood corrispondente
            movies = applyMoodToMovies(response.results, genreNamesById)
        } catch (e: Exception) {
            refreshError = e.localizedMessage ?: "Unable to refresh films"
        } finally {
            isRefreshing = false
        }
    }


    LaunchedEffect(Unit) {
        val genreNamesById = obtainGenres()
        obtainFamousFilms(genreNamesById)
        obtainUpcomingFilms(genreNamesById)
    }

    // Film in primo piano: prendo dalla lista dei film popolari quello con data di uscita più recente
    // già avvenuta. Se non trovo date valide, uso comunque il film con release_date più alta.

    // Assegno inizialmente a `featuredMovie` la lista di `popularMovies`
    val featuredMovie = popularMovies
        .filter { movie ->
            val releaseDate = movie.release_date
            !releaseDate.isNullOrBlank() && releaseDate <= todayIso
        }

        // Prendo la data massima (quindi quella piu recente)
        .maxByOrNull { movie -> movie.release_date.orEmpty() }

        // Fallback in caso non ottenga nulla dal filter precedente
        ?: popularMovies.maxByOrNull { movie -> movie.release_date.orEmpty() }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141C))
            .padding(top = 16.dp)
    ) {

        // Barra di ricerca
        OutlinedTextField(
            value = filmString,
            onValueChange = {
                filmString = it
                if (it.isBlank()) {
                    hasSubmittedSearch = false
                }
            },
            label = { Text("Movie title") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = !isRefreshing && filmString.isNotBlank(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .height(58.dp)
                .padding(start = 16.dp),
            onClick = {

                // Lancio la coroutine per ottenere i film dall'API
                // Imposto lo state di `hasSubmittedSearch` = true, in modo da rimuovere i film popolari
                // e mostrare la lista di quelli ottenuti mediante ricerca
                coroutineScope.launch {
                    obtainSearchedFilms()
                    hasSubmittedSearch = true
                }
            }
        ) {
            Text(
                text = if (isRefreshing) "Searching..." else "Search",
                fontWeight = FontWeight.Bold
            )
        }

        refreshError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 28.dp),
            modifier = Modifier.fillMaxSize()
        ) {

            // Finchè l'utente non ricerca un film, mostro quelli popolari
            if (!hasSubmittedSearch) {
                featuredMovie?.let { movie ->
                    item {
                        // Renderizzo il film in primo piano nella parte alta della SearchScreen
                        FeaturedMovieCard(
                            movie = movie,
                            onMovieSelected = onMovieSelected
                        )
                    }
                }

                item {
                    // Renderizzo la lista di films popolari ottenuti dall'API
                    MovieSection(
                        title = "Film Popolari",
                        movies = popularMovies,
                        onMovieSelected = onMovieSelected
                    )
                }

                if (isLoadingPopularMovies && popularMovies.isEmpty()) {
                    item {
                        Text(
                            text = "Loading popular movies...",
                            color = Color(0xFFE1BEBF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                popularMoviesError?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                item {
                    // Renderizzo la lista di films in arrivo ottenuti dall'API
                    UpcomingMovieSection(
                        title = "Prossimamente",
                        movies = upComingMovies,
                        onMovieSelected = onMovieSelected
                    )
                }

                if (isLoadingUpcomingMovies && upComingMovies.isEmpty()) {
                    item {
                        Text(
                            text = "Loading upcoming movies...",
                            color = Color(0xFFE1BEBF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                upcomingMoviesError?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            } else {
                // Renderizzo la lista di films ottenuti dall'API solo nel momento in cui l'utente invia la ricerca
                items(movies) { movie ->
                    MovieResultItem(
                        movie = movie,
                        onMovieSelected = onMovieSelected
                    )
                }
            }
        }
    }
}


// Composable per mostrare la Card principale riguardante il film in primo piano
@Composable
private fun FeaturedMovieCard(
    movie: SingleMovieResultDto,
    onMovieSelected: suspend (SingleMovieResultDto) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val backdropUrl = movie.backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" }
    val posterUrl = movie.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 5f)
            .padding(bottom = 28.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF182029))
            .clickable {
                coroutineScope.launch {
                    onMovieSelected(movie)
                }
            }
    ) {
        AsyncImage(
            model = backdropUrl ?: posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x66101820),
                            Color(0xFF0C141C)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "Ultima uscita",
                color = Color(0xFF70F8E8),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0x262EC4B6))
                    .border(
                        width = 1.dp,
                        color = Color(0x442EC4B6),
                        shape = RoundedCornerShape(50.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = movie.title,
                color = Color(0xFFF7F9FC),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = movie.overview?.takeIf { it.isNotBlank() } ?: "No description available.",
                color = Color(0xFFE1BEBF),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MovieSection(
    title: String,
    movies: List<SingleMovieResultDto>,
    onMovieSelected: suspend (SingleMovieResultDto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
    ) {
        SectionHeader(title = title)


        // Utilizzo di LazyRow per renderizzare la lista di film in riga (scrollabile)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(movies) { movie ->
                PopularMovieCard(
                    movie = movie,
                    onMovieSelected = onMovieSelected
                )
            }
        }
    }
}

@Composable
private fun UpcomingMovieSection(
    title: String,
    movies: List<SingleMovieResultDto>,
    onMovieSelected: suspend (SingleMovieResultDto) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        SectionHeader(title = title)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(end = 8.dp)
        ) {
            items(movies) { movie ->
                PopularMovieCard(
                    movie = movie,
                    onMovieSelected = onMovieSelected,
                    showReleaseDateBadge = true
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            color = Color(0xFFF7F9FC),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}
