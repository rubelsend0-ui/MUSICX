package com.soniqxpro.ui.theme

import androidx.compose.ui.graphics.Color

val SoniqBlack = Color(0xFF050507)
val Charcoal = Color(0xFF111116)
val SurfaceHigh = Color(0xFF1A1A22)
val SurfaceHighest = Color(0xFF23232D)
val ElectricBlue = Color(0xFF2D7BFF)
val Violet = Color(0xFF8B5CF6)

data class AccentPalette(val name: String, val primary: Color, val secondary: Color)

val AccentPalettes = listOf(
    AccentPalette("Electric", ElectricBlue, Violet),
    AccentPalette("Magenta", Color(0xFFFF3D9A), Violet),
    AccentPalette("Emerald", Color(0xFF10D9A0), ElectricBlue),
    AccentPalette("Amber", Color(0xFFFFB020), Color(0xFFFF5C5C))
)
