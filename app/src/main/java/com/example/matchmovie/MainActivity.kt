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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.matchmovie.database.FilmDatabase
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.components.MatchMovieTitle
import com.example.matchmovie.enumentity.Screen
import com.example.matchmovie.model.ChatMessage
import com.example.matchmovie.model.MovieDetailsUi
import com.example.matchmovie.model.toMovieDetailsUi
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.SingleMovieResultDto
import com.example.matchmovie.screens.AIChatScreen
import com.example.matchmovie.screens.ExploreScreen
import com.example.matchmovie.screens.FilmDetailScreen
import com.example.matchmovie.screens.LoginScreen
import com.example.matchmovie.screens.MyListScreen
import com.example.matchmovie.screens.ProfileScreen
import com.example.matchmovie.screens.HomeScreen
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMovieTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        // Ottengo il DAO
        val dao = FilmDatabase.getInstance(applicationContext).getDao()

        setContent {
            var selectedMovie by remember { mutableStateOf<MovieDetailsUi?>(null) }

            // State per distinguere film upComing da quelli gia usciti
            var selectedMovieCanBeSaved by remember { mutableStateOf(true) }
            var selectedMovieBackScreen by remember { mutableStateOf(Screen.HomeScreen) }

            var currentUser by remember { mutableStateOf<User?>(null) }
            var isAuthLoaded by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()

            // Memorizzo il cast ottenuto dall'id del film
            var castByMovie by remember { mutableStateOf<MovieCreditsDto?>(null) }

            // Mantengo lo stato della chat nel componente padre di AIChatScreen, così non viene perso cambiando schermata
            var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
            var chatMessagePrompt by remember { mutableStateOf("") }


            // Imposto come schermata di inizio quella di Login
            var currentScreen by remember { mutableStateOf(Screen.LoginPage)}

            LaunchedEffect(Unit) {

                // Ottengo l'utente loggato
                val loggedUser = withContext(Dispatchers.IO) {
                    dao.getLoggedInUser()
                }
                currentUser = loggedUser

                // Se c'è un utente loggato, mostro la relativa schermata dei film
                // Altrimenti, devo far eseguire il login (/ signup)
                currentScreen = if (loggedUser == null) Screen.LoginPage else Screen.HomeScreen
                isAuthLoaded = true
            }


            // Carica i dati condivisi e apre la schermata di dettaglio del film
            suspend fun openMovieDetails(
                movieDetails: MovieDetailsUi,
                canBeSaved: Boolean,
                backScreen: Screen
            ) {
                castByMovie = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getMovieCredits(movieDetails.id)
                }

                selectedMovie = movieDetails
                selectedMovieCanBeSaved = canBeSaved
                selectedMovieBackScreen = backScreen
                currentScreen = Screen.FilmDetailsScreen
            }

            // Function che viene triggerata al click di un film presente nella lista di quelli ottenuti in HomeScreen

            // Al click su un film, in base alla lista da cui l'ho ottenuto, distinguo se è upComing oppure no
            suspend fun onMovieSelected(movie: SingleMovieResultDto, isUpcomingMovie: Boolean = false) {
                openMovieDetails(
                    movieDetails = movie.toMovieDetailsUi(),
                    canBeSaved = !isUpcomingMovie,
                    backScreen = Screen.HomeScreen
                )
            }


            // Funzione chiamata al click su una Card presente in `MyListScreen`
            // State hoisting: muovo la funzione che modifica uno state nel Composable antenato comune ai 2
            // (`MyListScreen` e `HomeScreen`)
            suspend fun onSavedMovieSelected(movie: UserMovie) {
                openMovieDetails(
                    movieDetails = movie.toMovieDetailsUi(),
                    canBeSaved = false,
                    backScreen = Screen.MyListScreen
                )
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
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {

                            // Mostro il Composable per il titolo dell'app, tranne per FilmDetailsScreen e LoginScreen
                            if (currentScreen != Screen.FilmDetailsScreen && currentScreen != Screen.LoginPage)
                                MatchMovieTitle()

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f)
                            ) {

                            // Definisco la schermata da mostrare, associandola ad ogni enum
                            // della classe `Screen`
                            if (!isAuthLoaded) {
                                PlaceholderScreen(title = "Loading")
                            } else {

                                // In base allo state `currentScreen`, mostro la rispettiva schermata
                                when(currentScreen) {
                                    Screen.LoginPage -> LoginScreen(
                                        dao = dao,
                                        onAuthenticated = { user ->
                                            currentUser = user
                                            currentScreen = Screen.HomeScreen
                                        }
                                    )


                                Screen.HomeScreen -> HomeScreen(
                                    dao,
                                    ::onMovieSelected
                                )

                                Screen.ExploreScreen -> currentUser?.let { user ->
                                    ExploreScreen(
                                        dao = dao,
                                        currentUser = user
                                    )
                                } ?: run {
                                    currentScreen = Screen.LoginPage
                                }

                                Screen.FilmDetailsScreen -> selectedMovie?.let { movie ->

                                    // Chiamo la nuova schermata, passando il film cliccato e il suo cast come parametri
                                    currentUser?.let { user ->
                                        FilmDetailScreen(
                                            movie = movie,
                                            cast = castByMovie,
                                            dao = dao,
                                            currentUser = user,

                                            // Se è un upComingMovie (=true), non posso salvarlo (=false)
                                            canSaveMovie = selectedMovieCanBeSaved,

                                            // Lambda per il ritorno alla schermata precedente, dopo aver aggiunto un film alla propria "collezione"
                                            onBackClick = {
                                                currentScreen = selectedMovieBackScreen
                                            }
                                        )

                                        // In caso il currentUser sia null, rimando alla LoginPage
                                    } ?: run {
                                        currentScreen = Screen.LoginPage
                                    }

                                }


                                // Mediante il when, assegno ad ogni enumeration la schermata corrispondente
                                Screen.ChatScreen -> AIChatScreen(

                                    // Passo chatMessages e messagePrompt come parametri del Composable, in modo da mantenerli anche al cambio di schermata
                                    chatMessages = chatMessages,
                                    onChatMessagesChange = { chatMessages = it },
                                    messagePrompt = chatMessagePrompt,
                                    onMessagePromptChange = { chatMessagePrompt = it }
                                )
                                Screen.ProfileScreen -> ProfileScreen(
                                    user = currentUser,
                                    dao = dao,
                                    onLogout = {
                                        coroutineScope.launch {
                                            withContext(Dispatchers.IO) {
                                                dao.logoutAllUsers()
                                            }

                                            // "Pulisco" la sessione corrente
                                            currentUser = null
                                            selectedMovie = null
                                            selectedMovieCanBeSaved = true
                                            castByMovie = null
                                            currentScreen = Screen.LoginPage
                                        }
                                    }
                                )
                                Screen.MyListScreen -> currentUser?.let { user ->
                                    MyListScreen(
                                        dao = dao,
                                        currentUser = user,
                                        onMovieSelected = ::onSavedMovieSelected
                                    )
                                } ?: run {
                                    currentScreen = Screen.LoginPage
                                }
                                }
                            }
                        }
                        }
                    }
                }
            }
        }
    }
}

private fun Screen.isBottomTab(): Boolean {
    return this == Screen.HomeScreen ||
        this == Screen.ExploreScreen ||
        this == Screen.MyListScreen ||
        this == Screen.ChatScreen ||
        this == Screen.ProfileScreen
}


// Data class di supporto per la bottom bar
private data class BottomBarItem(
    val screen: Screen,
    val label: String,
    val icon: BottomBarIcon
)

private enum class BottomBarIcon {
    Search,
    Explore,
    Profile,
    MyList,
    Chat
}


// Composable per la creazione di una bottom bar
@Composable
private fun MatchMovieBottomBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {
    val items = listOf(
        BottomBarItem(Screen.HomeScreen, "Home", BottomBarIcon.Search),
        BottomBarItem(Screen.ExploreScreen, "Explore", BottomBarIcon.Explore),
        BottomBarItem(Screen.MyListScreen, "MyList", BottomBarIcon.MyList),
        BottomBarItem(Screen.ChatScreen, "AI Chat", BottomBarIcon.Chat),
        BottomBarItem(Screen.ProfileScreen, "Profile", BottomBarIcon.Profile)
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

            BottomBarIcon.Explore -> {
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.32f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.5f, size.height * 0.5f),
                    end = Offset(size.width * 0.66f, size.height * 0.32f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f)
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

            BottomBarIcon.Chat -> {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.2f),
                    size = Size(size.width * 0.64f, size.height * 0.48f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                        size.minDimension * 0.12f,
                        size.minDimension * 0.12f
                    ),
                    style = stroke
                )
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.35f, size.height * 0.68f),
                    end = Offset(size.width * 0.27f, size.height * 0.82f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.38f, size.height * 0.44f)
                )
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.5f, size.height * 0.44f)
                )
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.035f,
                    center = Offset(size.width * 0.62f, size.height * 0.44f)
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
