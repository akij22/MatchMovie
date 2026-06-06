package com.example.matchmovie.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.network.dto.SingleMovieResultDto
import kotlinx.coroutines.launch

// Componente che rappresenta un singolo item da mostrare nella lista dei risultati della ricerca
@Composable
fun MovieResultItem(movie: SingleMovieResultDto, onMovieSelected: suspend (SingleMovieResultDto) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // Controllo che il path dell'immagine non sia null
            if (movie.poster_path != null) {
                AsyncImage (

                    // Specifico l'url da cui recuperare l'immagine
                    model = "https://image.tmdb.org/t/p/w500${movie.poster_path}",
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            movie.release_date?.takeIf { it.isNotBlank() }?.let { releaseDate ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = releaseDate,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            movie.overview?.takeIf { it.isNotBlank() }?.let { overview ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = overview,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Rating: ${movie.vote_average}",
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        onMovieSelected(movie)
                    }
                }
            ) {
                Text("Film Details")
            }
        }
    }
}
