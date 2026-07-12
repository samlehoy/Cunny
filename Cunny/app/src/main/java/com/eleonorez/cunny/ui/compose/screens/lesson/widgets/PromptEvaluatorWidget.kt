@file:OptIn(ExperimentalLayoutApi::class)

package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

// ── Data ─────────────────────────────────────────────────────────────────────

private data class TextSegment(
    val text: String,
    val isIncorrect: Boolean,
    val phraseIndex: Int = -1
)

private enum class EvalStage { CHOOSE_PROMPT, AUDIT_RESPONSE }

// ── Parser ───────────────────────────────────────────────────────────────────

/**
 * Splits [response] into a list of segments. Plain-text segments have
 * [TextSegment.isIncorrect] = false; substring-matched phrases from
 * [phrases] have isIncorrect = true with the corresponding [TextSegment.phraseIndex].
 */
private fun parseAiResponse(
    response: String,
    phrases: List<String>
): List<TextSegment> {
    if (phrases.isEmpty() || response.isEmpty()) {
        return listOf(TextSegment(response, isIncorrect = false))
    }

    data class Occ(val start: Int, val phraseIdx: Int)

    val occurrences = mutableListOf<Occ>()
    for (i in phrases.indices) {
        val idx = response.indexOf(phrases[i])
        if (idx != -1) occurrences.add(Occ(idx, i))
    }
    occurrences.sortBy { it.start }

    // Remove overlapping matches
    val filtered = mutableListOf<Occ>()
    var endPos = 0
    for (occ in occurrences) {
        if (occ.start >= endPos) {
            filtered.add(occ)
            endPos = occ.start + phrases[occ.phraseIdx].length
        }
    }

    val segments = mutableListOf<TextSegment>()
    var pos = 0
    for (occ in filtered) {
        if (occ.start > pos) {
            segments.add(TextSegment(response.substring(pos, occ.start), isIncorrect = false))
        }
        segments.add(TextSegment(phrases[occ.phraseIdx], isIncorrect = true, phraseIndex = occ.phraseIdx))
        pos = occ.start + phrases[occ.phraseIdx].length
    }
    if (pos < response.length) {
        segments.add(TextSegment(response.substring(pos), isIncorrect = false))
    }
    return segments
}

// ── Main Widget ──────────────────────────────────────────────────────────────

@Composable
fun PromptEvaluatorWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    // ── Parse API config ─────────────────────────────────────────────────
    val scenario = (block.config?.get("scenario") as? String) ?: "Evaluasi jawaban AI"
    val aiResponse = (block.config?.get("ai_response") as? String) ?: ""
    val promptOptions = remember {
        @Suppress("UNCHECKED_CAST")
        (block.config?.get("prompt_options") as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()
    }
    val incorrectPhrases = remember {
        @Suppress("UNCHECKED_CAST")
        (block.config?.get("incorrect_phrases") as? List<*>)
            ?.filterIsInstance<String>() ?: emptyList()
    }
    val promptResponses = remember {
        @Suppress("UNCHECKED_CAST")
        (block.config?.get("prompt_responses") as? List<*>)
            ?.filterIsInstance<Map<String, Any>>() ?: emptyList()
    }

    // ── State ────────────────────────────────────────────────────────────
    var stage by remember {
        mutableStateOf(
            if (promptOptions.isEmpty()) EvalStage.AUDIT_RESPONSE
            else EvalStage.CHOOSE_PROMPT
        )
    }
    var selectedPrompt by remember { mutableStateOf("") }
    var showTyping by remember { mutableStateOf(false) }
    var showResponse by remember { mutableStateOf(false) }
    var spottedPhrases by remember { mutableStateOf(setOf<Int>()) }

    // Reset spotted phrases when a new prompt is selected
    LaunchedEffect(selectedPrompt) {
        spottedPhrases = emptySet()
    }

    // Resolve active response dynamically based on selectedPrompt
    val activeResponseObj = remember(selectedPrompt, promptResponses) {
        promptResponses.firstOrNull { it["prompt"] == selectedPrompt }
    }
    val activeAiResponse = remember(activeResponseObj, aiResponse) {
        (activeResponseObj?.get("ai_response") as? String) ?: aiResponse
    }
    val activeIncorrectPhrases = remember(activeResponseObj, incorrectPhrases) {
        @Suppress("UNCHECKED_CAST")
        (activeResponseObj?.get("incorrect_phrases") as? List<*>)
            ?.filterIsInstance<String>() ?: incorrectPhrases
    }
    val activeExplanation = remember(activeResponseObj) {
        (activeResponseObj?.get("explanation") as? String) ?: ""
    }

    val segments = remember(activeAiResponse, activeIncorrectPhrases) {
        parseAiResponse(activeAiResponse, activeIncorrectPhrases)
    }
    val totalIncorrect = segments.count { it.isIncorrect }

    val completed = spottedPhrases.size == totalIncorrect && totalIncorrect > 0

    LaunchedEffect(completed) {
        if (completed) onWidgetCompleted(true)
    }

    // Typing indicator → reveal response after delay
    LaunchedEffect(stage) {
        if (stage == EvalStage.AUDIT_RESPONSE) {
            showTyping = true
            delay(1500L)
            showTyping = false
            showResponse = true
        }
    }

    // ── Outer 3-D container ──────────────────────────────────────────────
    val outerShape = RoundedCornerShape(CunnyDimens.radiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 4.dp)
            .background(color = CunnyColors.tactileShadow, shape = outerShape)
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
                // ── Header ───────────────────────────────────────────────
                Text("🐰", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Evaluator Jawaban AI",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Skenario: $scenario",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ── Stage 1: Prompt selection ────────────────────────────
                if (stage == EvalStage.CHOOSE_PROMPT) {
                    Text(
                        text = "Pilih pertanyaan untuk ditanyakan ke AI!",
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = CunnyColors.textBody,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    promptOptions.forEachIndexed { index, prompt ->
                        PromptChip(text = prompt) {
                            selectedPrompt = prompt
                            stage = EvalStage.AUDIT_RESPONSE
                        }
                        if (index < promptOptions.lastIndex) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }

                // ── Stage 2: Chat audit ──────────────────────────────────
                if (stage == EvalStage.AUDIT_RESPONSE) {
                    // User chat bubble (only if a prompt was chosen)
                    if (selectedPrompt.isNotEmpty()) {
                        UserChatBubble(text = selectedPrompt)
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Typing indicator
                    AnimatedVisibility(visible = showTyping) {
                        TypingIndicator()
                    }

                    // AI response bubble + instruction / success
                    AnimatedVisibility(
                        visible = showResponse,
                        enter = fadeIn(tween(300)) + slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    ) {
                        Column {
                            AiResponseBubble(
                                segments = segments,
                                spottedPhrases = spottedPhrases,
                                onSpotPhrase = { idx ->
                                    spottedPhrases = spottedPhrases + idx
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            if (!completed) {
                                Text(
                                    text = "💡 Ketuk bagian yang menurutmu salah!",
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 12.sp,
                                    color = CunnyColors.textSubtle,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Success panel
                            AnimatedVisibility(
                                visible = completed,
                                enter = fadeIn(tween(300)) +
                                        expandVertically(animationSpec = tween(300))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(CunnyDimens.radiusMd))
                                        .background(CunnyColors.primaryPale)
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "🎉 Halusinasi Terdeteksi!",
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CunnyColors.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (activeExplanation.isNotEmpty()) {
                                            activeExplanation
                                        } else {
                                            "Kamu berhasil menemukan informasi yang salah! " +
                                                    "AI bisa terdengar sangat meyakinkan padahal salah. " +
                                                    "Fenomena ini disebut Halusinasi AI. " +
                                                    "Selalu periksa fakta dari sumber terpercaya!"
                                        },
                                        fontFamily = DmSansFontFamily,
                                        fontSize = 12.sp,
                                        color = CunnyColors.textBody,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Sub-components ───────────────────────────────────────────────────────────

/** 3-D tactile chip for prompt selection in Stage 1. */
@Composable
private fun PromptChip(text: String, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-3).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "promptOffset"
    )
    val chipShape = RoundedCornerShape(CunnyDimens.radiusMd)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .padding(bottom = 3.dp)
            .background(CunnyColors.tactileShadow, chipShape)
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
                .clip(chipShape)
                .background(Color.White.copy(alpha = 0.7f))
                .border(1.dp, CunnyColors.borderLight, chipShape)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = CunnyColors.textDark
            )
        }
    }
}

/** Right-aligned plum chat bubble showing the user's chosen prompt. */
@Composable
private fun UserChatBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        val shape = RoundedCornerShape(
            topStart = 16.dp, topEnd = 4.dp,
            bottomStart = 16.dp, bottomEnd = 16.dp
        )
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(shape)
                .background(CunnyColors.primary.copy(alpha = 0.12f))
                .border(1.dp, CunnyColors.primary.copy(alpha = 0.3f), shape)
                .padding(12.dp)
        ) {
            Text(
                text = text,
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = CunnyColors.textDark
            )
        }
    }
}

/** Three bouncing dots indicating the AI is "typing". */
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")

    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🤖", fontSize = 20.sp)
        Spacer(modifier = Modifier.width(8.dp))

        for (i in 0 until 3) {
            val offsetY by transition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 400, delayMillis = i * 150),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot$i"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .offset(y = offsetY.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.textSubtle.copy(alpha = 0.5f))
            )
        }
    }
}

/**
 * Left-aligned AI chat bubble. Plain words flow naturally via [FlowRow];
 * incorrect phrases are rendered as tappable 3-D pills.
 */
@Composable
private fun AiResponseBubble(
    segments: List<TextSegment>,
    spottedPhrases: Set<Int>,
    onSpotPhrase: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            "🤖",
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CunnyColors.textSubtle
            )
            Spacer(modifier = Modifier.height(4.dp))

            val bubbleShape = RoundedCornerShape(
                topStart = 4.dp, topEnd = 16.dp,
                bottomStart = 16.dp, bottomEnd = 16.dp
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(bubbleShape)
                    .background(Color.White.copy(alpha = 0.6f))
                    .border(1.dp, CunnyColors.borderLight, bubbleShape)
                    .padding(12.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    segments.forEach { segment ->
                        if (segment.isIncorrect) {
                            InteractivePill(
                                text = segment.text,
                                isSpotted = segment.phraseIndex in spottedPhrases,
                                onClick = { onSpotPhrase(segment.phraseIndex) }
                            )
                        } else {
                            // Emit each word as its own Text so FlowRow can wrap naturally
                            segment.text.trim().split("\\s+".toRegex())
                                .filter { it.isNotEmpty() }
                                .forEach { word ->
                                    Text(
                                        text = word,
                                        fontFamily = DmSansFontFamily,
                                        fontSize = 14.sp,
                                        color = CunnyColors.textDark,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

/** Tappable 3-D pill for an incorrect phrase inside the AI response. */
@Composable
private fun InteractivePill(
    text: String,
    isSpotted: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-2).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pillOffset"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSpotted) CunnyColors.accentRed.copy(alpha = 0.15f)
        else Color(0xFFF0ECF8),
        label = "pillBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSpotted) CunnyColors.accentRed else CunnyColors.borderLight,
        label = "pillBorder"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSpotted) CunnyColors.accentRed else CunnyColors.textDark,
        label = "pillText"
    )
    val shadowColor = if (isSpotted) Color(0xFFC98E8E) else CunnyColors.tactileShadow
    val pillShape = RoundedCornerShape(6.dp)

    Box(
        modifier = Modifier
            .padding(bottom = 2.dp)
            .background(shadowColor, pillShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { if (!isSpotted) onClick() }
    ) {
        Row(
            modifier = Modifier
                .offset(y = offsetY)
                .clip(pillShape)
                .background(bgColor)
                .border(1.dp, borderColor, pillShape)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSpotted) {
                Text(
                    "⚠️",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Text(
                text = text,
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
