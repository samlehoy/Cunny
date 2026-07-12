package com.eleonorez.cunny.ui.compose.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import kotlin.math.roundToInt

/**
 * Text that appears character-by-character with a typewriter effect.
 * 
 * @param text The full text to reveal
 * @param isPlaying When true, starts the typewriter animation
 * @param charDelayMs Delay between each character (default 30ms)
 * @param onComplete Called when all characters have been revealed
 */
@Composable
fun TypewriterText(
    text: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    charDelayMs: Int = 30,
    fontFamily: FontFamily = DmSansFontFamily,
    fontWeight: FontWeight = FontWeight.Normal,
    fontSize: TextUnit = 15.sp,
    color: Color = CunnyColors.textBody,
    textAlign: TextAlign = TextAlign.Center,
    lineHeight: TextUnit = 22.sp,
    onComplete: () -> Unit = {}
) {
    var displayedText by remember(text) { mutableStateOf("") }
    val progress = remember(text) { Animatable(0f) }
    
    LaunchedEffect(text, isPlaying) {
        if (isPlaying && text.isNotEmpty()) {
            progress.snapTo(0f)
            displayedText = ""
            val totalDuration = text.length * charDelayMs
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = totalDuration,
                    easing = LinearEasing
                )
            )
            onComplete()
        } else if (!isPlaying) {
            // Show full text immediately when not playing
            displayedText = text
        }
    }
    
    // Update displayed text based on progress
    val charCount = (progress.value * text.length).roundToInt().coerceIn(0, text.length)
    displayedText = text.substring(0, charCount)
    
    Text(
        text = displayedText,
        modifier = modifier,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize,
        color = color,
        textAlign = textAlign,
        lineHeight = lineHeight
    )
}
