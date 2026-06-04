package com.example.matchmovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.matchmovie.database.FilmDatabase
import com.example.matchmovie.enumentity.Screen
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.SingleMovieResultDto
import com.example.matchmovie.screens.FilmDetailScreen
import com.example.matchmovie.screens.SearchScreen
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMovieTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Ottengo il DAO
        val dao = FilmDatabase.getInstance(applicationContext).getDao()

        setContent {
            var selectedMovie by remember { mutableStateOf<SingleMovieResultDto?>(null) }

            // Memorizzo il cast ottenuto dall'id del film
            var castByMovie by remember { mutableStateOf<MovieCreditsDto?>(null) }

            // DA CAMBIARE --> Schermata iniziale dovrà essere `MyFilmScreen`
            var currentScreen by remember { mutableStateOf(Screen.SearchScreen)}


            // Function che viene triggerata al click di un film presente nella lista di quelli ottenuti in SearchScreen
            suspend fun onMovieSelected(movie: SingleMovieResultDto) {

                // Recupero il cast del film mediante API
                val cast = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getMovieCredits(movie.id)
                }
                castByMovie = cast

                selectedMovie = movie

                // Passo alla schermata del singolo film cliccato
                currentScreen = Screen.FilmDetailsScreen

            }

            MatchMovieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            if (currentScreen.isBottomTab()) {
                                MatchMovieBottomBar(
                                    currentScreen = currentScreen,

                                    // Lambda per il cambio di schermata
                                    onTabSelected = { screen ->
                                        currentScreen = screen
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {

                            // Definisco la schermata da mostrare, associandola ad ogni enum
                            // della classe `Screen`
                            when(currentScreen) {
                                Screen.LoginPage -> Text("Login")
                                Screen.SearchScreen -> SearchScreen(
                                    dao,
                                    ::onMovieSelected
                                )

                                Screen.FilmDetailsScreen -> selectedMovie?.let { movie ->

                                    // Chiamo la nuova schermata, passando il film cliccato e il suo cast come parametri
                                    FilmDetailScreen(clickedFilm = movie, cast = castByMovie)
                                }


                                // Mediante il when, assegno ad ogni enumeration la schermata corrispondente
                                // (TO UPDATE)
                                Screen.HomeScreen -> PlaceholderScreen(title = "Home")
                                Screen.ProfileScreen -> PlaceholderScreen(title = "Profile")
                                Screen.MyListScreen -> PlaceholderScreen(title = "MyList")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun Screen.isBottomTab(): Boolean {
    return this == Screen.SearchScreen ||
        this == Screen.ProfileScreen ||
        this == Screen.MyListScreen ||
        this == Screen.HomeScreen
}


// Data class di supporto per la bottom bar
private data class BottomBarItem(
    val screen: Screen,
    val label: String,
    val icon: BottomBarIcon
)

private enum class BottomBarIcon {
    Search,
    Profile,
    MyList,
    Home
}


// Composable per la creazione di una bottom bar
@Composable
private fun MatchMovieBottomBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        BottomBarItem(Screen.SearchScreen, "Search", BottomBarIcon.Search),
        BottomBarItem(Screen.ProfileScreen, "Profile", BottomBarIcon.Profile),
        BottomBarItem(Screen.MyListScreen, "MyList", BottomBarIcon.MyList),
        BottomBarItem(Screen.HomeScreen, "Home", BottomBarIcon.Home)
    )

    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
        containerColor = MatchMovieCard,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->

            // Individuo la schermata cliccata nella bottom bar
            val selected = currentScreen == item.screen
            val contentColor = if (selected) Color(0xFF5B0016) else MatchMovieMutedText

            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(item.screen) },
                icon = {
                    BottomBarIconGraphic(
                        icon = item.icon,
                        color = contentColor,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = contentColor,
                    selectedTextColor = contentColor,
                    indicatorColor = Color(0xFFFA576B),
                    unselectedIconColor = MatchMovieMutedText,
                    unselectedTextColor = MatchMovieMutedText
                )
            )
        }
    }
}


// Composable per la creazione delle singole icone delle schermate nella bottom bar
@Composable
private fun BottomBarIconGraphic(
    icon: BottomBarIcon,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = size.minDimension * 0.09f, cap = StrokeCap.Round)

        when (icon) {
            BottomBarIcon.Search -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.28f,
                    center = Offset(size.width * 0.43f, size.height * 0.43f),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.63f, size.height * 0.63f),
                    end = Offset(size.width * 0.82f, size.height * 0.82f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            BottomBarIcon.Profile -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.16f,
                    center = Offset(size.width * 0.5f, size.height * 0.32f),
                    style = stroke
                )
                drawArc(
                    color = color,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.23f, size.height * 0.43f),
                    size = Size(size.width * 0.54f, size.height * 0.44f),
                    style = stroke
                )
            }

            BottomBarIcon.MyList -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.22f, size.height * 0.18f),
                    size = Size(size.width * 0.56f, size.height * 0.64f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        size.minDimension * 0.06f,
                        size.minDimension * 0.06f
                    ),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.36f, size.height * 0.38f),
                    end = Offset(size.width * 0.64f, size.height * 0.38f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.36f, size.height * 0.56f),
                    end = Offset(size.width * 0.64f, size.height * 0.56f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }

            BottomBarIcon.Home -> {
                val roof = Path().apply {
                    moveTo(size.width * 0.18f, size.height * 0.48f)
                    lineTo(size.width * 0.5f, size.height * 0.2f)
                    lineTo(size.width * 0.82f, size.height * 0.48f)
                }
                drawPath(roof, color = color, style = stroke)
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.27f, size.height * 0.48f),
                    end = Offset(size.width * 0.27f, size.height * 0.8f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.73f, size.height * 0.48f),
                    end = Offset(size.width * 0.73f, size.height * 0.8f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.27f, size.height * 0.8f),
                    end = Offset(size.width * 0.73f, size.height * 0.8f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}


// Componente "temporaneo", per mostrare schermate placeholder in attesa del loro sviluppo
@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MatchMovieBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                text = "Placeholder",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

