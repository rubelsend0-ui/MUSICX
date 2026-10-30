package com.musicx.player.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF101018)
val ElectricBlue = Color(0xFF00E5FF)
val PurpleAccent = Color(0xFFB388FF)
val CardBackground = Color(0x33222238)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9E9EAF)

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = PurpleAccent,
    background = DeepBlack,
    surface = DarkSurface,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun MusicXTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
