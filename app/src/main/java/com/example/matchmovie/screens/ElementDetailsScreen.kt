package com.example.matchmovie.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.network.dto.MovieCastMemberDto
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieCrewMemberDto
import com.example.matchmovie.network.dto.TvEpisodeDto
import com.example.matchmovie.network.dto.TvSeasonSummaryDto
import com.example.matchmovie.network.dto.TvSeriesDetailsDto
import com.example.matchmovie.model.MediaType
import com.example.matchmovie.model.MovieDetailsUi
import com.example.matchmovie.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FilmDetailScreen(
    movie: MovieDetailsUi,
    cast: MovieCreditsDto?,
    tvSeriesDetails: TvSeriesDetailsDto? = null,
    dao: FilmDAO,

    // currentUser passato mediante la MainActivity, la quale ha l'utente attualmente loggato in una state variable
    currentUser: User,
    canSaveMovie: Boolean = true,
    onBackClick: () -> Unit
) {
    val backdropUrl = movie.backdropPath?.let { "https://image.tmdb.org/t/p/w780$it" }
    val posterUrl = movie.posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }
    val isTvSeries = movie.mediaType == MediaType.TvSeries
    val releaseDateLabel = if (isTvSeries) "First air date not available" else "Release date not available"
    val descriptionTitle = if (isTvSeries) "TV Series Description" else "Film Description"
    val savedLabel = if (isTvSeries) "series" else "film"
    var userRating by remember { mutableIntStateOf(0) }
    var isMovieSaved by remember { mutableStateOf(false) }

    // State per la gestione delle stagioni/episodi delle serie TV
    var selectedSeasonNumber by remember { mutableIntStateOf(-1) }
    var seasonDetails by remember { mutableStateOf<com.example.matchmovie.network.dto.TvSeasonDetailsDto?>(null) }
    var episodes by remember { mutableStateOf<List<TvEpisodeDto>>(emptyList()) }
    var isLoadingSeason by remember { mutableStateOf(false) }

    // Coroutine per lanciare operazioni su DB
    val coroutineScope = rememberCoroutineScope()


    // Gestisco il back click mediante Composable apposito
    // Richiamo la funzione già esistente `onBackClick()` per tornare alla schermata precedente
    BackHandler(enabled = true) {
        onBackClick()
    }

    LaunchedEffect(currentUser._id, movie.id) {
        isMovieSaved = withContext(Dispatchers.IO) {
            dao.isMovieSaved(
                userId = currentUser._id,
                tmdbMovieId = movie.id
            )
        }
    }

    // All'apertura di una serie TV, seleziono automaticamente la prima stagione disponibile
    LaunchedEffect(tvSeriesDetails?.id) {
        val details = tvSeriesDetails ?: return@LaunchedEffect
        selectedSeasonNumber = details.seasons.firstOrNull()?.season_number ?: 0
    }

    // Al cambio di stagione selezionata, recupero gli episodi della stagione tramite API
    LaunchedEffect(tvSeriesDetails?.id, selectedSeasonNumber) {
        val details = tvSeriesDetails ?: return@LaunchedEffect
        if (selectedSeasonNumber < 0) return@LaunchedEffect

        isLoadingSeason = true
        seasonDetails = null
        episodes = emptyList()
        seasonDetails = withContext(Dispatchers.IO) {
            runCatching {
                RetrofitInstance.api.getTvSeriesSeason(details.id, selectedSeasonNumber)
            }.getOrNull()
        }
        episodes = seasonDetails?.episodes.orEmpty()
        isLoadingSeason = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C141C)),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                AsyncImage(

                    // Caricamento dell'immagine principale del film
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
                                    Color(0x330C141C),
                                    Color(0xCC0C141C),
                                    Color(0xFF0C141C)
                                )
                            )
                        )
                )


                /* Parte riguardante:
                * Icone di voto medio al film e lingua originale
                * Titolo del film
                * Data di uscita
                * */
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        movie.voteAverage?.let { voteAverage ->
                            InfoChip(text = "★ ${String.format("%.1f", voteAverage)}")
                        }
                        movie.originalLanguage?.takeIf { it.isNotBlank() }?.let { originalLanguage ->
                            InfoChip(text = originalLanguage.uppercase())
                        }
                        InfoChip(text = movie.mood.toString())
                        if (isTvSeries) {
                            InfoChip(text = "TV SERIES")
                        }
                    }

                    Text(
                        text = movie.title,
                        color = Color(0xFFF7F9FC),
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = movie.releaseDate ?: releaseDateLabel,
                        color = Color(0xFFE1BEBF),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                // Sezione riguardante la breve descrizione del film
                Text(
                    text = descriptionTitle,
                    color = Color(0xFFF7F9FC),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = movie.overview?.takeIf { it.isNotBlank() }
                        ?: "No description available.",
                    color = Color(0xFFD6E0EC),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }

        // Sezione stagioni/episodi (solo per le serie TV)
        if (isTvSeries && tvSeriesDetails != null) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Seasons & Episodes",
                            color = Color(0xFFF7F9FC),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        val selectedSeason = tvSeriesDetails.seasons
                            .find { it.season_number == selectedSeasonNumber }
                        selectedSeason?.episode_count?.let { count ->
                            Text(
                                text = "$count episodes",
                                color = Color(0x99E1BEBF),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selettore di stagione
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(tvSeriesDetails.seasons) { season ->
                            SeasonChip(
                                season = season,
                                selected = season.season_number == selectedSeasonNumber,
                                onClick = { selectedSeasonNumber = season.season_number }
                            )
                        }
                    }
                }
            }

            item {
                when {
                    isLoadingSeason -> {
                        LoadingScreen(
                            message = "Loading episodes...",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        )
                    }

                    seasonDetails?.episodes.isNullOrEmpty() -> {
                        Text(
                            text = "No episodes available for this season.",
                            color = Color(0xFFE1BEBF),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                        )
                    }

                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                        ) {
                            seasonDetails?.name?.takeIf { it.isNotBlank() }?.let { seasonName ->
                                Text(
                                    text = seasonName,
                                    color = Color(0xFF70F8E8),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                contentPadding = PaddingValues(end = 8.dp)
                            ) {
                                items(episodes) { episode ->
                                    EpisodeCard(episode = episode)
                                }
                            }
                        }
                    }
                }
            }
        }

        movie.userRating?.let { savedRating ->
            item {
                Text(
                    text = "Your saved rating: $savedRating",
                    color = Color(0xFFE1BEBF),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }


        // Sezione per il caricamento del Cast e del regista
        if (cast == null) {
            item {
                LoadingScreen(
                    message = "Loading cast...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        } else {
            item {
                Text(
                    text = "Top Cast",
                    color = Color(0xFFF7F9FC),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 28.dp, bottom = 14.dp)
                )

                // Inserisco il cast in una LazyRow, in modo da rendere la lista "scorribile"
                LazyRow (

                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {

                    // Renderizzo la lista di `MovieCastMemberDto` per mostrare SOLO gli attori
                    items(cast.cast) { actor ->
                        CastMemberItem(member = actor)
                    }
                }
            }

            val mainCrewMember = if (isTvSeries) {
                cast.crew.firstOrNull { it.job == "Creator" }
                    ?: cast.crew.firstOrNull { it.job == "Executive Producer" }
                    ?: cast.crew.firstOrNull()
            } else {
                cast.crew.firstOrNull { it.job == "Director" }
            }

            if (mainCrewMember != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 30.dp)) {
                        Text(
                            text = if (isTvSeries) "Creator" else "Director",
                            color = Color(0xFFF7F9FC),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Mostro il regista mediante apposito Composable
                        DirectorMemberItem(member = mainCrewMember)
                    }
                }
            }

        }

        if (canSaveMovie) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Your Rating",
                        color = Color(0xFFF7F9FC),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    StarRatingSelector(
                        rating = userRating,

                        // Collego lo State `userRating` al parametro della funzione `rating`
                        onRatingSelected = { rating -> userRating = rating }
                    )


                    // Se film è gia presente sul db dell'utente loggato, non permetto il salvataggio
                    if (isMovieSaved) {
                        Text(
                            text = "${savedLabel.replaceFirstChar { it.uppercase() }} already saved in MyList",
                            color = Color(0xFFE1BEBF),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Button(

                            // Salvo le stelle selezionate dall'utente, per formattarle in modo differente dalle altre
                            enabled = userRating > 0,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp),

                            // Al click, devo creare un nuovo oggetto UserMovie e salvarlo su DB
	                            onClick = {
	                                coroutineScope.launch {
	                                    withContext(Dispatchers.IO) {

	                                        dao.insert(
	                                            // Creo un nuovo UserMovie
	                                            UserMovie(
	                                                userId = currentUser._id,
	                                                tmdbMovieId = movie.id,
	                                                title = movie.title,
	                                                description = movie.overview ?: "",
	                                                image = movie.posterPath ?: "",
	                                                bio = "",
	                                                userRating = userRating,
	                                                release_date = movie.releaseDate,

	                                                mood = movie.mood
	                                            )
	                                        )
	                                    }
                                    isMovieSaved = true
                                    onBackClick()
                                }
                            }
                        ) {
                            Text(
                                text = "Save",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}


/* Composable per formattare le piccole iconi riguardanti:
* Voto medio
* Lingua originale
* */

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0x262EC4B6))
            .border(
                width = 1.dp,
                color = Color(0x442EC4B6),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = Color(0xFF70F8E8),
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


// Composable per il rating dell'utente mediante stelle
@Composable
private fun StarRatingSelector(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Renderizzo le 5 stelle
        (1..5).forEach { starValue ->
            val isSelected = starValue <= rating
            Text(
                text = "★",
                color = if (isSelected) Color(0xFFFFB400) else Color(0xFF4A5663),
                fontSize = 36.sp,
                lineHeight = 40.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))

                    // Al click su una singola stella, invoco la funzione passata come parametro
                    // (Ogni stella ha il corrispettivo numero associato)
                    .clickable { onRatingSelected(starValue) }
                    .padding(horizontal = 2.dp, vertical = 4.dp)
            )
        }

        Text(
            text = if (rating > 0) "$rating/5" else "Select",
            color = Color(0xFFE1BEBF),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}


// Composable per mostrare solo la sezione riguardante il regista
@Composable
private fun DirectorMemberItem(
    member: MovieCrewMemberDto,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0x99202C38))
            .border(
                width = 1.dp,
                color = Color(0x1AF7F9FC),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF142231))
                .border(2.dp, Color(0x44E84A5F), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Renderizzo l'immagine del regista
            AsyncImage(
                model = member.imageUrl,
                contentDescription = member.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier.padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = member.name,
                color = Color(0xFFF7F9FC),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = member.job.orEmpty().uppercase(),
                color = Color(0xFF70F8E8),
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}


/*
* Singolo componente per mostrare ogni singolo membro del cast
* foto del membro del cast "rounded circle"
*
*
* */

@Composable
fun CastMemberItem(
    member: MovieCastMemberDto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF142231))
                .border(2.dp, Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {

            // Renderizzo l'immagine del membro mediante `AsyncImage`
            AsyncImage(
                model = member.imageUrl,
                contentDescription = member.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = member.name,
            color = Color(0xFFF7F9FC),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = member.role.uppercase(),
            color = Color(0x99E1BEBF),
            fontSize = 9.sp,
            lineHeight = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// Composable per il selettore di stagione (chip)
@Composable
private fun SeasonChip(
    season: TvSeasonSummaryDto,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) Color(0xFFE84A5F) else Color(0x99202C38)
    val contentColor = if (selected) Color.White else Color(0xFFE1BEBF)
    val borderColor = if (selected) Color.Transparent else Color(0x1AF7F9FC)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = season.name?.takeIf { it.isNotBlank() }
                ?: "Season ${season.season_number}",
            color = contentColor,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// Composable per la card di un singolo episodio (non cliccabile)
@Composable
private fun EpisodeCard(
    episode: TvEpisodeDto,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(260.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF182029))
            .border(1.dp, Color(0x1AF7F9FC), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C141C))
        ) {
            AsyncImage(
                model = episode.stillUrl,
                contentDescription = episode.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Overlay scuro per leggibilità del badge runtime
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x330C141C)
                            )
                        )
                    )
            )

            // Badge numero episodio in alto a sinistra
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xCC0C141C))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "E${(episode.episode_number ?: 0).toString().padStart(2, '0')}",
                    color = Color(0xFF70F8E8),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Badge runtime in basso a destra
            episode.runtime?.let { runtime ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xCC0C141C))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${runtime}m",
                        color = Color(0xFFE1BEBF),
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = episode.name?.takeIf { it.isNotBlank() }
                ?: "Episode ${episode.episode_number ?: ""}",
            color = Color(0xFFF7F9FC),
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        episode.air_date?.takeIf { it.isNotBlank() }?.let { airDate ->
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = airDate,
                color = Color(0x99E1BEBF),
                fontSize = 11.sp,
                lineHeight = 13.sp
            )
        }

        episode.overview?.takeIf { it.isNotBlank() }?.let { overview ->
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = overview,
                color = Color(0xFFE1BEBF),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
