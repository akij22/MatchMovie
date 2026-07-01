package com.example.matchmovie

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.matchmovie.components.LoadingScreen
import com.example.matchmovie.components.MatchMovieBottomBar
import com.example.matchmovie.components.MatchMovieTitle
import com.example.matchmovie.database.FilmDatabase
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.enumentity.Screen
import com.example.matchmovie.model.ChatMessage
import com.example.matchmovie.model.MediaType
import com.example.matchmovie.model.MovieDetailsUi
import com.example.matchmovie.model.toMovieDetailsUi
import com.example.matchmovie.network.AuthToken
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.MovieCreditsDto
import com.example.matchmovie.network.dto.SingleMovieResultDto
import com.example.matchmovie.network.dto.SingleTvSeriesResultDto
import com.example.matchmovie.screens.AIChatScreen
import com.example.matchmovie.screens.ExploreScreen
import com.example.matchmovie.screens.FilmDetailScreen
import com.example.matchmovie.screens.LoginScreen
import com.example.matchmovie.screens.MyListScreen
import com.example.matchmovie.screens.ProfileScreen
import com.example.matchmovie.screens.StatsScreen
import com.example.matchmovie.screens.HomeScreen
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import com.example.matchmovie.ui.theme.MatchMovieSurface
import com.example.matchmovie.ui.theme.MatchMovieTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
            var showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1800)
                showSplash = false
            }

            LaunchedEffect(Unit) {

                // Ottengo l'utente loggato
                val loggedUser = withContext(Dispatchers.IO) {
                    dao.getLoggedInUser()
                }

                if (loggedUser == null) {
                    AuthToken.token = null
                    currentUser = null
                    currentScreen = Screen.LoginPage
                } else {
                    AuthToken.token = loggedUser.password

                    val validatedUser = runCatching {
                        withContext(Dispatchers.IO) {
                            RetrofitInstance.api.getCurrentUser()
                        }
                    }.getOrNull()

                    if (validatedUser == null) {
                        withContext(Dispatchers.IO) {
                            dao.logoutAllUsers()
                        }
                        AuthToken.token = null
                        currentUser = null
                        currentScreen = Screen.LoginPage
                    } else {
                        currentUser = loggedUser.copy(
                            name = validatedUser.name.ifBlank { validatedUser.email },
                            email = validatedUser.email,
                            profileImage = validatedUser.profileImage,
                            bio = validatedUser.bio,
                        )
                        currentScreen = Screen.HomeScreen
                    }
                }

                isAuthLoaded = true
            }


            // Carica i dati condivisi e apre la schermata di dettaglio del film
            suspend fun openMovieDetails(
                movieDetails: MovieDetailsUi,
                canBeSaved: Boolean,
                backScreen: Screen
            ) {
                castByMovie = withContext(Dispatchers.IO) {
                    when (movieDetails.mediaType) {
                        MediaType.Movie -> RetrofitInstance.api.getMovieCredits(movieDetails.id)
                        MediaType.TvSeries -> RetrofitInstance.api.getTvSeriesCredits(movieDetails.id)
                    }
                }

                selectedMovie = movieDetails
                selectedMovieCanBeSaved = canBeSaved && movieDetails.mediaType == MediaType.Movie
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

            suspend fun onTvSeriesSelected(series: SingleTvSeriesResultDto) {
                openMovieDetails(
                    movieDetails = series.toMovieDetailsUi(),
                    canBeSaved = false,
                    backScreen = Screen.HomeScreen
                )
            }

            suspend fun onChatMovieSelected(movie: SingleMovieResultDto) {
                openMovieDetails(
                    movieDetails = movie.toMovieDetailsUi(),
                    canBeSaved = true,
                    backScreen = Screen.ChatScreen
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
                    if (showSplash) {
                        SplashScreen()
                    } else {
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
                                        LoadingScreen(modifier = Modifier.fillMaxSize())
                                    } else {
                                        // In base allo state `currentScreen`, mostro la rispettiva schermata
                                        when (currentScreen) {
                                            Screen.LoginPage -> LoginScreen(
                                                dao = dao,
                                                onAuthenticated = { user ->
                                                    currentUser = user
                                                    currentScreen = Screen.HomeScreen
                                                }
                                            )

                                            Screen.HomeScreen -> HomeScreen(
                                                dao,
                                                ::onMovieSelected,
                                                ::onTvSeriesSelected
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
                                            Screen.ChatScreen -> currentUser?.let { user ->
                                                AIChatScreen(
                                                    // Passo chatMessages e messagePrompt come parametri del Composable, in modo da mantenerli anche al cambio di schermata
                                                    chatMessages = chatMessages,
                                                    onChatMessagesChange = { chatMessages = it },
                                                    messagePrompt = chatMessagePrompt,
                                                    onMessagePromptChange = { chatMessagePrompt = it },
                                                    onMovieSelected = ::onChatMovieSelected,
                                                    currentUser = user,
                                                    dao = dao
                                                )
                                            } ?: run {
                                                currentScreen = Screen.LoginPage
                                            }
                                            Screen.ProfileScreen -> ProfileScreen(
                                                user = currentUser,
                                                dao = dao,
                                                onOpenStats = {
                                                    currentScreen = Screen.StatsScreen
                                                },
                                                onLogout = {
                                                    coroutineScope.launch {
                                                        withContext(Dispatchers.IO) {
                                                            dao.logoutAllUsers()
                                                        }

                                                        // "Pulisco" la sessione corrente
                                                        currentUser = null
                                                        AuthToken.token = null
                                                        selectedMovie = null
                                                        selectedMovieCanBeSaved = true
                                                        castByMovie = null
                                                        currentScreen = Screen.LoginPage
                                                    }
                                                }
                                            )
                                            Screen.StatsScreen -> currentUser?.let { user ->
                                                StatsScreen(
                                                    user = user,
                                                    dao = dao,
                                                    onBackClick = { currentScreen = Screen.ProfileScreen }
                                                )
                                            } ?: run {
                                                currentScreen = Screen.LoginPage
                                            }

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
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MatchMovieBackground,
                        MatchMovieSurface,
                        MatchMovieBackground
                    )
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.matchmovie_logo),
                contentDescription = "MatchMovie logo",
                modifier = Modifier
                    .size(132.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MatchMovieCard)
                    .padding(18.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "MatchMovie",
                color = MatchMovieLightText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Find your next film",
                color = MatchMovieMutedText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            CircularProgressIndicator(
                color = MatchMoviePrimary,
                strokeWidth = 3.dp,
                modifier = Modifier.size(32.dp)
            )
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


