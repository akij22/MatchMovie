package com.example.matchmovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.matchmovie.database.FilmDatabase
import com.example.matchmovie.enumentity.Screen
import com.example.matchmovie.network.dto.SingleMovieResultDto
import com.example.matchmovie.screens.FilmDetailScreen
import com.example.matchmovie.screens.SearchScreen
import com.example.matchmovie.ui.theme.MatchMovieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Ottengo il DAO
        val dao = FilmDatabase.getInstance(applicationContext).getDao()

        setContent {
            var selectedMovie by remember { mutableStateOf<SingleMovieResultDto?>(null) }

            // DA CAMBIARE --> Schermata iniziale dovrà essere `MyFilmScreen`
            var currentScreen by remember { mutableStateOf(Screen.SearchScreen)}


            // Definizione della function che viene triggerata al click di un film presente nella lista di quelli ottenuti in SearchScreen
            val onMovieSelected: (SingleMovieResultDto) -> Unit = { movie ->
                selectedMovie = movie
                currentScreen = Screen.FilmDetailsScreen
            }

            MatchMovieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    when(currentScreen) {
                        Screen.LoginPage -> Text("Login")
                        Screen.SearchScreen -> SearchScreen(
                            dao,
                            onMovieSelected
                        )

                        Screen.FilmDetailsScreen -> selectedMovie?.let { movie ->
                            FilmDetailScreen(clickedFilm = movie)
                        }
                        Screen.MyFilmScreen -> Text("My films")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MatchMovieTheme {
        Greeting("Android")
    }
}
