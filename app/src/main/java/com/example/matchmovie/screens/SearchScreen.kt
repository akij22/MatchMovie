package com.example.matchmovie.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.SingleMovieResultDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SearchScreen(dao: FilmDAO) {
    var movies by remember { mutableStateOf<List<SingleMovieResultDto>>(emptyList()) }
    var isRefreshing by remember { mutableStateOf(false) }
    var refreshError by remember { mutableStateOf<String?>(null) }
    var filmString by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    suspend fun refreshFilms() {
        if (filmString.isBlank()) {
            movies = emptyList()
            refreshError = null
            return
        }

        isRefreshing = true
        refreshError = null

        try {
            val response = withContext(Dispatchers.IO) {
                RetrofitInstance.api.searchMovies(filmString)
            }

            movies = response.results
        } catch (e: Exception) {
            refreshError = e.localizedMessage ?: "Unable to refresh films"
        } finally {
            isRefreshing = false
        }
    }


    Column(
        modifier = Modifier.padding(top = 16.dp)
    ) {
        OutlinedTextField(
            value = filmString,
            onValueChange = { filmString = it },
            label = { Text("Movie title") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            enabled = !isRefreshing && filmString.isNotBlank(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier
                .height(58.dp)
                .padding(start = 16.dp),
            onClick = {

                // Lancio la coroutine per ottenere i film dall'API
                coroutineScope.launch {
                    refreshFilms()
                }
            }
        ) {
            Text(
                text = if (isRefreshing) "Searching..." else "Search",
                fontWeight = FontWeight.Bold
            )
        }

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
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(movies) { movie ->
                MovieResultItem(movie = movie)
            }
        }
    }
}

@Composable
private fun MovieResultItem(movie: SingleMovieResultDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
        }
    }
}
