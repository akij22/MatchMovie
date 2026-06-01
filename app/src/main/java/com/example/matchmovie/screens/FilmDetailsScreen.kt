package com.example.matchmovie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.matchmovie.network.dto.MovieCastMemberDto
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.MovieCrewMemberDto
import com.example.matchmovie.network.dto.SingleMovieResultDto

@Composable
fun FilmDetailScreen(clickedFilm: SingleMovieResultDto, cast: MovieCreditsDto?) {

    LazyColumn (
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        item {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = clickedFilm.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text (
                    text = (clickedFilm.release_date ?: "not available")
                            + " ⸱ " + clickedFilm.original_language
                )

                Text(
                    text = clickedFilm.overview?.takeIf { it.isNotBlank() }
                        ?: "No description available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Cast",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (cast == null) {
            item {
                Text(
                    text = "Loading cast...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            item {
                LazyRow {

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
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Film Director",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )


                    Column(
                        modifier = Modifier.width(64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF142231)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Renderizzo l'immagine del regista
                            AsyncImage(
                                model = director.imageUrl,
                                contentDescription = director.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = director.name,
                            color = Color(0xFFEAF0F7),
                            fontSize = 11.sp,
                            lineHeight = 13.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

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
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(Color(0xFF142231)),
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
            color = Color(0xFFEAF0F7),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = member.role.uppercase(),
            color = Color(0xFF6F7D8C),
            fontSize = 8.sp,
            lineHeight = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
