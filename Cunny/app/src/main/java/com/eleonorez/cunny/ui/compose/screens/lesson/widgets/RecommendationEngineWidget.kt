package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import com.adamglin.phosphoricons.Regular

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.regular.Television
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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

// ── Data models ──────────────────────────────────────────────────────────────

data class RecProfile(
    val id: String,
    val name: String,
    val emoji: String,
    val interests: List<String>
)

data class RecVideo(
    val id: String,
    val title: String,
    val emoji: String,
    val category: String,
    val targetProfile: String
)

private enum class RecStage {
    MATCHING,       // User is matching videos to profiles
    FEEDBACK,       // Show feedback for current video
    FINAL_SCORE     // Show final beranda score
}

// ── Config parser ────────────────────────────────────────────────────────────

private fun parseProfiles(config: Map<String, Any>?): List<RecProfile> {
    val list = config?.get("profiles") as? List<*> ?: return defaultProfiles()
    return list.filterIsInstance<Map<*, *>>().map { m ->
        RecProfile(
            id = m["id"]?.toString() ?: "",
            name = m["name"]?.toString() ?: "",
            emoji = m["emoji"]?.toString() ?: "👤",
            interests = (m["interests"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
        )
    }.ifEmpty { defaultProfiles() }
}

private fun parseVideos(config: Map<String, Any>?): List<RecVideo> {
    val list = config?.get("videos") as? List<*> ?: return defaultVideos()
    return list.filterIsInstance<Map<*, *>>().map { m ->
        RecVideo(
            id = m["id"]?.toString() ?: "",
            title = m["title"]?.toString() ?: "",
            emoji = m["emoji"]?.toString() ?: "🎬",
            category = m["category"]?.toString() ?: "",
            targetProfile = m["target_profile"]?.toString() ?: ""
        )
    }.ifEmpty { defaultVideos() }
}

private fun defaultProfiles() = listOf(
    RecProfile("budi", "Budi", "🧑‍🚀", listOf("Sains", "Luar Angkasa")),
    RecProfile("siti", "Siti", "👩‍🎓", listOf("Kucing", "Hewan Lucu")),
    RecProfile("andi", "Andi", "🧑‍🎾", listOf("Olahraga", "Sepak Bola"))
)

private fun defaultVideos() = listOf(
    RecVideo("v1", "Misteri Planet Mars", "🪐", "Sains", "budi"),
    RecVideo("v2", "Kompilasi Kucing Lucu", "🐱", "Hewan Lucu", "siti"),
    RecVideo("v3", "Gol Terbaik Liga Inggris", "⚽", "Olahraga", "andi"),
    RecVideo("v4", "Bagaimana Roket Bekerja", "🚀", "Sains", "budi")
)

// ── Interest emoji mapper ────────────────────────────────────────────────────

private fun interestEmoji(interest: String): String {
    val lower = interest.lowercase()
    return when {
        lower.contains("sains") || lower.contains("science") -> "🔬"
        lower.contains("angkasa") || lower.contains("space") -> "🌌"
        lower.contains("kucing") || lower.contains("cat") -> "🐱"
        lower.contains("hewan") || lower.contains("animal") -> "🐾"
        lower.contains("olahraga") || lower.contains("sport") -> "🏃"
        lower.contains("bola") || lower.contains("football") || lower.contains("soccer") -> "⚽"
        else -> "💡"
    }
}

// ── Main widget ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendationEngineWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val profiles = remember { parseProfiles(block.config) }
    val videos = remember { parseVideos(block.config) }

    var currentVideoIdx by remember { mutableIntStateOf(0) }
    var stage by remember { mutableStateOf(RecStage.MATCHING) }
    var showVideoCard by remember { mutableStateOf(true) }
    var lastCorrect by remember { mutableStateOf(false) }

    val selections = remember { mutableStateMapOf<String, String>() } // videoId -> profileId
    val correctCount by remember { derivedStateOf { 
        selections.count { (videoId, profileId) ->
            videos.find { it.id == videoId }?.targetProfile == profileId
        }
    }}

    // Animate card entrance
    LaunchedEffect(currentVideoIdx) {
        showVideoCard = false
        delay(200)
        showVideoCard = true
    }

    // Auto-advance after feedback
    LaunchedEffect(stage) {
        if (stage == RecStage.FEEDBACK) {
            delay(1800)
            if (currentVideoIdx < videos.size - 1) {
                currentVideoIdx++
                stage = RecStage.MATCHING
            } else {
                stage = RecStage.FINAL_SCORE
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
                // Header
                Icon(
                    imageVector = PhosphorIcons.Regular.Television,
                    contentDescription = null,
                    tint = CunnyColors.primary,
                    modifier = Modifier.size(40.dp).padding(bottom = 4.dp)
                )
                Text(
                    text = "Mesin Rekomendasi AI",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Cocokkan video ke pengguna yang tepat berdasarkan minat!",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                // ── Profile avatar cards ──
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    profiles.forEach { profile ->
                        ProfileAvatarCard(
                            profile = profile,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Progress section ──
                if (stage != RecStage.FINAL_SCORE) {
                    // Progress dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        videos.forEachIndexed { idx, video ->
                            val dotColor = when {
                                idx < currentVideoIdx || (idx == currentVideoIdx && stage == RecStage.FEEDBACK) -> {
                                    val sel = selections[video.id]
                                    if (sel == video.targetProfile) CunnyColors.accentGreen
                                    else CunnyColors.accentRed
                                }
                                idx == currentVideoIdx -> CunnyColors.primary
                                else -> CunnyColors.borderLight
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentVideoIdx) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    Text(
                        text = "Video ${currentVideoIdx + 1} dari ${videos.size}",
                        fontFamily = DmSansFontFamily,
                        fontSize = 11.sp,
                        color = CunnyColors.textSubtle
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Video card ──
                    val currentVideo = videos.getOrNull(currentVideoIdx)
                    if (currentVideo != null) {
                        AnimatedVisibility(
                            visible = showVideoCard,
                            enter = slideInVertically(
                                initialOffsetY = { it / 3 },
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ) + fadeIn(tween(300)),
                            exit = fadeOut(tween(150))
                        ) {
                            VideoFeedCard(
                                video = currentVideo,
                                profiles = profiles,
                                stage = stage,
                                selectedProfileId = selections[currentVideo.id],
                                isCorrect = lastCorrect,
                                onSelectProfile = { profileId ->
                                    selections[currentVideo.id] = profileId
                                    lastCorrect = currentVideo.targetProfile == profileId
                                    stage = RecStage.FEEDBACK
                                }
                            )
                        }
                    }
                }

                // ── Final score ──
                AnimatedVisibility(
                    visible = stage == RecStage.FINAL_SCORE,
                    enter = fadeIn(tween(400)) + expandVertically(tween(500))
                ) {
                    FeedScorePanel(
                        videos = videos,
                        profiles = profiles,
                        selections = selections,
                        correctCount = correctCount,
                        onWidgetCompleted = onWidgetCompleted
                    )
                }
            }
        }
    }
}

// ── Profile avatar card ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileAvatarCard(
    profile: RecProfile,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(CunnyDimens.radiusMd)

    Box(
        modifier = modifier
            .padding(bottom = 3.dp)
            .background(CunnyColors.tactileShadow, shape = cardShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-3).dp)
                .clip(cardShape)
                .background(Color.White.copy(alpha = 0.6f))
                .border(1.dp, CunnyColors.borderLight, cardShape)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar emoji
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(
                                CunnyColors.primary.copy(alpha = 0.2f),
                                CunnyColors.primaryShadow.copy(alpha = 0.15f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = profile.emoji, fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = profile.name,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CunnyColors.textDark
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Interest badges
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                profile.interests.forEach { interest ->
                    val emoji = interestEmoji(interest)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CunnyColors.primaryPale)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$emoji $interest",
                            fontFamily = DmSansFontFamily,
                            fontSize = 9.sp,
                            color = CunnyColors.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

// ── Video feed card ──────────────────────────────────────────────────────────

@Composable
private fun VideoFeedCard(
    video: RecVideo,
    profiles: List<RecProfile>,
    stage: RecStage,
    selectedProfileId: String?,
    isCorrect: Boolean,
    onSelectProfile: (String) -> Unit
) {
    val cardShape = RoundedCornerShape(CunnyDimens.radiusMd)

    val cardBaseColor = when {
        stage == RecStage.FEEDBACK && isCorrect -> CunnyColors.primaryShadow
        stage == RecStage.FEEDBACK && !isCorrect -> Color(0xFFC98E8E)
        else -> CunnyColors.tactileShadow
    }
    val cardBg = when {
        stage == RecStage.FEEDBACK && isCorrect -> CunnyColors.primaryPale
        stage == RecStage.FEEDBACK && !isCorrect -> Color(0xFFFFF5F5)
        else -> Color.White.copy(alpha = 0.7f)
    }
    val cardBorder = when {
        stage == RecStage.FEEDBACK && isCorrect -> CunnyColors.primary.copy(alpha = 0.24f)
        stage == RecStage.FEEDBACK && !isCorrect -> CunnyColors.accentRed.copy(alpha = 0.24f)
        else -> CunnyColors.borderLight
    }

    // 3D tactile card
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
                .border(1.dp, cardBorder, cardShape)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Video thumbnail simulation
            val thumbShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(thumbShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color(0xFF262040),
                                Color(0xFF302B48),
                                Color(0xFF262040)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Play button overlay
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = video.emoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", fontSize = 16.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Video title
            Text(
                text = video.title,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CunnyColors.textDark
            )

            // Category badge
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CunnyColors.primaryPale)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "📂 ${video.category}",
                    fontFamily = DmSansFontFamily,
                    fontSize = 11.sp,
                    color = CunnyColors.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Question text
            if (stage == RecStage.MATCHING) {
                Text(
                    text = "Rekomendasikan video ini untuk siapa?",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Profile selection buttons (hidden during feedback)
            if (stage == RecStage.MATCHING) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    profiles.forEach { profile ->
                        ProfileSelectButton(
                            profile = profile,
                            onClick = { onSelectProfile(profile.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Feedback panel
            AnimatedVisibility(
                visible = stage == RecStage.FEEDBACK,
                enter = expandVertically(tween(300)) + fadeIn(tween(300))
            ) {
                val targetProfile = profiles.find { it.id == video.targetProfile }
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
                        text = if (isCorrect)
                            "${targetProfile?.emoji ?: ""} ${targetProfile?.name ?: ""} menyukai ${video.category}!"
                        else {
                            val correctName = targetProfile?.name ?: ""
                            val correctEmoji = targetProfile?.emoji ?: ""
                            "Video ini lebih cocok untuk $correctEmoji $correctName yang suka ${video.category}."
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

// ── Profile select button (3D tactile) ───────────────────────────────────────

@Composable
private fun ProfileSelectButton(
    profile: RecProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-3).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "profileBtnOffset"
    )

    val btnShape = RoundedCornerShape(CunnyDimens.radiusMd)
    Box(
        modifier = modifier
            .padding(bottom = 3.dp)
            .background(CunnyColors.primaryShadow, shape = btnShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = offsetY)
                .clip(btnShape)
                .background(brush = Brush.verticalGradient(CunnyColors.gradPlum))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = profile.emoji, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = profile.name,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}

// ── Final score panel ────────────────────────────────────────────────────────

@Composable
private fun FeedScorePanel(
    videos: List<RecVideo>,
    profiles: List<RecProfile>,
    selections: Map<String, String>,
    correctCount: Int,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val total = videos.size

    val scoreEmoji = when {
        correctCount == total -> "🏆"
        correctCount >= (total * 0.7).toInt() -> "📺"
        else -> "💪"
    }
    val scoreMessage = when {
        correctCount == total -> "Sempurna! Kamu ahli rekomendasi AI!"
        correctCount >= (total * 0.7).toInt() -> "Bagus! Kamu memahami cara kerja rekomendasi."
        else -> "Terus berlatih! Perhatikan minat setiap pengguna."
    }

    LaunchedEffect(Unit) {
        onWidgetCompleted(true)
    }

    val reportShape = RoundedCornerShape(CunnyDimens.radiusMd)

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
                text = "Beranda Terisi!",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CunnyColors.textDark
            )
            Text(
                text = "Rekomendasi tepat: $correctCount/$total",
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

            // Per-video results
            videos.forEach { video ->
                val selectedId = selections[video.id]
                val correct = selectedId == video.targetProfile
                val selectedProfile = profiles.find { it.id == selectedId }
                val targetProfile = profiles.find { it.id == video.targetProfile }

                val rowBg = if (correct) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                val rowBorder = if (correct) Color(0xFF81C784) else Color(0xFFFFB74D)

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
                    Text(text = video.emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = CunnyColors.textDark,
                            maxLines = 1
                        )
                        Text(
                            text = if (correct)
                                "→ ${targetProfile?.emoji} ${targetProfile?.name} ✅"
                            else
                                "Pilih: ${selectedProfile?.emoji} ${selectedProfile?.name} (seharusnya ${targetProfile?.emoji} ${targetProfile?.name})",
                            fontFamily = DmSansFontFamily,
                            fontSize = 10.sp,
                            color = CunnyColors.textSubtle,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
