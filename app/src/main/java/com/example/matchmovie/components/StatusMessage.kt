package com.example.matchmovie.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.matchmovie.ui.theme.MatchMovieMutedText

@Composable
fun StatusMessage(message: String, modifier: Modifier = Modifier) {
    Box(

        // Centro il testo informativo nella schermata
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MatchMovieMutedText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}