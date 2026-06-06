package com.example.matchmovie.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.database.UserMovie

// Componente che rappresenta un singolo item da mostrare nella lista dei film salvati dall'utente
@Composable
fun MovieDaoItem(movie: UserMovie) {
    Card (
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

            movie.description.takeIf { it.isNotBlank() }?.let { overview ->
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
                text = "Rating: ${movie.userRating}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
