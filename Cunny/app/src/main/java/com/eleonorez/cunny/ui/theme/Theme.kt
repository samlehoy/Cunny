package com.eleonorez.cunny.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6C5CE7),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE9FF),
    onPrimaryContainer = Color(0xFF1A1830),
    secondary = Color(0xFFA68B52),
    onSecondary = Color.White,
    tertiary = Color(0xFF4A6B58),
    onTertiary = Color.White,
    background = Color(0xFFFBF9FD),
    onBackground = Color(0xFF1A1830),
    surface = Color(0xFFF0ECF8),
    onSurface = Color(0xFF1A1830),
    surfaceVariant = Color(0xFFE8E3F4),
    onSurfaceVariant = Color(0xFF44415A),
    outline = Color(0xFFD8D4E8),
    outlineVariant = Color(0xFFEBE8F4)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA29BFE),
    onPrimary = Color(0xFF110F1A),
    primaryContainer = Color(0xFF262040),
    onPrimaryContainer = Color(0xFFA29BFE),
    secondary = Color(0xFFE4C690),
    onSecondary = Color(0xFF110F1A),
    tertiary = Color(0xFF8CC4A4),
    onTertiary = Color(0xFF110F1A),
    background = Color(0xFF110F1A),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1A1828),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF221F30),
    onSurfaceVariant = Color(0xFFA29BFE),
    outline = Color(0xFF3A3650),
    outlineVariant = Color(0xFF2C2840)
)

@Composable
fun CunnyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val cunnyColors = if (darkTheme) DarkCunnyColors else LightCunnyColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            
            val decorView = window.decorView
            val wic = androidx.core.view.WindowCompat.getInsetsController(window, decorView)
            wic.isAppearanceLightStatusBars = !darkTheme
            wic.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    val typography = androidx.compose.runtime.remember(cunnyColors) {
        getTypography(cunnyColors)
    }

    CompositionLocalProvider(LocalCunnyColors provides cunnyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
