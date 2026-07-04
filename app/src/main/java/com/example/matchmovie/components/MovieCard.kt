package com.example.matchmovie.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.matchmovie.enumentity.displayName
import com.example.matchmovie.network.dto.SingleMovieResultDto
import kotlinx.coroutines.launch

@Composable
fun MovieCard (
    movie: SingleMovieResultDto,
    onMovieSelected: suspend (SingleMovieResultDto) -> Unit,
    showReleaseDateBadge: Boolean = false,
    enableSelection: Boolean = true
) {
    val coroutineScope = rememberCoroutineScope()


    val posterUrl = movie.poster_path?.let { "https://image.tmdb.org/t/p/w500$it" }
    val releaseYear = movie.release_date
        ?.takeIf { it.length >= 4 }
        ?.take(4)
        ?: "N/A"

    val cardModifier = if (enableSelection) {
        Modifier
            .width(160.dp)
            .clickable {
                coroutineScope.launch {

                    // Al click della card del film, carico il cast e regista e passo alla schermata `FilmDetailsScreen`
                    onMovieSelected(movie)
                }
            }
    } else {
        Modifier.width(160.dp)
    }

    Column(
        modifier = cardModifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF182029))
                .border(
                    width = 1.dp,
                    color = Color(0xFF2E363E),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            AsyncImage(
                model = posterUrl,
                contentDescription = "Poster ${movie.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x66101820),
                                Color(0xF0101820)
                            )
                        )
                    )
            )

            if (showReleaseDateBadge) {
                ReleaseDateBadge(
                    releaseDate = movie.release_date,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            } else {
                RatingBadge(
                    rating = movie.vote_average,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = movie.title,
            color = Color(0xFFF7F9FC),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(

            // Mostro il banner "in arrivo" solo se si tratta della lista upComingFilms
            text = if (showReleaseDateBadge) {
                "${movie.mood.displayName()} • Coming soon"
            } else {
                "${movie.mood.displayName()} • $releaseYear"
            },
            color = Color(0xFFE1BEBF),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RatingBadge(
    rating: Double,
    modifier: Modifier = Modifier
) {
    Text(
        text = "★ ${String.format("%.1f", rating)}",
        color = Color(0xFFF7F9FC),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCC182029))
            .border(
                width = 1.dp,
                color = Color(0x332E363E),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

@Composable
private fun ReleaseDateBadge(
    releaseDate: String?,
    modifier: Modifier = Modifier
) {
    Text(
        text = formatReleaseMonthYear(releaseDate),
        color = Color(0xFF68001A),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xEEFA576B))
            .border(
                width = 1.dp,
                color = Color(0x33FFB3B6),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    )
}

private fun formatReleaseMonthYear(releaseDate: String?): String {
    if (releaseDate.isNullOrBlank()) return "TBA"

    val dateParts = releaseDate.split("-")
    if (dateParts.size < 2) return "TBA"

    val month = when (dateParts[1]) {
        "01" -> "GEN"
        "02" -> "FEB"
        "03" -> "MAR"
        "04" -> "APR"
        "05" -> "MAG"
        "06" -> "GIU"
        "07" -> "LUG"
        "08" -> "AGO"
        "09" -> "SET"
        "10" -> "OTT"
        "11" -> "NOV"
        "12" -> "DIC"
        else -> return "TBA"
    }

    return "$month ${dateParts[0]}"
}
