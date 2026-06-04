package com.example.matchmovie.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.network.dto.SingleMovieResultDto
import kotlinx.coroutines.launch

@Composable
fun PopularMovieCard (
    movie: SingleMovieResultDto,
    onMovieSelected: suspend (SingleMovieResultDto) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val posterUrl = movie.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
    val releaseYear = movie.release_date
        ?.takeIf { it.length >= 4 }
        ?.take(4)
        ?: "N/A"

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .aspectRatio(2f / 3f),
        onClick = {
            coroutineScope.launch {

                // Al click della card del film popolare, carico il cast e regista e passo alla schermata `FilmDetailsScreen`
                onMovieSelected(movie)
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF182029))
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = "Poster ${movie.title}",
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
                                Color(0xF0101820)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color(0xFFF7F9FC),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "$releaseYear • ★ ${String.format("%.1f", movie.vote_average)}",
                    color = Color(0xFFE1BEBF),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}