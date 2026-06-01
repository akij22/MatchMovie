package com.example.matchmovie.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.matchmovie.network.dto.SingleMovieResultDto
import com.example.matchmovie.ui.theme.MatchMovieTheme

@Composable
fun FilmDetailScreen(clickedFilm: SingleMovieResultDto) {

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

                Text(
                    text = clickedFilm.overview?.takeIf { it.isNotBlank() }
                        ?: "No description available.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FilmDetailScreenPreview() {
    MatchMovieTheme {
        FilmDetailScreen(
            clickedFilm = SingleMovieResultDto(
                id = 1,
                title = "Movie title",
                overview = "Movie overview shown in the details screen preview.",
                poster_path = null,
                backdrop_path = null,
                release_date = "2026-06-01",
                genre_ids = emptyList(),
                vote_average = 8.0,
                original_language = "en",
                popularity = 100.0
            )
        )
    }
}
