package com.example.matchmovie.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.matchmovie.ui.theme.MatchMovieLightText
import com.example.matchmovie.ui.theme.MatchMovieMutedButton
import com.example.matchmovie.ui.theme.MatchMovieMutedText
import com.example.matchmovie.ui.theme.MatchMoviePrimary
import com.example.matchmovie.ui.theme.MatchMovieSecondary
import com.example.matchmovie.ui.theme.MatchMovieSurface

@Composable
fun RatingDialog(
    movieTitle: String?,
    rating: Int,
    onRatingChange: (Int) -> Unit,
    onConfirm: () -> Unit,
    onSkip: () -> Unit,
    onDismiss: () -> Unit
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "♥",
                    color = MatchMoviePrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Rate this movie",
                    color = MatchMovieLightText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (movieTitle != null) {
                    Text(
                        text = movieTitle,
                        color = MatchMovieMutedText,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                StarRatingSelector(
                    rating = rating,
                    onRatingSelected = onRatingChange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onSkip,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatchMovieMutedButton,
                            contentColor = MatchMovieLightText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Skip", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onConfirm,
                        enabled = rating > 0,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MatchMoviePrimary,
                            contentColor = Color.White,
                            disabledContainerColor = MatchMovieMutedButton,
                            disabledContentColor = MatchMovieMutedText
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun StarRatingSelector(
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { starValue ->
            val isSelected = starValue <= rating
            Text(
                text = "★",
                color = if (isSelected) MatchMovieSecondary else Color(0xFF4A5663),
                fontSize = 40.sp,
                lineHeight = 44.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onRatingSelected(starValue) }
                    .padding(horizontal = 2.dp, vertical = 4.dp)
            )
        }
    }
}