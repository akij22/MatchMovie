package com.example.matchmovie.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchmovie.R
import com.example.matchmovie.ui.theme.MatchMovieMutedText

@Composable
fun InfoMessage(imageRes: Int, message: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        
        //Se l'utente non ha film salvati, mostro l'immagine "di fallback"
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Ask for your next movie recommendation",
            modifier = Modifier
                .size(200.dp)
        )
        Text(
            text = message,
            color = MatchMovieMutedText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}