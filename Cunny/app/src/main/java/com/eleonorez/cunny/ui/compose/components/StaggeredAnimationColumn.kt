package com.eleonorez.cunny.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun StaggeredItem(
    index: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    staggerDelayMs: Int = 60,
    itemDurationMs: Int = 400,
    slideDistanceDp: Int = 24,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val delayMs = index * staggerDelayMs
    
    AnimatedVisibility(
        visible = isVisible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = itemDurationMs,
                delayMillis = delayMs,
                easing = EaseOutCubic
            )
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = itemDurationMs,
                delayMillis = delayMs,
                easing = EaseOutCubic
            )
        ) { with(density) { slideDistanceDp.dp.roundToPx() } }
    ) {
        content()
    }
}
