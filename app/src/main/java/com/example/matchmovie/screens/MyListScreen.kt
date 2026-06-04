package com.example.matchmovie.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.matchmovie.components.MovieDaoItem
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.UserMovie

@Composable
fun MyListScreen (dao: FilmDAO) {

    var userFilmList by remember { mutableStateOf<List<UserMovie>>(emptyList()) }


    // Caricamento dei film dell'utente alla prima apertura di `MyListScreen` mediante una coroutine dedicata
    LaunchedEffect(Unit) {
        userFilmList = dao.getAll()
    }


    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (userFilmList.isEmpty()) {
            item {
                Text(
                    text = "No movies saved yet",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        // Renderizzo la lista di film salvati dall'utente
        items(userFilmList) { movie ->
            MovieDaoItem(
                movie = movie
            )
        }
    }
}
