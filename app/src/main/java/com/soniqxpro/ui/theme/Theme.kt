package com.soniqxpro.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.soniqxpro.model.AppSettings
import com.soniqxpro.model.ThemeMode

val LocalAccent = staticCompositionLocalOf { AccentPalettes[0] }

@Composable
fun accentBrush(alpha: Float = 1f): Brush {
    val a = LocalAccent.current
    return Brush.linearGradient(listOf(a.primary.copy(alpha = alpha), a.secondary.copy(alpha = alpha)))
}

@Composable
fun SoniqTheme(settings: AppSettings, content: @Composable () -> Unit) {
    val dark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val accent = AccentPalettes[settings.accentIndex.coerceIn(0, AccentPalettes.lastIndex)]
    val scheme = if (dark) {
        darkColorScheme(
            primary = accent.primary,
            onPrimary = Color.White,
            secondary = accent.secondary,
            onSecondary = Color.White,
            background = SoniqBlack,
            onBackground = Color(0xFFF2F2F7),
            surface = Charcoal,
            onSurface = Color(0xFFF2F2F7),
            surfaceVariant = SurfaceHigh,
            onSurfaceVariant = Color(0xFFA6A6B5),
            outline = SurfaceHighest
        )
    } else {
        lightColorScheme(
            primary = accent.primary,
            onPrimary = Color.White,
            secondary = accent.secondary,
            onSecondary = Color.White,
            background = Color(0xFFF7F7FB),
            onBackground = Color(0xFF14141A),
            surface = Color.White,
            onSurface = Color(0xFF14141A),
            surfaceVariant = Color(0xFFECECF3),
            onSurfaceVariant = Color(0xFF5B5B6B)
        )
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }
        }
    }
    CompositionLocalProvider(LocalAccent provides accent) {
        MaterialTheme(colorScheme = scheme, typography = SoniqTypography, content = content)
    }
}
