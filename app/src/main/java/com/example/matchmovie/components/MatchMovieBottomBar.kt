package com.example.matchmovie.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.matchmovie.R
import com.example.matchmovie.enumentity.Screen
import com.example.matchmovie.ui.theme.MatchMovieCard
import com.example.matchmovie.ui.theme.MatchMovieMutedText


// Data class di supporto per la bottom bar
// per rappresentare un singolo elemento nella bottom bar
private data class BottomBarItem(
    val screen: Screen,
    val label: String,
    val iconRes: Int
)


// Composable per la creazione di una bottom bar
@Composable
fun MatchMovieBottomBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit
) {

    // Definisco la lista di elementi che compongono la bottom bar
    val items = listOf(
        BottomBarItem(Screen.HomeScreen, "Home", R.drawable.home),
        BottomBarItem(Screen.ExploreScreen, "Explore", R.drawable.explore),
        BottomBarItem(Screen.MyListScreen, "MyList", R.drawable.mylist),
        BottomBarItem(Screen.ChatScreen, "AI Chat", R.drawable.ai),
        BottomBarItem(Screen.ProfileScreen, "Profile", R.drawable.user)
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
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
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