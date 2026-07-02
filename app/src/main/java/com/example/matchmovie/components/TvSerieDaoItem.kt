package com.example.matchmovie.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.matchmovie.R
import com.example.matchmovie.database.UserTvSerie
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary

// Componente che rappresenta un singolo item da mostrare nella lista delle serie TV salvate dall'utente (MyList)
@Composable
fun TvSerieDaoItem(
    tvSerie: UserTvSerie,
    onTvSerieClick: () -> Unit,
    onDeleteClick: () -> Unit,

    // Parametro per riutilizzare il component con dimensioni (e formato) differenti
    compact: Boolean = false,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MatchMovieCard,
            contentColor = MatchMovieLightText
        ),
        modifier = modifier
            .padding(bottom = 12.dp)
            .clickable(onClick = onTvSerieClick)
    ) {
        if (compact) {
            Box {
                if (!tvSerie.image.isNullOrBlank()) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w500${tvSerie.image}",
                        contentDescription = "Poster ${tvSerie.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = tvSerie.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        repeat(5) { index ->
                            Text(
                                text = if (index < tvSerie.userRating) "★" else "☆",
                                color = if (index < tvSerie.userRating) Color(0xFFFFB400) else Color.White.copy(alpha = 0.3f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {

                // Controllo che il path dell'immagine non sia null
                if (!tvSerie.image.isNullOrBlank()) {
                    AsyncImage(

                        // Specifico l'url da cui recuperare l'immagine
                        model = "https://image.tmdb.org/t/p/w500${tvSerie.image}",
                        contentDescription = "Poster ${tvSerie.title}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))


                Text(
                    text = tvSerie.title,
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                tvSerie.first_air_date?.takeIf { it.isNotBlank() }?.let { firstAirDate ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = firstAirDate,
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                tvSerie.description.takeIf { it.isNotBlank() }?.let { overview ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = overview,
                        color = MatchMovieLightText,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Rating: ${tvSerie.userRating}",
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                            colorFilter = ColorFilter.tint(MatchMoviePrimary),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
