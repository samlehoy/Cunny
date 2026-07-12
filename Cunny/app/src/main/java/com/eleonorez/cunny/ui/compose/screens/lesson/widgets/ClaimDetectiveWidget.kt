package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay

// ── Data models ──────────────────────────────────────────────────────────────

private data class Claim(
    val text: String,
    val isFake: Boolean,
    val explanation: String
)

private data class ClaimResult(
    val claim: Claim,
    val userSaidFake: Boolean,
    val isCorrect: Boolean
)

private enum class DetectiveStage {
    REVIEWING,      // Show one claim at a time
    FEEDBACK,       // Show feedback for current claim
    FINAL_SCORE     // Show detective report
}

// ── Config parser ────────────────────────────────────────────────────────────

private fun parseClaims(block: WidgetBlock): List<Claim> {
    val claimsList = block.config?.get("claims") as? List<*> ?: return defaultClaims()
    return claimsList.filterIsInstance<Map<*, *>>().map { claimMap ->
        Claim(
            text = claimMap["text"]?.toString() ?: "",
            isFake = claimMap["is_fake"] as? Boolean ?: false,
            explanation = claimMap["explanation"]?.toString() ?: ""
        )
    }.ifEmpty { defaultClaims() }
}

private fun defaultClaims() = listOf(
    Claim("Gajah adalah hewan darat terbesar di dunia.", false,
        "Benar! Gajah Afrika adalah hewan darat terbesar yang masih hidup saat ini."),
    Claim("AI sudah bisa membaca pikiran manusia dengan akurat 100%.", true,
        "Hoaks! AI belum bisa membaca pikiran. AI hanya bisa menganalisis data yang diberikan kepadanya."),
    Claim("Deepfake bisa membuat video palsu yang terlihat sangat nyata.", false,
        "Benar! Teknologi deepfake menggunakan AI untuk meniru wajah dan suara secara sangat realistis."),
    Claim("Semua foto di internet yang terlihat bagus pasti asli dan tidak diedit.", true,
        "Hoaks! Banyak foto di internet yang sudah diedit atau bahkan sepenuhnya dibuat oleh AI generatif."),
    Claim("Kata Deepfake berasal dari Deep Learning dan Fake.", false,
        "Benar! Deep Learning adalah teknologi AI yang digunakan, dan Fake artinya palsu.")
)

// ── Main widget ──────────────────────────────────────────────────────────────

@Composable
fun ClaimDetectiveWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val claims = remember { parseClaims(block) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var stage by remember { mutableStateOf(DetectiveStage.REVIEWING) }
    var showCard by remember { mutableStateOf(true) }
    val results = remember { mutableStateListOf<ClaimResult>() }

    // Animate card entrance for each new claim
    LaunchedEffect(currentIndex) {
        showCard = false
        delay(200)
        showCard = true
    }

    // Auto-advance after feedback
    LaunchedEffect(stage) {
        if (stage == DetectiveStage.FEEDBACK) {
            delay(2200)
            if (currentIndex < claims.size - 1) {
                currentIndex++
                stage = DetectiveStage.REVIEWING
            } else {
                stage = DetectiveStage.FINAL_SCORE
            }
        }
    }

    // 3D outer container
    val outerShape = RoundedCornerShape(CunnyDimens.radiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 4.dp)
            .background(
                color = CunnyColors.tactileShadow,
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
                // Header
                Text("🕵️", fontSize = 36.sp, modifier = Modifier.padding(bottom = 4.dp))
                Text(
                    text = "Detektif Klaim AI",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bedakan klaim fakta dan hoaks buatan AI!",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Progress dots
                if (stage != DetectiveStage.FINAL_SCORE) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        claims.forEachIndexed { idx, _ ->
                            val dotColor = when {
                                idx < results.size -> {
                                    if (results[idx].isCorrect) CunnyColors.accentGreen
                                    else CunnyColors.accentRed
                                }
                                idx == currentIndex -> CunnyColors.primary
                                else -> CunnyColors.borderLight
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentIndex) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    // Progress text
                    Text(
                        text = "Klaim ${currentIndex + 1} dari ${claims.size}",
                        fontFamily = DmSansFontFamily,
                        fontSize = 11.sp,
                        color = CunnyColors.textSubtle
                    )

                    // Progress bar
                    Spacer(modifier = Modifier.height(6.dp))
                    val progressShape = RoundedCornerShape(3.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(4.dp)
                            .clip(progressShape)
                            .background(CunnyColors.borderLight)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(
                                    fraction = (currentIndex + 1).toFloat() / claims.size
                                )
                                .height(4.dp)
                                .clip(progressShape)
                                .background(
                                    brush = Brush.horizontalGradient(CunnyColors.gradProgress)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // ── Claim card ──
                if (stage != DetectiveStage.FINAL_SCORE) {
                    val currentClaim = claims.getOrNull(currentIndex)
                    if (currentClaim != null) {
                        AnimatedVisibility(
                            visible = showCard,
                            enter = slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + fadeIn(tween(300)),
                            exit = fadeOut(tween(150))
                        ) {
                            ClaimCard(
                                claim = currentClaim,
                                showFeedback = stage == DetectiveStage.FEEDBACK,
                                lastResult = results.lastOrNull(),
                                onAnswer = { userSaidFake ->
                                    val correct = userSaidFake == currentClaim.isFake
                                    results.add(
                                        ClaimResult(
                                            claim = currentClaim,
                                            userSaidFake = userSaidFake,
                                            isCorrect = correct
                                        )
                                    )
                                    stage = DetectiveStage.FEEDBACK
                                }
                            )
                        }
                    }
                }

                // ── Final score ──
                AnimatedVisibility(
                    visible = stage == DetectiveStage.FINAL_SCORE,
                    enter = fadeIn(tween(400)) + expandVertically(tween(500))
                ) {
                    DetectiveReport(
                        claims = claims,
                        results = results,
                        onWidgetCompleted = onWidgetCompleted
                    )
                }
            }
        }
    }
}

// ── Claim card composable ────────────────────────────────────────────────────

@Composable
private fun ClaimCard(
    claim: Claim,
    showFeedback: Boolean,
    lastResult: ClaimResult?,
    onAnswer: (userSaidFake: Boolean) -> Unit
) {
    val cardShape = RoundedCornerShape(CunnyDimens.radiusMd)

    // 3D tactile card
    val cardBaseColor = when {
        showFeedback && lastResult?.isCorrect == true -> CunnyColors.primaryShadow
        showFeedback && lastResult?.isCorrect == false -> Color(0xFFC98E8E)
        else -> CunnyColors.tactileShadow
    }
    val cardBg = when {
        showFeedback && lastResult?.isCorrect == true -> CunnyColors.primaryPale
        showFeedback && lastResult?.isCorrect == false -> Color(0xFFFFF5F5)
        else -> Color.White.copy(alpha = 0.7f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp)
            .background(cardBaseColor, shape = cardShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-3).dp)
                .clip(cardShape)
                .background(cardBg)
                .border(
                    width = 1.dp,
                    color = when {
                        showFeedback && lastResult?.isCorrect == true ->
                            CunnyColors.primary.copy(alpha = 0.24f)
                        showFeedback && lastResult?.isCorrect == false ->
                            CunnyColors.accentRed.copy(alpha = 0.24f)
                        else -> CunnyColors.borderLight
                    },
                    shape = cardShape
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Newspaper emoji
            Text("📰", fontSize = 28.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Claim text
            Text(
                text = "\"${claim.text}\"",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center,
                fontStyle = FontStyle.Italic,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons (hidden during feedback)
            if (!showFeedback) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ClaimActionButton(
                        text = "✅ Fakta",
                        shadowColor = Color(0xFF3A6B4A),
                        bgGradient = listOf(Color(0xFF4A8B62), Color(0xFF3A6B4A)),
                        onClick = { onAnswer(false) },
                        modifier = Modifier.weight(1f)
                    )
                    ClaimActionButton(
                        text = "❌ Hoaks AI",
                        shadowColor = Color(0xFF8B4A4A),
                        bgGradient = listOf(Color(0xFFB05555), Color(0xFF8B4A4A)),
                        onClick = { onAnswer(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Feedback panel
            AnimatedVisibility(
                visible = showFeedback && lastResult != null,
                enter = expandVertically(tween(300)) + fadeIn(tween(300))
            ) {
                if (lastResult != null) {
                    val isCorrect = lastResult.isCorrect
                    val feedbackBg = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    val feedbackBorder = if (isCorrect) Color(0xFF81C784) else Color(0xFFFFB74D)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(feedbackBg)
                            .border(1.dp, feedbackBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isCorrect) "✅ Tepat!" else "⚠️ Kurang tepat!",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = lastResult.claim.explanation,
                            fontFamily = DmSansFontFamily,
                            fontSize = 12.sp,
                            color = CunnyColors.textBody,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Claim action button ──────────────────────────────────────────────────────

@Composable
private fun ClaimActionButton(
    text: String,
    shadowColor: Color,
    bgGradient: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-3).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "claimBtnOffset"
    )

    val btnShape = RoundedCornerShape(CunnyDimens.radiusFull)
    Box(
        modifier = modifier
            .padding(bottom = 3.dp)
            .background(shadowColor, shape = btnShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .clip(btnShape)
                .background(brush = Brush.verticalGradient(bgGradient))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

// ── Detective report (final score) ───────────────────────────────────────────

@Composable
private fun DetectiveReport(
    claims: List<Claim>,
    results: List<ClaimResult>,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val correctCount = results.count { it.isCorrect }
    val total = results.size

    val scoreEmoji = when {
        correctCount == total -> "🏆"
        correctCount >= (total * 0.7).toInt() -> "🕵️"
        else -> "💪"
    }
    val scoreMessage = when {
        correctCount == total -> "Sempurna! Kamu Detektif Klaim sejati!"
        correctCount >= (total * 0.7).toInt() -> "Bagus! Kamu cukup jeli mengenali hoaks AI."
        else -> "Terus berlatih! Selalu verifikasi sebelum percaya."
    }

    LaunchedEffect(Unit) {
        onWidgetCompleted(true)
    }

    val reportShape = RoundedCornerShape(CunnyDimens.radiusMd)

    // 3D tactile report card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp)
            .background(CunnyColors.primaryShadow, shape = reportShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-3).dp)
                .clip(reportShape)
                .background(CunnyColors.primaryPale)
                .border(1.dp, CunnyColors.primary.copy(alpha = 0.24f), reportShape)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = scoreEmoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Laporan Detektif",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CunnyColors.textDark
            )
            Text(
                text = "Skor: $correctCount/$total klaim teridentifikasi!",
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = CunnyColors.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = scoreMessage,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                color = CunnyColors.textBody,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Per-claim result list
            results.forEach { result ->
                val correct = result.isCorrect
                val rowBg = if (correct) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                val rowBorder = if (correct) Color(0xFF81C784) else Color(0xFFFFB74D)
                val statusEmoji = if (correct) "✅" else "❌"
                val typeLabel = if (result.claim.isFake) "Hoaks" else "Fakta"

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = statusEmoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = result.claim.text,
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            color = CunnyColors.textDark,
                            maxLines = 2
                        )
                        Text(
                            text = "$typeLabel — ${if (correct) "Kamu benar!" else "Jawaban salah"}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 10.sp,
                            color = CunnyColors.textSubtle
                        )
                    }
                }
            }
        }
    }
}
