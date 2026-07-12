package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.launch

@Composable
fun RewardTrainerWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var dogActionText by remember { mutableStateOf("Kelinci menunggu perintah...") }
    var dogEmoji by remember { mutableStateOf("🐰") }
    var isSuccess by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val animProgress = remember { Animatable(0f) }
    var hurdleRotation by remember { mutableStateOf(0f) }
    var dogRotation by remember { mutableStateOf(0f) }
    var maxHeight by remember { mutableStateOf(60) } // 25 for low, 60 for medium, 100 for high
    var isAnimating by remember { mutableStateOf(false) }

    // Reinforcement Learning Exploration States
    var triedLow by remember { mutableStateOf(false) }
    var triedMedium by remember { mutableStateOf(false) }
    var triedHigh by remember { mutableStateOf(false) }

    val startX = (-100).dp
    val endX = 100.dp

    val currentX = startX + (endX - startX) * animProgress.value
    // Parabolic equation: y = -4 * maxHeight * t * (1 - t)
    val currentY = (-4 * maxHeight * animProgress.value * (1 - animProgress.value)).dp

    // Completed when all actions have been explored/learned by the AI
    val explorationComplete = triedLow && triedMedium && triedHigh

    LaunchedEffect(explorationComplete) {
        if (explorationComplete) {
            isSuccess = true
            dogActionText = "Latihan Selesai! Kelinci paham taktik terbaik adalah Lompat Sedang (Reward tertinggi)."
            onWidgetCompleted(true)
        }
    }

    val outerShape = RoundedCornerShape(CunnyDimens.radiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 4.dp)
            .background(
                color = CunnyColors.tactileShadow, // Solid 3D base shadow
                shape = outerShape
            )
    ) {
        GlassSurface(
            shape = outerShape,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Lab Pembelajaran Penguatan (RL)",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Latih kelinci agar melompati rintangan dengan tinggi yang pas untuk mendapatkan skor Reward maksimal.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Beautiful interactive arena
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(Color(0xFFF0ECF8), shape = RoundedCornerShape(CunnyDimens.radiusMd))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Ground line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFFE0DCF0))
                            .align(Alignment.BottomCenter)
                    )

                    // Hurdle / Obstacle (placed in center, height 40.dp)
                    val animatedHurdleRotation by animateFloatAsState(targetValue = hurdleRotation, label = "hurdleRotation")
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(x = 0.dp, y = 0.dp)
                            .graphicsLayer {
                                rotationZ = animatedHurdleRotation
                                transformOrigin = TransformOrigin(0.5f, 1f) // rotate from bottom center
                            }
                            .width(8.dp)
                            .height(42.dp)
                            .background(Color(0xFF8B5A2B), shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    ) {
                        // Crossbar detail
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(Color(0xFFD2691E))
                                .align(Alignment.TopCenter)
                        )
                    }

                    // Obstacle height text indicator
                    Text(
                        text = "Rintangan (40cm)",
                        fontFamily = SoraFontFamily,
                        fontSize = 9.sp,
                        color = Color(0xFF8B5A2B),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-45).dp)
                    )

                    // Rabbit emoji actor animating its parabolic jump
                    val animatedDogRotation by animateFloatAsState(targetValue = dogRotation, label = "dogRotation")
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(x = currentX, y = currentY)
                            .graphicsLayer {
                                rotationZ = animatedDogRotation
                            }
                    ) {
                        Text(
                            text = dogEmoji,
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Kelinci Memory / Q-Table (Visual representation of AI learning)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFBF9FD), shape = RoundedCornerShape(CunnyDimens.radiusSm))
                        .border(1.dp, Color(0xFFE0DCF0), shape = RoundedCornerShape(CunnyDimens.radiusSm))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🧠 Memori Pembelajaran Kelinci (Q-Table)",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CunnyColors.textDark,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    
                    QTableRow(
                        actionName = "Lompat Rendah (20cm)",
                        statusText = if (triedLow) "Menabrak (Penalti: -10 XP)" else "Belum dicoba",
                        isTried = triedLow,
                        color = if (triedLow) Color(0xFFA84860) else CunnyColors.textSubtle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    QTableRow(
                        actionName = "Lompat Sedang (60cm)",
                        statusText = if (triedMedium) "Sempurna (Reward: +50 XP)" else "Belum dicoba",
                        isTried = triedMedium,
                        color = if (triedMedium) CunnyColors.primary else CunnyColors.textSubtle
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    QTableRow(
                        actionName = "Lompat Tinggi (100cm)",
                        statusText = if (triedHigh) "Terlalu Tinggi (Reward: +10 XP)" else "Belum dicoba",
                        isTried = triedHigh,
                        color = if (triedHigh) CunnyColors.accentYellow else CunnyColors.textSubtle
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Total Reward: $score XP",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CunnyColors.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dogActionText,
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textBody,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 36.dp) // Grow naturally if wrapped, preventing clipping
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CunnyOutlineButton(
                        text = "Lompat Rendah",
                        onClick = {
                            if (isAnimating) return@CunnyOutlineButton
                            isAnimating = true
                            maxHeight = 20
                            scope.launch {
                                // Reset
                                animProgress.snapTo(0f)
                                dogEmoji = "🐰"
                                hurdleRotation = 0f
                                dogRotation = 0f
                                
                                // Jump to obstacle center (t = 0.5)
                                animProgress.animateTo(
                                    targetValue = 0.5f,
                                    animationSpec = tween(durationMillis = 400, easing = LinearEasing)
                                )
                                
                                // Crash!
                                dogRotation = 90f // Tilt the rabbit to show fall
                                hurdleRotation = 90f // Tilt the obstacle
                                score -= 10
                                dogActionText = "Lompatan terlalu rendah! Menabrak rintangan. (Penalti: -10 XP)"
                                triedLow = true
                                isAnimating = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CunnyOutlineButton(
                        text = "Lompat Sedang",
                        onClick = {
                            if (isAnimating) return@CunnyOutlineButton
                            isAnimating = true
                            maxHeight = 60
                            scope.launch {
                                // Reset
                                animProgress.snapTo(0f)
                                dogEmoji = "🐰"
                                hurdleRotation = 0f
                                dogRotation = 0f
                                
                                // Clean jump
                                animProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 800, easing = LinearEasing)
                                )
                                
                                dogEmoji = "🐰"
                                score += 50
                                dogActionText = "Sempurna! Lompatan pas melewati rintangan. (Reward: +50 XP)"
                                triedMedium = true
                                isAnimating = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CunnyOutlineButton(
                        text = "Lompat Tinggi",
                        onClick = {
                            if (isAnimating) return@CunnyOutlineButton
                            isAnimating = true
                            maxHeight = 100
                            scope.launch {
                                // Reset
                                animProgress.snapTo(0f)
                                dogEmoji = "🐰"
                                hurdleRotation = 0f
                                dogRotation = 0f
                                
                                // Clean but exhausting jump
                                animProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 800, easing = LinearEasing)
                                )
                                
                                dogEmoji = "🐰"
                                score += 10
                                dogActionText = "Lompatan terlalu tinggi, membuang energi berlebih. (Reward: +10 XP)"
                                triedHigh = true
                                isAnimating = false
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun QTableRow(
    actionName: String,
    statusText: String,
    isTried: Boolean,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isTried) color.copy(alpha = 0.08f) else Color.Transparent, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = actionName,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = CunnyColors.textDark,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = statusText,
            fontFamily = DmSansFontFamily,
            fontSize = 11.sp,
            fontWeight = if (isTried) FontWeight.Bold else FontWeight.Normal,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.5f)
        )
    }
}
