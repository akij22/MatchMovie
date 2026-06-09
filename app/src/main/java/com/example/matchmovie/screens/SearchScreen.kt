package com.example.matchmovie.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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

@Composable
fun SearchScreen(dao: FilmDAO, onMovieSelected: suspend (SingleMovieResultDto) -> Unit) {
    var movies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    // State per memorizzare i film più popolari mediante apposito endpoint
    var popularMovies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingPopularMovies by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var popularMoviesError by remember { mutableStateOf<String?>(null) }
    var filmString by remember { mutableStateOf("") }
    var hasSubmittedSearch by remember { mutableStateOf(false) }

    // Mappa <ID Genre, Genre String>
    var genresById by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    val coroutineScope = rememberCoroutineScope()


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
    }


    Column (
        modifier = Modifier.padding(top = 16.dp)
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
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {

            // Finchè l'utente non ricerca un film, mostro quelli popolari
            if (!hasSubmittedSearch) {
                item {
                    Text(
                        text = "Popular Movies",
                        color = Color(0xFFF7F9FC),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                // Renderizzo la lista di films popolari ottenuti dall'API
                items(popularMovies) { movie ->
                    PopularMovieCard(
                        movie = movie,
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
