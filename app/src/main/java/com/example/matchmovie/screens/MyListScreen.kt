package com.example.matchmovie.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchmovie.R
import com.example.matchmovie.components.InfoMessage
import com.example.matchmovie.components.LoadingScreen
import com.example.matchmovie.components.MovieDaoItem
import com.example.matchmovie.components.TvSerieDaoItem
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.database.UserTvSerie
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class MyListContentType {
    Movies,
    TvSeries
}

@Composable
fun MyListScreen(
    dao: FilmDAO,
    currentUser: User,
    onMovieSelected: suspend (UserMovie) -> Unit,
    onTvSerieSelected: suspend (UserTvSerie) -> Unit
) {

    var userFilmList by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var userTvSeriesList by remember { mutableStateOf<List<UserTvSerie>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedContentType by remember { mutableStateOf(MyListContentType.Movies) }
    val coroutineScope = rememberCoroutineScope()


    // Caricamento dei film e delle serie TV dell'utente alla prima apertura di `MyListScreen`
    LaunchedEffect(currentUser._id) {
        isLoading = true
        val movies = withContext(Dispatchers.IO) {
            dao.getMoviesByUser(currentUser._id)
        }
        val tvSeries = withContext(Dispatchers.IO) {
            dao.getTvSeriesByUser(currentUser._id)
        }
        userFilmList = movies
        userTvSeriesList = tvSeries
        isLoading = false
    }


    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(MatchMovieBackground)
            .fillMaxWidth()
    ) {
        item {
            MyListContentTypeSwitch(
                selectedContentType = selectedContentType,
                onContentTypeSelected = { selectedContentType = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }

        when {
            isLoading -> item {
                LoadingScreen(
                    message = "Loading your list...",
                    modifier = Modifier.fillMaxSize()
                )
            }

            selectedContentType == MyListContentType.Movies && userFilmList.isEmpty() -> item {
                InfoMessage(R.drawable.mylist_notfound, "No movies saved yet")
            }

            selectedContentType == MyListContentType.TvSeries && userTvSeriesList.isEmpty() -> item {
                InfoMessage(R.drawable.mylist_notfound, "No TV series saved yet")
            }
        }

        // Renderizzo la lista di film salvati dall'utente
        if (selectedContentType == MyListContentType.Movies) {
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
        } else {
            // Renderizzo la lista di serie TV salvate dall'utente
            items(userTvSeriesList) { tvSerie ->
                TvSerieDaoItem(
                    tvSerie = tvSerie,
                    onTvSerieClick = {
                        coroutineScope.launch {
                            onTvSerieSelected(tvSerie)
                        }
                    },

                    // Definizione della function per eliminare una serie TV dalla MyList
                    onDeleteClick = {
                        coroutineScope.launch {
                            withContext(Dispatchers.IO) {
                                dao.deleteUserTvSerie(tvSerie._id)
                            }
                            userTvSeriesList = userTvSeriesList.filterNot { savedTvSerie ->
                                savedTvSerie._id == tvSerie._id
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun MyListContentTypeSwitch(
    selectedContentType: MyListContentType,
    onContentTypeSelected: (MyListContentType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xB3232B33))
            .border(1.dp, Color(0x1AF7F9FC), RoundedCornerShape(50))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MyListSwitchOptionButton(
            text = "Movies",
            selected = selectedContentType == MyListContentType.Movies,
            onClick = { onContentTypeSelected(MyListContentType.Movies) },
            modifier = Modifier.weight(1f)
        )
        MyListSwitchOptionButton(
            text = "TV Series",
            selected = selectedContentType == MyListContentType.TvSeries,
            onClick = { onContentTypeSelected(MyListContentType.TvSeries) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MyListSwitchOptionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (selected) Color(0xFFE84A5F) else Color.Transparent,
        animationSpec = tween(durationMillis = 250),
        label = "myListSwitchContainerColor"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color(0xFFE1BEBF),
        animationSpec = tween(durationMillis = 250),
        label = "myListSwitchContentColor"
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedContainerColor,
            contentColor = animatedContentColor
        ),
        contentPadding = PaddingValues(vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 12.sp
        )
    }
}
