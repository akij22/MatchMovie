package com.example.matchmovie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.enumentity.MovieMood
import com.example.matchmovie.network.dto.MovieCastMemberDto
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieCrewMemberDto
import com.example.matchmovie.network.dto.SingleMovieResultDto
import kotlinx.coroutines.launch

@Composable
fun FilmDetailScreen(
    clickedFilm: SingleMovieResultDto,
    cast: MovieCreditsDto?,
    dao: FilmDAO,
    onBackClick: () -> Unit
) {
    val backdropUrl = clickedFilm.backdrop_path?.let { "https://image.tmdb.org/t/p/w780$it" }
    val posterUrl = clickedFilm.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
    var userRating by remember { mutableIntStateOf(0) }

    // Coroutine per lanciare operazioni su DB
    val coroutineScope = rememberCoroutineScope()

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
                    contentDescription = clickedFilm.title,
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
                        InfoChip(text = "★ ${String.format("%.1f", clickedFilm.vote_average)}")
                        InfoChip(text = clickedFilm.original_language.uppercase())
                    }

                    Text(
                        text = clickedFilm.title,
                        color = Color(0xFFF7F9FC),
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = clickedFilm.release_date ?: "Release date not available",
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
                    text = "Film Description",
                    color = Color(0xFFF7F9FC),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = clickedFilm.overview?.takeIf { it.isNotBlank() }
                        ?: "No description available.",
                    color = Color(0xFFD6E0EC),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }


        // Sezione per il caricamento dl Cast e del regista
        if (cast == null) {
            item {
                Text(
                    text = "Loading cast...",
                    color = Color(0xFFAAB6C2),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
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

            // Ottengo solo il regista del film, prendendolo dalla lista `crew` che è di tipo `MovieCrewMemberDto`
            val director = cast.crew.firstOrNull { it.job == "Director" }

            if (director != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 30.dp)) {
                        Text(
                            text = "Director",
                            color = Color(0xFFF7F9FC),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Mostro il regista mediante apposito Composable
                        DirectorMemberItem(member = director)
                    }
                }
            }

        }

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
                            dao.insert(

                                // Creo un nuovo UserMovie
                                UserMovie(
                                    tmdbMovieId = clickedFilm.id,
                                    title = clickedFilm.title,
                                    description = clickedFilm.overview ?: "",
                                    image = clickedFilm.poster_path ?: "",
                                    bio = "",
                                    userRating = userRating,
                                    release_date = clickedFilm.release_date,

                                    // TODO
                                    mood = MovieMood.RELAXED
                                )
                            )
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
