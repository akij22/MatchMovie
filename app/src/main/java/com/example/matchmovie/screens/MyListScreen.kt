package com.example.matchmovie.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchmovie.R
import com.example.matchmovie.components.MovieDaoItem
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MyListScreen (
    dao: FilmDAO,
    currentUser: User,
    onMovieSelected: suspend (UserMovie) -> Unit
) {

    var userFilmList by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()


    // Caricamento dei film dell'utente alla prima apertura di `MyListScreen` mediante una coroutine dedicata
    LaunchedEffect(currentUser._id) {
        userFilmList = dao.getMoviesByUser(currentUser._id)
    }


    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MatchMovieBackground)
            .fillMaxWidth()
    ) {
        if (userFilmList.isEmpty()) {
            item {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.mylist_notfound),
                        contentDescription = "No movies saved yet",
                        modifier = Modifier
                            .size(200.dp)
                    )
                    Text(
                        text = "No movies saved yet",
                        color = MatchMovieMutedText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Renderizzo la lista di film salvati dall'utente
        items(userFilmList) { movie ->
            MovieDaoItem(
                movie = movie,
                onMovieClick = {
                    coroutineScope.launch {
                        onMovieSelected(movie)
                    }
                },

                // Definizione della function per eliminare un film dalla MyList
                onDeleteClick = {
                    coroutineScope.launch {
                        withContext(Dispatchers.IO) {
                            dao.deleteUserMovie(movie._id)
                        }
                        userFilmList = userFilmList.filterNot { savedMovie ->
                            savedMovie._id == movie._id
                        }
                    }
                }
            )
        }
    }
}
