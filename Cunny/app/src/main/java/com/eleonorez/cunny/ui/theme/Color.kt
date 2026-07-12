package com.eleonorez.cunny.ui.theme

import com.eleonorez.cunny.ui.theme.CunnyColors

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CunnyColorPalette(
    val primary: Color,
    val primaryLight: Color,
    val primaryPale: Color,
    val primaryShadow: Color,
    val accentOrange: Color,
    val accentGreen: Color,
    val successSoft: Color,
    val accentRed: Color,
    val accentYellow: Color,
    val background: Color,
    val backgroundSoft: Color,
    val backgroundWarm: Color,
    val textDark: Color,
    val textBody: Color,
    val textSubtle: Color,
    val border: Color,
    val borderLight: Color,
    val glassBg: Color,
    val glassBorder: Color,
    val textOnDarkSurface: Color,
    val textOnPrimary: Color,
    val tactileShadow: Color,
    val illustGradient: List<Color>,
    val gradPlum: List<Color>,
    val gradPlumSoft: List<Color>,
    val gradHeroDark: List<Color>,
    val gradNavBar: List<Color>,
    val gradNavActive: List<Color>,
    val gradCanvas: List<Color>,
    val gradProgress: List<Color>,
    val gradGold: List<Color>
)

val LightCunnyColors = CunnyColorPalette(
    primary = Color(0xFF6C5CE7),            // Cool Violet primary
    primaryLight = Color(0xFF8B7FF2),        // Lighter violet
    primaryPale = Color(0xFFEDE9FF),         // Very pale violet bg
    primaryShadow = Color(0xFF4A3FB5),       // Deep violet shadow
    accentOrange = Color(0xFFD97706),        // Warm Amber (hint/tip accent)
    accentGreen = Color(0xFF4A6B58),
    successSoft = Color(0xFFE8EDE9),
    accentRed = Color(0xFFB42318),
    accentYellow = Color(0xFFF59E0B),         // Bright amber (icon tint)
    background = Color(0xFFFBF9FD),          // Slight violet tint
    backgroundSoft = Color(0xFFF0ECF8),      // Soft violet wash
    backgroundWarm = Color(0xFFE8E3F4),      // Warm violet surface
    textDark = Color(0xFF1A1830),            // Deep navy-violet
    textBody = Color(0xFF44415A),            // Muted violet-gray
    textSubtle = Color(0xFF8884A0),          // Subtle violet-gray
    border = Color(0xFFD8D4E8),             // Light violet border
    borderLight = Color(0xFFEBE8F4),         // Very light violet border
    glassBg = Color(0xFFFFFEFF).copy(alpha = 0.28f),
    glassBorder = Color(0xFFF0ECFF).copy(alpha = 0.58f),
    textOnDarkSurface = Color(0xFFFFFFFF),
    textOnPrimary = Color(0xFFFFFFFF),       // White on violet gradient
    tactileShadow = Color(0xFFCCC8DE),       // Violet-tinted shadow
    illustGradient = listOf(Color(0xFFEDE9FF), Color(0xFFE4E0F8)),
    gradPlum = listOf(Color(0xFF8B7FF2), Color(0xFF6C5CE7), Color(0xFF4A3FB5)),
    gradPlumSoft = listOf(Color(0xFFFFFFFF), Color(0xFFFAF8FF), Color(0xFFFFFFFF)),
    gradHeroDark = listOf(Color(0xFF24223A), Color(0xFF1A1830), Color(0xFF2C2846)),
    gradNavBar = listOf(Color(0xFF24223A), Color(0xFF1A1830), Color(0xFF2C2846)),
    gradNavActive = listOf(Color(0xFF36324E), Color(0xFF2E2A44), Color(0xFF3E3A58)),
    gradCanvas = listOf(Color(0xFFE8E3F4), Color(0xFFDFDAEE), Color(0xFFD5D0E6)),
    gradProgress = listOf(Color(0xFFB3ABFA), Color(0xFF8B7FF2)),
    gradGold = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFB45309))
)

val DarkCunnyColors = CunnyColorPalette(
    primary = Color(0xFFA29BFE),             // Soft periwinkle
    primaryLight = Color(0xFFBDB8FF),        // Lighter periwinkle
    primaryPale = Color(0xFF262040),         // Dark violet container
    primaryShadow = Color(0xFF0D0B18),       // Deep violet-black
    accentOrange = Color(0xFFFBBF24),         // Light amber for dark bg
    accentGreen = Color(0xFF8CC4A4),
    successSoft = Color(0xFF1D2B22),
    accentRed = Color(0xFFEA7B74),
    accentYellow = Color(0xFFFCD34D),          // Bright yellow-amber for dark
    background = Color(0xFF110F1A),          // Deep violet-black
    backgroundSoft = Color(0xFF1A1828),      // Dark slate violet
    backgroundWarm = Color(0xFF221F30),      // Rich dark violet
    textDark = Color(0xFFFFFFFF),            // Pure white
    textBody = Color(0xFFE0DEFF),            // Lavender white body
    textSubtle = Color(0xFFC0BCD4),          // Brightened violet-gray
    border = Color(0xFF3A3650),              // Violet-charcoal
    borderLight = Color(0xFFA29BFE).copy(alpha = 0.08f),
    glassBg = Color(0xFF1A1828).copy(alpha = 0.65f),
    glassBorder = Color(0xFFA29BFE).copy(alpha = 0.15f),
    textOnDarkSurface = Color(0xFFFFFFFF),
    textOnPrimary = Color(0xFF110F1A),       // Dark bg on light violet gradient
    tactileShadow = Color(0xFF0D0B18),
    illustGradient = listOf(Color(0xFF262040), Color(0xFF221F30)),
    gradPlum = listOf(Color(0xFFBDB8FF), Color(0xFFA29BFE), Color(0xFF8680E6)),
    gradPlumSoft = listOf(Color(0xFF1A1828), Color(0xFF110F1A), Color(0xFF1A1828)),
    gradHeroDark = listOf(Color(0xFF1A1828), Color(0xFF110F1A), Color(0xFF221F30)),
    gradNavBar = listOf(Color(0xFF1A1828), Color(0xFF110F1A), Color(0xFF221F30)),
    gradNavActive = listOf(Color(0xFF262040), Color(0xFF221F30), Color(0xFF302B48)),
    gradCanvas = listOf(Color(0xFF110F1A), Color(0xFF1A1828), Color(0xFF0D0B18)),
    gradProgress = listOf(Color(0xFFA29BFE), Color(0xFFBDB8FF)),
    gradGold = listOf(Color(0xFFFCD34D), Color(0xFFFBBF24), Color(0xFFF59E0B))
)

val LocalCunnyColors = staticCompositionLocalOf { LightCunnyColors }

object CunnyColors {
    val primary: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.primary
    val primaryLight: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.primaryLight
    val primaryPale: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.primaryPale
    val primaryShadow: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.primaryShadow
    val accentOrange: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.accentOrange
    val accentGreen: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.accentGreen
    val successSoft: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.successSoft
    val accentRed: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.accentRed
    val accentYellow: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.accentYellow
    val background: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.background
    val backgroundSoft: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.backgroundSoft
    val backgroundWarm: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.backgroundWarm
    val textDark: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.textDark
    val textBody: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.textBody
    val textSubtle: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.textSubtle
    val border: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.border
    val borderLight: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.borderLight
    val glassBg: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.glassBg
    val glassBorder: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.glassBorder
    val textOnDarkSurface: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.textOnDarkSurface
    val textOnPrimary: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.textOnPrimary
    val tactileShadow: Color @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.tactileShadow
    val illustGradient: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.illustGradient
    val gradPlum: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradPlum
    val gradPlumSoft: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradPlumSoft
    val gradHeroDark: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradHeroDark
    val gradNavBar: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradNavBar
    val gradNavActive: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradNavActive
    val gradCanvas: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradCanvas
    val gradProgress: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradProgress
    val gradGold: List<Color> @Composable @ReadOnlyComposable get() = LocalCunnyColors.current.gradGold
}
