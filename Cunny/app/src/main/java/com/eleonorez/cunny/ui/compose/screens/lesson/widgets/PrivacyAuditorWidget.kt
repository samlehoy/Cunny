package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import com.adamglin.phosphoricons.Regular

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
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.regular.Shield
import com.adamglin.phosphoricons.regular.Lock
import com.adamglin.phosphoricons.regular.Clipboard
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

private data class PermissionItem(
    val name: String,
    val isSuspicious: Boolean
)

private data class AuditApp(
    val name: String,
    val emoji: String,
    val permissions: List<PermissionItem>,
    val reason: String
)

private enum class AuditorStage {
    PHONE_IDLE,           // Show app icon + "Instal" button
    PERMISSION_REVIEW,    // Show permissions one by one
    AUDIT_RESULT,         // Show per-app audit report
    FINAL_SCORE           // Show final score across all apps
}

// null = not yet audited, true = user chose to reject, false = user chose to allow
private data class PermissionAuditResult(
    val permission: PermissionItem,
    val userRejected: Boolean?,  // null = pending
    val isCorrect: Boolean?      // null = pending
)

// ── Config parser ────────────────────────────────────────────────────────────

private fun parseApps(block: WidgetBlock): List<AuditApp> {
    val appsList = block.config?.get("apps") as? List<*> ?: return defaultApps()
    return appsList.filterIsInstance<Map<*, *>>().map { appMap ->
        val name = appMap["name"]?.toString() ?: "Aplikasi"
        val emoji = appMap["emoji"]?.toString() ?: "📱"
        val reason = appMap["reason"]?.toString() ?: ""
        val perms = (appMap["requested_permissions"] as? List<*>)?.map { permRaw ->
            when (permRaw) {
                is Map<*, *> -> PermissionItem(
                    name = permRaw["name"]?.toString() ?: "Izin",
                    isSuspicious = permRaw["is_suspicious"] as? Boolean ?: false
                )
                is String -> PermissionItem(name = permRaw, isSuspicious = false)
                else -> PermissionItem(name = "Izin", isSuspicious = false)
            }
        } ?: emptyList()
        AuditApp(name = name, emoji = emoji, permissions = perms, reason = reason)
    }
}

private fun defaultApps() = listOf(
    AuditApp(
        name = "Kalkulator Pintar AI", emoji = "🧮",
        permissions = listOf(
            PermissionItem("Kalkulasi Angka", false),
            PermissionItem("Lokasi Presisi GPS", true),
            PermissionItem("Daftar Kontak", true)
        ),
        reason = "Aplikasi kalkulator tidak membutuhkan GPS dan daftar kontak untuk berhitung."
    ),
    AuditApp(
        name = "Edit Foto Lucu AI", emoji = "📸",
        permissions = listOf(
            PermissionItem("Akses Galeri/Foto", false),
            PermissionItem("Kamera", false)
        ),
        reason = "Wajar jika editor foto membutuhkan akses kamera dan galeri."
    )
)

// ── Emoji mapper ─────────────────────────────────────────────────────────────

private fun permissionEmoji(name: String): String {
    val lower = name.lowercase()
    return when {
        lower.contains("gps") || lower.contains("lokasi") || lower.contains("location") -> "📍"
        lower.contains("kontak") || lower.contains("contact") -> "👥"
        lower.contains("kalkul") || lower.contains("math") || lower.contains("calcul") -> "🧮"
        lower.contains("kamera") || lower.contains("camera") -> "📸"
        lower.contains("galeri") || lower.contains("foto") || lower.contains("gallery") || lower.contains("photo") -> "🖼️"
        lower.contains("mikrofon") || lower.contains("micro") -> "🎙️"
        lower.contains("pesan") || lower.contains("sms") || lower.contains("message") -> "💬"
        else -> "📋"
    }
}

// ── Main widget ──────────────────────────────────────────────────────────────

@Composable
fun PrivacyAuditorWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val apps = remember { parseApps(block) }
    var currentAppIndex by remember { mutableIntStateOf(0) }
    var stage by remember { mutableStateOf(AuditorStage.PHONE_IDLE) }
    var currentPermIndex by remember { mutableIntStateOf(0) }
    var showPermDialog by remember { mutableStateOf(false) }
    var showFeedback by remember { mutableStateOf(false) }
    var lastFeedbackCorrect by remember { mutableStateOf(false) }

    // Track audit results per app
    val allAppResults = remember {
        mutableStateListOf<List<PermissionAuditResult>>()
    }
    val currentResults = remember {
        mutableStateListOf<PermissionAuditResult>()
    }

    val currentApp = apps.getOrNull(currentAppIndex)

    // When entering PERMISSION_REVIEW, show dialog with delay
    LaunchedEffect(stage, currentPermIndex) {
        if (stage == AuditorStage.PERMISSION_REVIEW) {
            showPermDialog = false
            delay(400)
            showPermDialog = true
        }
    }

    // Auto-dismiss feedback and advance
    LaunchedEffect(showFeedback) {
        if (showFeedback) {
            delay(1200)
            showFeedback = false
            val app = apps.getOrNull(currentAppIndex) ?: return@LaunchedEffect
            if (currentPermIndex < app.permissions.size - 1) {
                currentPermIndex++
            } else {
                // All permissions for this app audited
                allAppResults.add(currentResults.toList())
                stage = AuditorStage.AUDIT_RESULT
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
                Icon(
                    imageVector = PhosphorIcons.Regular.Shield,
                    contentDescription = null,
                    tint = CunnyColors.primary,
                    modifier = Modifier.size(40.dp).padding(bottom = 4.dp)
                )
                Text(
                    text = "Audit Privasi",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Periksa izin akses setiap aplikasi. Tolak yang mencurigakan!",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // App progress dots
                if (apps.size > 1) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        apps.forEachIndexed { idx, _ ->
                            val dotColor = when {
                                idx < currentAppIndex -> CunnyColors.accentGreen
                                idx == currentAppIndex -> CunnyColors.primary
                                else -> CunnyColors.borderLight
                            }
                            Box(
                                modifier = Modifier
                                    .size(if (idx == currentAppIndex) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                }

                // ── Phone frame ──
                if (currentApp != null && stage != AuditorStage.FINAL_SCORE) {
                    PhoneFrame(
                        app = currentApp,
                        stage = stage,
                        currentPermIndex = currentPermIndex,
                        showPermDialog = showPermDialog,
                        showFeedback = showFeedback,
                        lastFeedbackCorrect = lastFeedbackCorrect,
                        currentResults = currentResults,
                        onInstall = {
                            currentPermIndex = 0
                            currentResults.clear()
                            stage = AuditorStage.PERMISSION_REVIEW
                        },
                        onPermissionAction = { rejected ->
                            val perm = currentApp.permissions[currentPermIndex]
                            // Correct: reject suspicious, allow non-suspicious
                            val correct = if (perm.isSuspicious) rejected else !rejected
                            currentResults.add(
                                PermissionAuditResult(
                                    permission = perm,
                                    userRejected = rejected,
                                    isCorrect = correct
                                )
                            )
                            lastFeedbackCorrect = correct
                            showFeedback = true
                        },
                        onNextApp = {
                            if (currentAppIndex < apps.size - 1) {
                                currentAppIndex++
                                currentPermIndex = 0
                                currentResults.clear()
                                stage = AuditorStage.PHONE_IDLE
                            } else {
                                stage = AuditorStage.FINAL_SCORE
                            }
                        }
                    )
                }

                // ── Final score ──
                AnimatedVisibility(
                    visible = stage == AuditorStage.FINAL_SCORE,
                    enter = fadeIn(tween(400)) + expandVertically(tween(500))
                ) {
                    FinalScorePanel(
                        apps = apps,
                        allResults = allAppResults,
                        onWidgetCompleted = onWidgetCompleted
                    )
                }
            }
        }
    }
}

// ── Phone frame composable ───────────────────────────────────────────────────

@Composable
private fun PhoneFrame(
    app: AuditApp,
    stage: AuditorStage,
    currentPermIndex: Int,
    showPermDialog: Boolean,
    showFeedback: Boolean,
    lastFeedbackCorrect: Boolean,
    currentResults: List<PermissionAuditResult>,
    onInstall: () -> Unit,
    onPermissionAction: (rejected: Boolean) -> Unit,
    onNextApp: () -> Unit
) {
    val phoneShape = RoundedCornerShape(24.dp)
    val phoneBaseColor = Color(0xFF262040)

    // Phone bezel 3D
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .background(
                color = Color(0xFF161428),
                shape = phoneShape
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-3).dp)
                .clip(phoneShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF302B48),
                            phoneBaseColor,
                            Color(0xFF1A1828)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF3E3A58),
                    shape = phoneShape
                )
                .padding(2.dp)
        ) {
            // Status bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "13:01",
                    fontFamily = DmSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.8f)
                )
                // Notch
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                )
                Text(
                    text = "📶 🔋",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            // Screen content area
            val screenShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .clip(screenShape)
                    .background(Color(0xFF1A1828))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (stage) {
                        AuditorStage.PHONE_IDLE -> PhoneIdleContent(app, onInstall)
                        AuditorStage.PERMISSION_REVIEW -> PhonePermissionContent(
                            app = app,
                            permIndex = currentPermIndex,
                            showDialog = showPermDialog,
                            showFeedback = showFeedback,
                            feedbackCorrect = lastFeedbackCorrect,
                            onPermissionAction = onPermissionAction
                        )
                        AuditorStage.AUDIT_RESULT -> PhoneAuditResult(
                            app = app,
                            results = currentResults,
                            onNextApp = onNextApp
                        )
                        else -> { /* FINAL_SCORE handled outside phone frame */ }
                    }
                }
            }

            // Home indicator bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }
    }
}

// ── Stage 1: Phone idle — app icon + install button ──────────────────────────

@Composable
private fun PhoneIdleContent(app: AuditApp, onInstall: () -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))

    // App icon circle
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CunnyColors.primary.copy(alpha = 0.5f),
                        CunnyColors.primaryShadow.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = app.emoji, fontSize = 32.sp)
    }

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = app.name,
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Aplikasi ini meminta izin akses",
        fontFamily = DmSansFontFamily,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.5f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(20.dp))

    // Install button (3D tactile inside phone)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnOffsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-3).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "installBtnOffset"
    )

    val btnShape = RoundedCornerShape(CunnyDimens.radiusFull)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .padding(bottom = 3.dp)
            .background(
                color = CunnyColors.primaryShadow,
                shape = btnShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onInstall
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = btnOffsetY)
                .clip(btnShape)
                .background(
                    brush = Brush.verticalGradient(CunnyColors.gradPlum)
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📥 Instal Aplikasi",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

// ── Stage 2: Permission review — one permission at a time ────────────────────

@Composable
private fun PhonePermissionContent(
    app: AuditApp,
    permIndex: Int,
    showDialog: Boolean,
    showFeedback: Boolean,
    feedbackCorrect: Boolean,
    onPermissionAction: (rejected: Boolean) -> Unit
) {
    val perm = app.permissions.getOrNull(permIndex) ?: return

    // Progress text
    Text(
        text = "Izin ${permIndex + 1} dari ${app.permissions.size}",
        fontFamily = DmSansFontFamily,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.5f)
    )

    // Mini progress bar
    Spacer(modifier = Modifier.height(6.dp))
    val progressShape = RoundedCornerShape(3.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(4.dp)
            .clip(progressShape)
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = (permIndex + 1).toFloat() / app.permissions.size)
                .height(4.dp)
                .clip(progressShape)
                .background(
                    brush = Brush.horizontalGradient(CunnyColors.gradProgress)
                )
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Permission dialog card
    AnimatedVisibility(
        visible = showDialog,
        enter = slideInVertically(
            initialOffsetY = { it / 2 },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(tween(300))
    ) {
        val dialogShape = RoundedCornerShape(16.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(dialogShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), dialogShape)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock icon
            Icon(
                imageVector = PhosphorIcons.Regular.Lock,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Izin Akses",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Permission name with emoji
            val permEmoji = permissionEmoji(perm.name)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$permEmoji  ${perm.name}",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "\"${app.name}\" meminta izin ini",
                fontFamily = DmSansFontFamily,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons — only show when not showing feedback
            if (!showFeedback) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // "Izinkan" button
                    PermissionActionButton(
                        text = "✅ Izinkan",
                        bgColor = Color(0xFF2A4A35),
                        borderColor = Color(0xFF4A8B62),
                        onClick = { onPermissionAction(false) },
                        modifier = Modifier.weight(1f)
                    )
                    // "Tolak" button
                    PermissionActionButton(
                        text = "🚫 Tolak",
                        bgColor = Color(0xFF4A2A2A),
                        borderColor = Color(0xFF8B4A4A),
                        onClick = { onPermissionAction(true) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Feedback badge
            AnimatedVisibility(
                visible = showFeedback,
                enter = expandVertically(tween(300)) + fadeIn(tween(300))
            ) {
                val feedbackBg by animateColorAsState(
                    targetValue = if (feedbackCorrect) Color(0xFF1A3A25) else Color(0xFF3A1A1A),
                    animationSpec = tween(300),
                    label = "feedbackBg"
                )
                val feedbackBorder by animateColorAsState(
                    targetValue = if (feedbackCorrect) Color(0xFF4A8B62) else Color(0xFF8B4A4A),
                    animationSpec = tween(300),
                    label = "feedbackBorder"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(feedbackBg)
                        .border(1.dp, feedbackBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (feedbackCorrect) "✅ Keputusan tepat!" else "⚠️ Hmm, kurang tepat!",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ── Permission action button (inside phone dark UI) ──────────────────────────

@Composable
private fun PermissionActionButton(
    text: String,
    bgColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-2).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "permBtnOffset"
    )

    val btnShape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .padding(bottom = 2.dp)
            .background(borderColor.copy(alpha = 0.5f), shape = btnShape)
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
                .background(bgColor)
                .border(1.dp, borderColor.copy(alpha = 0.6f), btnShape)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

// ── Stage 3: Audit result per-app ────────────────────────────────────────────

@Composable
private fun PhoneAuditResult(
    app: AuditApp,
    results: List<PermissionAuditResult>,
    onNextApp: () -> Unit
) {
    Icon(
        imageVector = PhosphorIcons.Regular.Clipboard,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(28.dp)
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = "Laporan Audit",
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.White
    )
    Text(
        text = app.name,
        fontFamily = DmSansFontFamily,
        fontSize = 11.sp,
        color = Color.White.copy(alpha = 0.6f)
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Result list
    val resultShape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(resultShape)
            .background(Color.White.copy(alpha = 0.08f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        results.forEach { result ->
            val correct = result.isCorrect == true
            val emoji = permissionEmoji(result.permission.name)
            val statusEmoji = if (correct) "✅" else "❌"
            val statusText = if (result.permission.isSuspicious) {
                if (correct) "Mencurigakan — Benar ditolak!" else "Mencurigakan — Seharusnya ditolak!"
            } else {
                if (correct) "Wajar — Benar diizinkan!" else "Wajar — Seharusnya diizinkan!"
            }

            val rowBg = if (correct) Color(0xFF1A3A25) else Color(0xFF3A1A1A)
            val rowBorder = if (correct) Color(0xFF4A8B62) else Color(0xFF8B4A4A)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(rowBg)
                    .border(1.dp, rowBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$emoji $statusEmoji", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.permission.name,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
                    Text(
                        text = statusText,
                        fontFamily = DmSansFontFamily,
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    // Reason from API
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "💡 ${app.reason}",
        fontFamily = DmSansFontFamily,
        fontSize = 11.sp,
        fontStyle = FontStyle.Italic,
        color = Color.White.copy(alpha = 0.6f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Next app button
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val btnOffsetY by animateDpAsState(
        targetValue = if (isPressed) 0.dp else (-2).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nextBtnOffset"
    )

    val btnShape = RoundedCornerShape(CunnyDimens.radiusFull)
    Box(
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .padding(bottom = 2.dp)
            .background(CunnyColors.primaryShadow, shape = btnShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onNextApp
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = btnOffsetY)
                .clip(btnShape)
                .background(brush = Brush.verticalGradient(CunnyColors.gradPlum))
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Lanjut →",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color.White
            )
        }
    }
}

// ── Final score panel ────────────────────────────────────────────────────────

@Composable
private fun FinalScorePanel(
    apps: List<AuditApp>,
    allResults: List<List<PermissionAuditResult>>,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val totalPerms = allResults.sumOf { it.size }
    val correctCount = allResults.sumOf { appResults -> appResults.count { it.isCorrect == true } }

    val scoreEmoji = when {
        correctCount == totalPerms -> "🏆"
        correctCount >= totalPerms * 0.7 -> "👍"
        else -> "💪"
    }

    val scoreMessage = when {
        correctCount == totalPerms -> "Sempurna! Kamu Detektif Data sejati!"
        correctCount >= totalPerms * 0.7 -> "Bagus! Kamu cukup jeli mengenali izin mencurigakan."
        else -> "Terus berlatih! Selalu waspada terhadap izin yang tidak masuk akal."
    }

    LaunchedEffect(Unit) {
        onWidgetCompleted(true)
    }

    val resultShape = RoundedCornerShape(CunnyDimens.radiusMd)

    // 3D tactile result card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 3.dp)
            .background(CunnyColors.primaryShadow, shape = resultShape)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-3).dp)
                .clip(resultShape)
                .background(CunnyColors.primaryPale)
                .border(1.dp, CunnyColors.primary.copy(alpha = 0.24f), resultShape)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = scoreEmoji, fontSize = 36.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Skor Audit: $correctCount/$totalPerms",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = CunnyColors.textDark
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

            // Per-app summary
            apps.forEachIndexed { idx, app ->
                val appResults = allResults.getOrNull(idx) ?: return@forEachIndexed
                val appCorrect = appResults.count { it.isCorrect == true }
                val suspicious = appResults.count { it.permission.isSuspicious }
                val caughtSuspicious = appResults.count { it.permission.isSuspicious && it.isCorrect == true }

                val rowBg = if (appCorrect == appResults.size) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                val rowBorder = if (appCorrect == appResults.size) Color(0xFF81C784) else Color(0xFFFFB74D)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(rowBg)
                        .border(1.dp, rowBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = app.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CunnyColors.textDark
                        )
                        Text(
                            text = if (suspicious > 0)
                                "Menangkap $caughtSuspicious/$suspicious izin mencurigakan"
                            else
                                "Semua izin wajar — $appCorrect/${appResults.size} benar",
                            fontFamily = DmSansFontFamily,
                            fontSize = 10.sp,
                            color = CunnyColors.textSubtle
                        )
                    }
                    Text(
                        text = if (appCorrect == appResults.size) "✅" else "⚠️",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
