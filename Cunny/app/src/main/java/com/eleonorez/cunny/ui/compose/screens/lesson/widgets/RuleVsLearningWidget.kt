package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.fill.Rabbit
import com.adamglin.phosphoricons.fill.Carrot
import com.adamglin.phosphoricons.fill.Warning
import com.adamglin.phosphoricons.fill.WarningCircle
import com.adamglin.phosphoricons.fill.XCircle
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.fill.Clock
import com.adamglin.phosphoricons.fill.Lightbulb
import com.adamglin.phosphoricons.regular.Square
import com.adamglin.phosphoricons.bold.ArrowUp
import com.adamglin.phosphoricons.bold.ArrowBendDownLeft
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private enum class SimulationState {
    IDLE, RUNNING, SUCCESS, FAILED
}

private enum class MazeMode(val displayName: String) {
    MANUAL("Aturan Kaku"),
    AI("Belajar Sendiri (AI)")
}

@Composable
fun RuleVsLearningWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var selectedMode by remember { mutableStateOf(MazeMode.MANUAL) }
    var simState by remember { mutableStateOf(SimulationState.IDLE) }
    var currentRabbitRow by remember { mutableStateOf(0) }
    var currentRabbitCol by remember { mutableStateOf(0) }
    val discoveredObstacles = remember { mutableStateListOf<Pair<Int, Int>>() }
    var currentTrial by remember { mutableStateOf(1) }
    var liveLogs by remember { mutableStateOf("") }

    val shakeOffset = remember { Animatable(0f) }

    val animRow by animateFloatAsState(
        targetValue = currentRabbitRow.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "rabbitRow"
    )
    val animCol by animateFloatAsState(
        targetValue = currentRabbitCol.toFloat(),
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "rabbitCol"
    )

    fun resetSimulation() {
        currentRabbitRow = 0
        currentRabbitCol = 0
        simState = SimulationState.IDLE
        discoveredObstacles.clear()
        currentTrial = 1
        liveLogs = if (selectedMode == MazeMode.MANUAL) {
            "Sistem siap. Menunggu perintah jalan..."
        } else {
            "AI Siap. Menunggu inisialisasi trial-and-error..."
        }
    }

    LaunchedEffect(selectedMode) {
        resetSimulation()
    }

    LaunchedEffect(simState, selectedMode) {
        if (simState == SimulationState.RUNNING) {
            if (selectedMode == MazeMode.MANUAL) {
                liveLogs = "Menjalankan aturan kaku: MAJU_LURUS..."
                delay(300)
                currentRabbitRow = 1
                currentRabbitCol = 0
                delay(300)

                shakeOffset.snapTo(0f)
                shakeOffset.animateTo(6f, tween(50))
                shakeOffset.animateTo(-6f, tween(50))
                shakeOffset.animateTo(4f, tween(50))
                shakeOffset.animateTo(-4f, tween(50))
                shakeOffset.animateTo(0f, tween(50))

                liveLogs = "Gagal! Aturan kaku ini hanya disetting untuk satu arah rute. Kelinci menabrak rintangan di (1,0)!"
                simState = SimulationState.FAILED
            } else {
                discoveredObstacles.clear()

                // --- TRIAL 1 ---
                currentTrial = 1
                liveLogs = "Trial 1: Eksplorasi lurus..."
                delay(300)
                currentRabbitRow = 1
                currentRabbitCol = 0
                delay(300)

                shakeOffset.snapTo(0f)
                shakeOffset.animateTo(6f, tween(50))
                shakeOffset.animateTo(-6f, tween(50))
                shakeOffset.animateTo(0f, tween(50))
                discoveredObstacles.add(1 to 0)
                liveLogs = "Tabrakan di (1,0)! Menandai lokasi & kembali ke start."
                delay(500)

                currentRabbitRow = 0
                currentRabbitCol = 0
                delay(400)

                // --- TRIAL 2 ---
                currentTrial = 2
                liveLogs = "Trial 2: Mencoba arah alternatif (Kanan)..."
                val trial2Path = listOf(0 to 1, 1 to 1, 2 to 1, 2 to 2)
                for (pos in trial2Path) {
                    delay(300)
                    currentRabbitRow = pos.first
                    currentRabbitCol = pos.second
                }
                delay(300)

                shakeOffset.snapTo(0f)
                shakeOffset.animateTo(6f, tween(50))
                shakeOffset.animateTo(-6f, tween(50))
                shakeOffset.animateTo(0f, tween(50))
                discoveredObstacles.add(2 to 2)
                liveLogs = "Tabrakan di (2,2)! Menghindari jalur ini dan mundur ke (1,1)."
                delay(500)

                currentRabbitRow = 1
                currentRabbitCol = 1
                delay(400)

                // --- TRIAL 3 ---
                currentTrial = 3
                liveLogs = "Trial 3: Menghindari rute memori merah..."
                val trial3Path = listOf(1 to 2, 1 to 3, 2 to 3, 3 to 3)
                for (pos in trial3Path) {
                    delay(300)
                    currentRabbitRow = pos.first
                    currentRabbitCol = pos.second
                }
                delay(300)

                liveLogs = "Sukses! Kelinci AI berhasil mencapai wortel dengan belajar menghindari rintangan."
                simState = SimulationState.SUCCESS
                onWidgetCompleted(true)
            }
        }
    }

    fun startSimulation() {
        simState = SimulationState.RUNNING
        currentRabbitRow = 0
        currentRabbitCol = 0
    }

    // ═══════════════════════════════════════════════════════════════════
    // SINGLE 3D outer container — same pattern as ClaimDetectiveWidget
    // ═══════════════════════════════════════════════════════════════════
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
                // Header — Phosphor Icon instead of emoji
                Icon(
                    imageVector = PhosphorIcons.Fill.Rabbit,
                    contentDescription = "Rabbit mascot",
                    tint = CunnyColors.primary,
                    modifier = Modifier.size(40.dp).padding(bottom = 4.dp)
                )
                Text(
                    text = "Aturan vs Belajar (Labirin)",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bandingkan kelinci aturan manual dengan kelinci AI yang belajar dari data.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // ── Tab Selector 3D ──────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MazeMode.entries.forEach { mode ->
                        val isSelected = selectedMode == mode
                        
                        val offsetY by animateDpAsState(
                            targetValue = if (isSelected) 0.dp else (-3).dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "tabOffset"
                        )
                        
                        val tabShape = RoundedCornerShape(CunnyDimens.radiusFull)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            if (!isSelected) {
                                // Shadow layer for unselected raised tab
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .height(38.dp)
                                        .background(CunnyColors.border, shape = tabShape)
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = offsetY)
                                    .height(38.dp)
                                    .clip(tabShape)
                                    .background(
                                        if (isSelected) 
                                            Brush.linearGradient(CunnyColors.gradPlum) 
                                        else 
                                            Brush.linearGradient(
                                                listOf(
                                                    CunnyColors.backgroundSoft,
                                                    CunnyColors.backgroundSoft
                                                )
                                            )
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else CunnyColors.border,
                                        shape = tabShape
                                    )
                                    .clickable { selectedMode = mode },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) CunnyColors.textOnDarkSurface else CunnyColors.textBody
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Info Panel (flat, subtle bg, thin border) ─────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CunnyDimens.radiusMd))
                        .background(CunnyColors.primaryPale.copy(alpha = 0.4f))
                        .border(1.dp, CunnyColors.primary.copy(alpha = 0.12f), RoundedCornerShape(CunnyDimens.radiusMd))
                        .padding(14.dp)
                ) {
                    if (selectedMode == MazeMode.MANUAL) {
                        ManualModePanel(isDark = isDark)
                    } else {
                        AiModePanel(
                            currentTrial = currentTrial,
                            rabbitRow = currentRabbitRow,
                            rabbitCol = currentRabbitCol,
                            discoveredObstacles = discoveredObstacles,
                            isDark = isDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── 4x4 Grid Maze (clean flat tiles) ─────────────────────
                // Dark-mode-aware cell colors
                val cellDefault = if (isDark) CunnyColors.backgroundWarm.copy(alpha = 0.5f) else CunnyColors.backgroundSoft
                val cellCarrot = if (isDark) Color(0xFF1A3A25).copy(alpha = 0.6f) else Color(0xFFE8F5E9)
                val cellObstacle = if (isDark) CunnyColors.accentRed.copy(alpha = 0.06f) else Color(0xFFFFF5F5)
                val cellDiscovered = if (isDark) CunnyColors.accentRed.copy(alpha = 0.15f) else Color(0xFFFDE8E8)
                val cellCarrotBorder = if (isDark) Color(0xFF4A8B62).copy(alpha = 0.5f) else Color(0xFF81C784).copy(alpha = 0.4f)

                Box(modifier = Modifier.size(164.dp)) {
                    // Grid cells
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        for (r in 0 until 4) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (c in 0 until 4) {
                                    val isCarrot = r == 3 && c == 3
                                    val isObstacle = (r == 1 && c == 0) || (r == 2 && c == 2)
                                    val isDiscovered = discoveredObstacles.contains(r to c)

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when {
                                                    isDiscovered -> cellDiscovered
                                                    isObstacle -> cellObstacle
                                                    isCarrot -> cellCarrot
                                                    else -> cellDefault
                                                }
                                            )
                                            .border(
                                                width = if (isDiscovered) 1.5.dp else 1.dp,
                                                color = when {
                                                    isDiscovered -> CunnyColors.accentRed.copy(alpha = 0.4f)
                                                    isCarrot -> cellCarrotBorder
                                                    else -> CunnyColors.borderLight
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when {
                                            isCarrot -> Icon(
                                                imageVector = PhosphorIcons.Fill.Carrot,
                                                contentDescription = "Goal",
                                                tint = if (isDark) Color(0xFF81C784) else Color(0xFF4A8B62),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            isDiscovered -> Icon(
                                                imageVector = PhosphorIcons.Fill.XCircle,
                                                contentDescription = "Blocked",
                                                tint = CunnyColors.accentRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            isObstacle -> Icon(
                                                imageVector = PhosphorIcons.Fill.Warning,
                                                contentDescription = "Obstacle",
                                                tint = CunnyColors.accentRed.copy(alpha = 0.5f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Rabbit floating overlay (clean highlight, no nested 3D)
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = (animCol * 42.dp.toPx() + shakeOffset.value.dp.toPx()).roundToInt(),
                                    y = (animRow * 42.dp.toPx()).roundToInt()
                                )
                            }
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CunnyColors.primaryPale)
                            .border(2.dp, CunnyColors.primary, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Fill.Rabbit,
                            contentDescription = "Rabbit",
                            tint = CunnyColors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ── Simulation Feedback ───────────────────────────────────
                val fbBg = when (simState) {
                    SimulationState.SUCCESS -> CunnyColors.primaryPale.copy(alpha = 0.5f)
                    SimulationState.FAILED -> if (isDark) CunnyColors.accentRed.copy(alpha = 0.12f) else Color(0xFFFDE8E8)
                    else -> CunnyColors.backgroundSoft.copy(alpha = if (isDark) 0.5f else 0.8f)
                }
                val fbBorder = when (simState) {
                    SimulationState.SUCCESS -> CunnyColors.primary.copy(alpha = 0.2f)
                    SimulationState.FAILED -> CunnyColors.accentRed.copy(alpha = 0.2f)
                    else -> CunnyColors.borderLight
                }
                val fbTextColor = when (simState) {
                    SimulationState.SUCCESS -> CunnyColors.primary
                    SimulationState.FAILED -> CunnyColors.accentRed
                    else -> CunnyColors.textBody
                }
                val fbIcon = when (simState) {
                    SimulationState.SUCCESS -> PhosphorIcons.Fill.CheckCircle
                    SimulationState.FAILED -> PhosphorIcons.Fill.XCircle
                    else -> PhosphorIcons.Fill.Clock
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(CunnyDimens.radiusMd))
                        .background(fbBg)
                        .border(1.dp, fbBorder, RoundedCornerShape(CunnyDimens.radiusMd))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = fbIcon,
                            contentDescription = null,
                            tint = fbTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = liveLogs,
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = fbTextColor,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Controls ─────────────────────────────────────────────
                when (simState) {
                    SimulationState.IDLE -> {
                        CunnyPrimaryButton(
                            text = "Jalankan Simulasi",
                            onClick = { startSimulation() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SimulationState.RUNNING -> {
                        CunnyPrimaryButton(
                            text = "Simulasi Berjalan...",
                            onClick = { },
                            isLoading = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    else -> {
                        CunnyPrimaryButton(
                            text = "Ulangi Simulasi",
                            onClick = { resetSimulation() },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Manual Mode Panel — flat code blocks with Phosphor icons
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ManualModePanel(isDark: Boolean) {
    Text(
        text = "Aturan Bahasa Natural (Scratch-Style):",
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = CunnyColors.primary
    )
    Spacer(modifier = Modifier.height(3.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Fill.Lightbulb,
            contentDescription = null,
            tint = CunnyColors.accentOrange,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Aturan ini disetting hanya untuk satu arah rute lurus:",
            fontFamily = DmSansFontFamily,
            fontSize = 11.sp,
            color = CunnyColors.textSubtle
        )
    }
    Spacer(modifier = Modifier.height(10.dp))

    // Code block 1: MAJU LURUS — flat colored row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CunnyColors.primaryPale)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Square,
                contentDescription = null,
                tint = CunnyColors.textSubtle,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "JIKA jalan depan bersih:",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = CunnyColors.textDark
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CunnyColors.primary)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "MAJU LURUS",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = CunnyColors.textOnDarkSurface
                )
                Icon(
                    imageVector = PhosphorIcons.Bold.ArrowUp,
                    contentDescription = null,
                    tint = CunnyColors.textOnDarkSurface,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Code block 2: BELOK KIRI — flat danger row
    val dangerBg = if (isDark) CunnyColors.accentRed.copy(alpha = 0.12f) else Color(0xFFFDE8E8)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(dangerBg)
            .border(1.dp, CunnyColors.accentRed.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Fill.Warning,
                contentDescription = null,
                tint = CunnyColors.accentRed,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "JIKA ada rintangan:",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = CunnyColors.textDark
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CunnyColors.accentRed)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "BELOK KIRI",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = CunnyColors.textOnDarkSurface
                )
                Icon(
                    imageVector = PhosphorIcons.Bold.ArrowBendDownLeft,
                    contentDescription = null,
                    tint = CunnyColors.textOnDarkSurface,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Fill.WarningCircle,
            contentDescription = null,
            tint = CunnyColors.accentRed,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "Kelinci akan menabrak karena rintangan di (1,0) tidak bisa dihindari dengan aturan satu arah ini.",
            fontFamily = DmSansFontFamily,
            fontSize = 10.sp,
            color = CunnyColors.accentRed,
            lineHeight = 14.sp
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// AI Mode Panel — clean metric pills with Phosphor icons
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun AiModePanel(
    currentTrial: Int,
    rabbitRow: Int,
    rabbitCol: Int,
    discoveredObstacles: List<Pair<Int, Int>>,
    isDark: Boolean
) {
    Text(
        text = "AI Trial-and-Error Metrics:",
        fontFamily = SoraFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = CunnyColors.primary
    )
    Spacer(modifier = Modifier.height(3.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = PhosphorIcons.Fill.Lightbulb,
            contentDescription = null,
            tint = CunnyColors.accentOrange,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "AI mencoba rute berulang-ulang untuk mengingat letak rintangan:",
            fontFamily = DmSansFontFamily,
            fontSize = 11.sp,
            color = CunnyColors.textSubtle
        )
    }
    Spacer(modifier = Modifier.height(10.dp))

    // Metric pills row — flat, no 3D
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Trial pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(CunnyDimens.radiusFull))
                .background(CunnyColors.primaryPale)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "Trial $currentTrial",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = CunnyColors.primary
            )
        }
        // Position pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(CunnyDimens.radiusFull))
                .background(CunnyColors.primaryPale)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = PhosphorIcons.Fill.Rabbit,
                    contentDescription = null,
                    tint = CunnyColors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "($rabbitRow, $rabbitCol)",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = CunnyColors.primary
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Memory Section
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Memori:",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = CunnyColors.textSubtle
        )
        if (discoveredObstacles.isEmpty()) {
            Text(
                text = "kosong",
                fontFamily = DmSansFontFamily,
                fontSize = 11.sp,
                color = CunnyColors.textSubtle
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val memBg = if (isDark) CunnyColors.accentRed.copy(alpha = 0.12f) else Color(0xFFFDE8E8)
                discoveredObstacles.forEach { obstacle ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(memBg)
                            .border(1.dp, CunnyColors.accentRed.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Fill.Warning,
                                contentDescription = null,
                                tint = CunnyColors.accentRed,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "(${obstacle.first},${obstacle.second})",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = CunnyColors.accentRed
                            )
                            Icon(
                                imageVector = PhosphorIcons.Fill.XCircle,
                                contentDescription = null,
                                tint = CunnyColors.accentRed,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
