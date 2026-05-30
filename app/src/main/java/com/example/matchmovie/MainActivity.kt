package com.example.matchmovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.matchmovie.database.FilmDatabase
import com.example.matchmovie.enumentity.Screen
import com.example.matchmovie.screens.SearchScreen
import com.example.matchmovie.ui.theme.MatchMovieTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Ottengo il DAO
        val dao = FilmDatabase.getInstance(applicationContext).getDao()

        setContent {
            MatchMovieTheme {

                // DA CAMBIARE --> Schermata iniziale dovrà essere `MyFilmScreen`
                val currentScreen by remember { mutableStateOf(Screen.SearchScreen)}

                when(currentScreen) {
                    Screen.LoginPage -> Text("Login")
                    Screen.SearchScreen -> SearchScreen(dao)
                    Screen.FilmDetailsScreen -> Text("Film details")
                    Screen.MyFilmScreen -> Text("My films")
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
