package com.example.matchmovie.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.matchmovie.ui.theme.MatchMoviePrimary


// Composable per il nome dell'app da inserire in cima ad ogni schermata
@Composable
fun MatchMovieTitle(modifier: Modifier = Modifier) {
    Text(
        text = "MatchMovie",
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        color = MatchMoviePrimary,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
}
