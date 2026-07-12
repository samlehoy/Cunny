package com.eleonorez.cunny.ui.compose.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.eleonorez.cunny.ui.theme.CunnyColors

/**
 * Shimmer placeholder effect using the warm plum palette.
 * Enhanced with wider sweep, smoother easing, and optional pulse.
 */
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1800
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val shimmerBase = if (isDark) CunnyColors.backgroundWarm else Color(0xFFEDE9FF) // soft lavender / dark warm plum
    val shimmerMid = if (isDark) CunnyColors.primaryPale.copy(alpha = 0.5f) else Color(0xFFF0ECFF) // lighter mid / selected highlight
    val shimmerHighlight = if (isDark) CunnyColors.primaryPale else Color(0xFFFBF9FD) // near-white highlight / selected highlight

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    // Subtle alpha pulse for breathing effect
    val alphaAnim by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = EaseInOutCubic
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    val brush = Brush.linearGradient(
        colors = listOf(
            shimmerBase,
            shimmerMid,
            shimmerHighlight,
            shimmerMid,
            shimmerBase
        ),
        start = Offset(translateAnim - 400f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Spacer(
        modifier = modifier
            .graphicsLayer { alpha = alphaAnim }
            .background(brush)
    )
}

/**
 * Enhanced shimmer with pulse scale animation — premium loading feel.
 * Use for hero/featured skeleton areas.
 */
@Composable
fun PulsingShimmerEffect(
    modifier: Modifier = Modifier,
    durationMillis: Int = 2000
) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val shimmerBase = if (isDark) CunnyColors.backgroundWarm else Color(0xFFEDE9FF)
    val shimmerHighlight = if (isDark) CunnyColors.primaryPale else Color(0xFFFBF9FD)

    val transition = rememberInfiniteTransition(label = "pulseShimmer")

    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseTranslate"
    )

    val scaleAnim by transition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val alphaAnim by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val brush = Brush.linearGradient(
        colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
        start = Offset(translateAnim - 400f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Spacer(
        modifier = modifier
            .graphicsLayer {
                scaleX = scaleAnim
                scaleY = scaleAnim
                alpha = alphaAnim
            }
            .background(brush)
    )
}
