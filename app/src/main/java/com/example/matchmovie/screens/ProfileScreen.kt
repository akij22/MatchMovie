package com.example.matchmovie.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.matchmovie.ui.theme.MatchMovieSecondary
import coil3.compose.AsyncImage
import com.example.matchmovie.components.LoadingScreen
import com.example.matchmovie.components.MovieDaoItem
import com.example.matchmovie.database.FilmDAO
import com.example.matchmovie.database.User
import com.example.matchmovie.database.UserMovie
import com.example.matchmovie.database.UserTvSerie
import com.example.matchmovie.enumentity.MovieMood
import com.example.matchmovie.enumentity.displayName
import com.example.matchmovie.network.RetrofitInstance
import com.example.matchmovie.network.dto.UpdateProfileRequestDto
import com.example.matchmovie.ui.theme.MatchMovieBackground
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieAccent
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedButton
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import com.example.matchmovie.ui.theme.MatchMovieSurface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    dao: FilmDAO,
    onOpenStats: () -> Unit,
    onUserUpdated: (User) -> Unit
) {

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var savedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var moviesByMood by remember { mutableStateOf<Map<MovieMood, List<UserMovie>>>(emptyMap()) }
    var topRatedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var recentlyAddedMovies by remember { mutableStateOf<List<UserMovie>>(emptyList()) }
    var savedTvSeries by remember { mutableStateOf<List<UserTvSerie>>(emptyList()) }
    var tvSeriesByMood by remember { mutableStateOf<Map<MovieMood, List<UserTvSerie>>>(emptyMap()) }
    var topRatedTvSeries by remember { mutableStateOf<List<UserTvSerie>>(emptyList()) }
    var recentlyAddedTvSeries by remember { mutableStateOf<List<UserTvSerie>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String>("") }
    var isLoading by remember { mutableStateOf(true) }
    var profileMessage by remember { mutableStateOf("") }
    var isSavingProfile by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var showBioDialog by remember { mutableStateOf(false) }

    fun createProfileImageUri(): Uri {
        val imageDirectory = File(context.filesDir, "profile_images").apply {
            mkdirs()
        }
        val imageFile = File(imageDirectory, "profile_${System.currentTimeMillis()}.jpg")

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }

    LaunchedEffect(user?._id) {
        val currentUser = user ?: return@LaunchedEffect

        isLoading = true
        try {
            val loadedMovies = withContext(Dispatchers.IO) {
                dao.getMoviesByUser(currentUser._id)
            }
            val loadedTvSeries = withContext(Dispatchers.IO) {
                dao.getTvSeriesByUser(currentUser._id)
            }
            savedMovies = loadedMovies
            savedTvSeries = loadedTvSeries

            // Liste separate per mood
            moviesByMood = loadedMovies.groupBy { movie -> movie.mood }
            tvSeriesByMood = loadedTvSeries.groupBy { tvSerie -> tvSerie.mood }

            // Primi 5 film per rating decrescente
            topRatedMovies = loadedMovies
                .sortedWith(
                    compareByDescending<UserMovie> { movie -> movie.userRating }
                        .thenByDescending { movie -> movie._id }
                )
                .take(5)

            // Ultimi 5 film aggiunti: _id cresce con gli insert nel DB locale
            recentlyAddedMovies = loadedMovies
                .sortedByDescending { movie -> movie._id }
                .take(5)

            topRatedTvSeries = loadedTvSeries
                .sortedWith(
                    compareByDescending<UserTvSerie> { tvSerie -> tvSerie.userRating }
                        .thenByDescending { tvSerie -> tvSerie._id }
                )
                .take(5)

            recentlyAddedTvSeries = loadedTvSeries
                .sortedByDescending { tvSerie -> tvSerie._id }
                .take(5)
        } catch (e: Exception) {
            savedMovies = emptyList()
            moviesByMood = emptyMap()
            topRatedMovies = emptyList()
            recentlyAddedMovies = emptyList()
            savedTvSeries = emptyList()
            tvSeriesByMood = emptyMap()
            topRatedTvSeries = emptyList()
            recentlyAddedTvSeries = emptyList()
            errorMessage = "Unable to load local information, please try again"
        } finally {
            isLoading = false
        }
    }

    val averageRating = savedMovies
        .takeIf { movies -> movies.isNotEmpty() }
        ?.map { movie -> movie.userRating }
        ?.average()
        ?.let { rating -> String.format("%.1f/5", rating) }
        ?: "0.0/5"

    var favoriteMood = moviesByMood
        .maxByOrNull { entry -> entry.value.size }
        ?.key
        ?.displayName()
        ?: "-"


    // Se mood è NOT_SPECIFIED, assegno un'apposita stringa
    if (favoriteMood == "NOT_SPECIFIED")
        favoriteMood = "Not Specified"


    val highestRatedMovie = topRatedMovies.firstOrNull()?.title ?: "-"
    val firstAddedMovie = savedMovies.minByOrNull { movie -> movie._id }?.title ?: "-"

    val averageTvSeriesRating = savedTvSeries
        .takeIf { tvSeries -> tvSeries.isNotEmpty() }
        ?.map { tvSerie -> tvSerie.userRating }
        ?.average()
        ?.let { rating -> String.format("%.1f/5", rating) }
        ?: "0.0/5"

    var favoriteTvSeriesMood = tvSeriesByMood
        .maxByOrNull { entry -> entry.value.size }
        ?.key
        ?.displayName()
        ?: "-"

    if (favoriteTvSeriesMood == "NOT_SPECIFIED")
        favoriteTvSeriesMood = "Not Specified"

    val highestRatedTvSeries = topRatedTvSeries.firstOrNull()?.title ?: "-"
    val firstAddedTvSeries = savedTvSeries.minByOrNull { tvSerie -> tvSerie._id }?.title ?: "-"

    fun updateProfile(profileImage: String?, bio: String?) {
        val currentUser = user ?: return
        coroutineScope.launch {
            isSavingProfile = true
            profileMessage = ""
            try {
                val normalizedProfileImage = profileImage?.trim()?.takeIf { it.isNotBlank() }
                val normalizedBio = bio?.trim()?.takeIf { it.isNotBlank() }

                val remoteUser = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.updateCurrentUser(
                        UpdateProfileRequestDto(
                            profileImage = normalizedProfileImage,
                            bio = normalizedBio
                        )
                    )
                }

                val updatedUser = currentUser.copy(
                    name = remoteUser.name.ifBlank { currentUser.name },
                    email = remoteUser.email,
                    profileImage = remoteUser.profileImage,
                    bio = remoteUser.bio
                )

                withContext(Dispatchers.IO) {
                    dao.updateUserProfile(
                        userId = updatedUser._id,
                        profileImage = updatedUser.profileImage,
                        bio = updatedUser.bio
                    )
                }

                onUserUpdated(updatedUser)
                profileMessage = "Profile updated"
                showImageDialog = false
                showBioDialog = false
            } catch (e: Exception) {
                profileMessage = "Unable to update profile, please try again"
            } finally {
                isSavingProfile = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        updateProfile(profileImage = uri.toString(), bio = user?.bio)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { wasSaved ->
        val imageUri = pendingCameraUri
        if (wasSaved && imageUri != null) {
            updateProfile(profileImage = imageUri.toString(), bio = user?.bio)
        }
        pendingCameraUri = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val imageUri = createProfileImageUri()
            pendingCameraUri = imageUri
            cameraLauncher.launch(imageUri)
        } else {
            profileMessage = "Camera permission denied"
            showImageDialog = false
        }
    }

    fun openCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val imageUri = createProfileImageUri()
            pendingCameraUri = imageUri
            cameraLauncher.launch(imageUri)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (showImageDialog) {
        EditProfileImageDialog(
            title = "Edit profile picture",
            onDismiss = { showImageDialog = false },
            onOpenCamera = {
                showImageDialog = false
                openCamera()
            },
            onOpenGallery = {
                showImageDialog = false
                galleryLauncher.launch(arrayOf("image/*"))
            }
        )
    }

    if (showBioDialog) {
        EditProfileDialog(
            title = "Edit bio",
            label = "Bio",
            initialValue = user?.bio.orEmpty(),
            singleLine = false,
            confirmEnabled = !isSavingProfile,
            onDismiss = { showBioDialog = false },
            onConfirm = { bio ->
                updateProfile(profileImage = user?.profileImage, bio = bio)
            }
        )
    }

    if (isLoading) {
        LoadingScreen(
            message = "Loading profile...",
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MatchMovieBackground),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Profile",
                    color = MatchMoviePrimary,
                    style = MaterialTheme.typography.headlineMedium
                )

                Spacer(modifier = Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MatchMoviePrimary,
                                    MatchMovieBackground
                                )
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user?.profileImage.isNullOrBlank()) {
                        AsyncImage(
                            model = user?.profileImage,
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(4.dp, MatchMovieBackground, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(MatchMovieMutedButton)
                                .border(4.dp, MatchMovieBackground, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.name?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = MatchMovieLightText,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { showImageDialog = true },
                        enabled = user != null && !isSavingProfile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatchMoviePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = MatchMovieMutedButton,
                            disabledContentColor = MatchMovieMutedText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit picture")
                    }

                    Button(
                        onClick = { showBioDialog = true },
                        enabled = user != null && !isSavingProfile,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatchMovieMutedButton,
                            contentColor = MatchMovieLightText,
                            disabledContainerColor = MatchMovieMutedButton,
                            disabledContentColor = MatchMovieMutedText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit bio")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = user?.name.orEmpty(),
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = user?.email.orEmpty(),
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodyMedium
                )

                user?.bio?.takeIf { it.isNotBlank() }?.let { bio ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = bio,
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp)
                    )
                } ?: run {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No bio yet",
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                profileMessage.takeIf { it.isNotBlank() }?.let { message ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = message,
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Films with best rating",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleLarge
                )

                topRatedMovies.take(3).chunked(2).forEach { rowMovies ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowMovies.forEach { movie ->
                            MovieDaoItem(
                                movie = movie,
                                onMovieClick = {},
                                onDeleteClick = {},

                                // Imposto il parametro a true, cosi da rendere la Card piu piccola
                                compact = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowMovies.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Text(
                    text = "Statistiche",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressStatCard(
                        label = "Film salvati",
                        count = savedMovies.count(),
                        goal = 20,
                        accent = MatchMoviePrimary,
                        icon = "\uD83C\uDFAC",
                        modifier = Modifier.weight(2f)
                    )
                    MoodStatCard(
                        mood = favoriteMood,
                        accent = MatchMovieAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RatingStatCard(
                        rating = averageRating,
                        accent = MatchMovieSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    HighestRatedStatCard(
                        label = "Rating più alto",
                        title = highestRatedMovie,
                        posterUrl = topRatedMovies.firstOrNull()?.image,
                        accent = MatchMoviePrimary,
                        modifier = Modifier.weight(2f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "TV Series Stats",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ProgressStatCard(
                        label = "Serie salvate",
                        count = savedTvSeries.count(),
                        goal = 8,
                        accent = MatchMovieSecondary,
                        icon = "\uD83D\uDCF1",
                        modifier = Modifier.weight(2f)
                    )
                    MoodStatCard(
                        mood = favoriteTvSeriesMood,
                        accent = MatchMovieAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RatingStatCard(
                        rating = averageTvSeriesRating,
                        accent = MatchMoviePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    HighestRatedStatCard(
                        label = "Rating più alto",
                        title = highestRatedTvSeries,
                        posterUrl = topRatedTvSeries.firstOrNull()?.image,
                        accent = MatchMovieSecondary,
                        modifier = Modifier.weight(2f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // CTA che apre la schermata Wrapped: stesso gradiente dell'hero in StatsScreen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MatchMoviePrimary,
                                    MatchMovieSecondary
                                )
                            )
                        )
                        .clickable(onClick = onOpenStats)
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Your Movie Wrapped",
                                color = MatchMovieLightText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "See your year in film",
                                color = MatchMovieLightText.copy(alpha = 0.85f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            text = "→",
                            color = MatchMovieLightText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        errorMessage.takeIf { it.isNotBlank() }?.let { error ->
            item {
                Text(
                    text = error,
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        item {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatchMovieMutedButton,
                    contentColor = MatchMovieLightText
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
}

@Composable
private fun ProgressStatCard(
    label: String,
    count: Int,
    goal: Int,
    accent: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    val progress = (count.toFloat() / goal).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MatchMovieCard, MatchMovieBackground)))
            .border(1.dp, MatchMovieMutedText.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = count.toString(),
                            color = MatchMovieLightText,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "/$goal",
                            color = MatchMovieMutedText.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                        )
                    }
                }
                Text(
                    text = icon,
                    color = accent,
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(MatchMovieLightText.copy(alpha = 0.08f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(accent)
                )
            }
        }
    }
}

@Composable
private fun MoodStatCard(
    mood: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MatchMovieCard, MatchMovieBackground)))
            .border(1.dp, MatchMovieMutedText.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "\u25C8",
                color = accent,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = mood,
                color = MatchMovieLightText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Mood preferito",
                color = MatchMovieMutedText,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
private fun RatingStatCard(
    rating: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MatchMovieCard, MatchMovieBackground)))
            .border(1.dp, MatchMovieMutedText.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "\u2605",
                color = accent,
                style = MaterialTheme.typography.headlineSmall
            )
            Column {
                Text(
                    text = rating,
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Voto medio",
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun HighestRatedStatCard(
    label: String,
    title: String,
    posterUrl: String?,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(MatchMovieCard, MatchMovieBackground)))
            .border(1.dp, MatchMovieMutedText.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 80.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.4f), MatchMovieBackground)))
                    .border(1.dp, MatchMovieLightText.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            ) {
                posterUrl?.takeIf { it.isNotBlank() }?.let { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MatchMovieMutedText,
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = title,
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EditProfileImageDialog(
    title: String,
    onDismiss: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenGallery: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MatchMovieSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onOpenCamera,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatchMoviePrimary,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open camera", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenGallery,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MatchMovieMutedButton,
                        contentColor = MatchMovieLightText
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open gallery", fontWeight = FontWeight.SemiBold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        color = MatchMovieMutedText,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileDialog(
    title: String,
    label: String,
    initialValue: String,
    singleLine: Boolean,
    confirmEnabled: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MatchMovieSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text(label) },
                    singleLine = singleLine,
                    minLines = if (singleLine) 1 else 4,
                    maxLines = if (singleLine) 1 else 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MatchMovieLightText,
                        unfocusedTextColor = MatchMovieLightText,
                        focusedBorderColor = MatchMoviePrimary,
                        unfocusedBorderColor = MatchMovieMutedButton,
                        focusedLabelColor = MatchMoviePrimary,
                        unfocusedLabelColor = MatchMovieMutedText,
                        cursorColor = MatchMoviePrimary,
                        focusedContainerColor = MatchMovieCard,
                        unfocusedContainerColor = MatchMovieCard
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Cancel",
                            color = MatchMovieMutedText,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { onConfirm(value) },
                        enabled = confirmEnabled,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatchMoviePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = MatchMovieMutedButton,
                            disabledContentColor = MatchMovieMutedText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
