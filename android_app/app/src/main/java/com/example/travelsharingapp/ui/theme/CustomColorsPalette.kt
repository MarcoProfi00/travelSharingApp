package com.example.travelsharingapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val MaterialTheme.customColorsPalette: CustomColorsPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColorsPalette.current

@Immutable
data class CustomColorsPalette(
    val extraColorRed: Color = Color.Unspecified,
    val extraColorOrange: Color = Color.Unspecified,
    val extraColorGreen: Color = Color.Unspecified,
    val favoriteButtonColor: Color = Color.Unspecified,
)

val LightExtraColorRed = Color(color = 0xFFB22222)
val LightExtraColorOrange = Color(color = 0xFFFF8C00)
val LightExtraColorGreen = Color(color = 0xFF228B22)
val LightFavoriteButtonColor = Color(color = 0xFFF44336)

val DarkExtraColorRed = Color(color = 0xFFB22222)
val DarkExtraColorOrange = Color(color = 0xFFFF8C00)
val DarkExtraColorGreen = Color(color = 0xFF228B22)
val DarkFavoriteButtonColor = Color(color = 0xFFF44336)


val LightCustomColorsPalette = CustomColorsPalette(
    extraColorRed = LightExtraColorRed,
    extraColorOrange = LightExtraColorOrange,
    extraColorGreen = LightExtraColorGreen,
    favoriteButtonColor = LightFavoriteButtonColor
)

val DarkCustomColorsPalette = CustomColorsPalette(
    extraColorRed = DarkExtraColorRed,
    extraColorOrange = DarkExtraColorOrange,
    extraColorGreen = DarkExtraColorGreen,
    favoriteButtonColor = DarkFavoriteButtonColor
)

val LocalCustomColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }