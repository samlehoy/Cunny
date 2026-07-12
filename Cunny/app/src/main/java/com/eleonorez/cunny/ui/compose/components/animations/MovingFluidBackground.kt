package com.eleonorez.cunny.ui.compose.components.animations

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eleonorez.cunny.ui.theme.CunnyColors

@Composable
fun MovingFluidBackground(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "fluid")
    
    // Animasi putaran sudut kontinu (0 - 360 derajat)
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing) // Dipercepat dari 16s menjadi 10s agar lebih dinamis/agresif
        ),
        label = "angle"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CunnyColors.background)
    ) {
        val isDark = androidx.compose.foundation.isSystemInDarkTheme()
        val washAlpha = if (isDark) 0.08f else 0.55f
        val radialAlpha = if (isDark) 0.06f else 0.18f
        val orbAlpha = if (isDark) 0.12f else 0.42f

        val primaryColor = CunnyColors.primary
        val primaryLightColor = CunnyColors.primaryLight

        // Draw the top wash on a Canvas (not blurred)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Linear Gradient Wash (bleeds from top to middle)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = washAlpha),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = h * 0.42f
                )
            )
            
            // Radial Ellipse Gradient at top center
            val ellipseWidth = w * 0.90f
            val ellipseHeight = h * 0.45f
            val radius = maxOf(ellipseWidth, ellipseHeight) * 0.5f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryLightColor.copy(alpha = radialAlpha),
                        Color.Transparent
                    ),
                    center = Offset(w / 2f, 0f),
                    radius = radius * 0.60f
                ),
                center = Offset(w / 2f, 0f),
                radius = radius * 0.60f
            )
        }

        // Draw the moving fluid orbs inside a blurred Box so they don't blur the base gradients!
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp) // Perbesar blur dari 64.dp menjadi 80.dp agar orbs besar membaur halus
        ) {
            // Bola Plum 1 - Di belakang Status Strip (TopCenter)
            Box(
                modifier = Modifier
                    .size(380.dp) // Perbesar dari 260.dp
                    .align(Alignment.TopCenter)
                    .offset(
                        x = (120 * kotlin.math.cos(Math.toRadians(angle.toDouble()))).dp, // Perbesar ayunan dari 40
                        y = (-100 + 90 * kotlin.math.sin(Math.toRadians(angle.toDouble()))).dp // Perbesar ayunan dari 30
                    )
                    .background(CunnyColors.primaryLight.copy(alpha = orbAlpha), shape = CircleShape)
            )

            // Bola Plum 2 - Di belakang Carousel Card (Center)
            Box(
                modifier = Modifier
                    .size(450.dp) // Perbesar dari 300.dp
                    .align(Alignment.Center)
                    .offset(
                        x = (150 * kotlin.math.cos(Math.toRadians((angle + 120).toDouble()))).dp, // Perbesar ayunan dari 50
                        y = (-90 + 110 * kotlin.math.sin(Math.toRadians((angle + 120).toDouble()))).dp // Perbesar ayunan dari 40
                    )
                    .background(CunnyColors.primary.copy(alpha = orbAlpha), shape = CircleShape)
            )

            // Bola Plum 3 - Di belakang Active Lesson Card (BottomCenter)
            Box(
                modifier = Modifier
                    .size(420.dp) // Perbesar dari 280.dp
                    .align(Alignment.BottomCenter)
                    .offset(
                        x = (130 * kotlin.math.sin(Math.toRadians((angle + 240).toDouble()))).dp, // Perbesar ayunan dari 60
                        y = (-200 + 100 * kotlin.math.cos(Math.toRadians((angle + 240).toDouble()))).dp // Perbesar ayunan dari 30
                    )
                    .background(
                        if (isDark) CunnyColors.primaryLight.copy(alpha = orbAlpha * 0.5f)
                        else CunnyColors.primaryPale.copy(alpha = orbAlpha), 
                        shape = CircleShape
                    )
            )
        }
    }
}
