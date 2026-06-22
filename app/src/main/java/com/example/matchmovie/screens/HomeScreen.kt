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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.matchmovie.components.MovieCard
import com.example.matchmovie.components.StatusMessage
import com.example.matchmovie.enumentity.MovieMood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    dao: FilmDAO,
    onMovieSelected: suspend (SingleMovieResultDto, Boolean) -> Unit
) {
    var movies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    // State per memorizzare i film più popolari mediante apposito endpoint
    var popularMovies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    var upComingMovies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }

    var isRefreshing by remember { mutableStateOf(false) }
    var isLoadingPopularMovies by remember { mutableStateOf(true) }
    var isLoadingUpcomingMovies by remember { mutableStateOf(true) }
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


    // Funzione per il recupero dei film in uscita dall'endpoint
    // Successivamente li filtro per ottenere quelli non presenti anche nella lista `popularFilms`
    // e per ottenere solo quelli la cui release_date è > data odierna
    suspend fun obtainUpcomingFilms(genreNamesById: Map<Int, String> = genresById) {
        isLoadingUpcomingMovies = true
        upcomingMoviesError = null

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.getUpcomingMovies()
            }
            val popularMovieIds = popularMovies.map { movie -> movie.id }.toSet()
            val upcomingMoviesNotInPopular = response.results.filterNot { movie ->
                movie.id in popularMovieIds

            }.filter { movie ->
                val releaseDate = movie.release_date
                !releaseDate.isNullOrBlank() && releaseDate > todayIso
            }.sortedBy { movie ->
                movie.release_date
            }

            // Applico il mood ottenuto alla lista di film in arrivo della SearchScreen
            upComingMovies = applyMoodToMovies(upcomingMoviesNotInPopular, genreNamesById)

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
            refreshError = "Unable to search films, please try again."
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

        HomeSearchBar(
            value = filmString,
            onValueChange = {
                filmString = it
                if (it.isBlank()) {
                    hasSubmittedSearch = false
                }
            },
            isSearching = isRefreshing,
            onSearch = {
                coroutineScope.launch {
                    obtainSearchedFilms()
                    hasSubmittedSearch = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

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
                if (isLoadingPopularMovies || isLoadingUpcomingMovies) {

                    // Mostro messaggio informativo durante il caricamento dei dati
                    item {
                        StatusMessage("Loading movies...",
                            modifier = Modifier.fillParentMaxSize()
                        )
                    }
                } else {
                    featuredMovie?.let { movie ->
                        item {
                            // Renderizzo il film in primo piano nella parte alta della SearchScreen
                            FeaturedMovieCard(
                                movie = movie,
                                onMovieSelected = { selectedMovie ->
                                    onMovieSelected(selectedMovie, false)
                                }
                            )
                        }
                    }

                    item {
                        // Renderizzo la lista di films popolari ottenuti dall'API
                        MovieSection(
                            title = "Popular films",
                            movies = popularMovies,
                            onMovieSelected = { selectedMovie ->
                                onMovieSelected(selectedMovie, false)
                            }
                        )
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
                            title = "Available soon",
                            movies = upComingMovies,
                            onMovieSelected = { selectedMovie ->
                                onMovieSelected(selectedMovie, true)
                            }
                        )
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
                }
            } else {
                // Renderizzo la lista di films ottenuti dall'API solo nel momento in cui l'utente invia la ricerca
                items(movies) { movie ->
                    MovieResultItem(
                        movie = movie,
                        onMovieSelected = { selectedMovie ->
                            onMovieSelected(selectedMovie, false)
                        }
                    )
                }
            }
        }
    }
}


// Composable per la barra di ricerca per nome
@Composable
private fun HomeSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    isSearching: Boolean,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xB3232B33))
            .border(1.dp, Color(0x1AF7F9FC), RoundedCornerShape(50))
            .padding(start = 16.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⌕",
            color = Color(0xFFE1BEBF),
            style = MaterialTheme.typography.titleLarge
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFDBE3EF)),
            cursorBrush = SolidColor(Color(0xFF4FDBCC)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                if (value.isNotBlank() && !isSearching) {
                    onSearch()
                }
            }),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {

                    // Se non c'è alcuna ricerca nel TextField, mostro placeholder
                    if (value.isBlank()) {
                        Text(
                            text = "Find films...",
                            color = Color(0x80E1BEBF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.weight(1f)
        )

        Button(
            enabled = value.isNotBlank() && !isSearching,
            onClick = onSearch,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE84A5F),
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2E363E),
                disabledContentColor = Color(0x80E1BEBF)
            )
        ) {
            Text(
                text = if (isSearching) "..." else "Search",
                fontWeight = FontWeight.Bold
            )
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
                text = "Latest release",
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
                MovieCard(
                    movie = movie,
                    onMovieSelected = onMovieSelected,
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
                MovieCard(
                    movie = movie,

                    // Al click su un film in arrivo, mostro il dettaglio ma senza possibilita di salvataggio
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
