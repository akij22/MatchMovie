package com.example.matchmovie.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary

// Componente che rappresenta un singolo item da mostrare nella lista dei film salvati dall'utente
@Composable
fun MovieDaoItem(
    movie: UserMovie,
    onDeleteClick: () -> Unit
) {
    Card (
        colors = CardDefaults.cardColors(
            containerColor = MatchMovieCard,
            contentColor = MatchMovieLightText
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Controllo che il path dell'immagine non sia null
            if (!movie.image.isNullOrBlank()) {
                AsyncImage (

                    // Specifico l'url da cui recuperare l'immagine
                    model = "https://image.tmdb.org/t/p/w500${movie.image}",
                    contentDescription = "Poster ${movie.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = movie.title,
                color = MatchMovieLightText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            movie.release_date?.takeIf { it.isNotBlank() }?.let { releaseDate ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = releaseDate,
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            movie.description.takeIf { it.isNotBlank() }?.let { overview ->
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
                text = "Rating: ${movie.userRating}",
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
                    TrashIcon(
                        color = MatchMoviePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrashIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)

        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.32f, size.height * 0.32f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.68f, size.height * 0.32f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = androidx.compose.ui.geometry.Offset(size.width * 0.42f, size.height * 0.22f),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.58f, size.height * 0.22f),
            strokeWidth = stroke.width,
            cap = StrokeCap.Round
        )
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.34f, size.height * 0.4f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                size.minDimension * 0.05f,
                size.minDimension * 0.05f
            ),
            style = stroke
        )
    }
}
