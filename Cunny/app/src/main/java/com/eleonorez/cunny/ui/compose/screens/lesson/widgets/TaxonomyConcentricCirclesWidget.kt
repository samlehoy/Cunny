package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.screens.lesson.parseMarkdownToAnnotatedString
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlin.math.roundToInt
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.foundation.layout.fillMaxSize

// Map level IDs to Cool Violet ring colors (matches concentric circle colors)
private val levelColorMap = mapOf(
    "ai" to Color(0xFFB3ABFA),      // Lightest violet (outer ring)
    "ml" to Color(0xFF8B7FF2),      // Medium violet
    "dl" to Color(0xFF6C5CE7),      // Primary violet
    "genai" to Color(0xFF4A3FB5)    // Deep violet (inner ring)
)

data class TaxonomyLevel(
    val id: String,
    val name: String,
    val description: String,
    val color: String
)

@Composable
fun TaxonomyConcentricCirclesWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val levelsRaw = block.config?.get("levels") as? List<Map<String, Any>>
    val levels = remember(levelsRaw) {
        levelsRaw?.map { lMap ->
            val id = lMap["id"] as? String ?: ""
            val name = lMap["name"] as? String ?: ""
            val desc = lMap["short_desc"] as? String ?: ""
            val colorStr = lMap["color"] as? String ?: "#6C5CE7"
            TaxonomyLevel(id, name, desc, colorStr)
        } ?: emptyList()
    }

    val contentData = remember(levels) {
        levels.associateBy { it.id }
    }

    var selectedLabelId by remember { mutableStateOf<String?>(null) }
    val placedLevels = remember { mutableStateListOf<String>() }
    var mascotMessage by remember { mutableStateOf("\"Coba seret label di bawah ke lingkaran yang tepat! Atau klik label lalu klik lingkaran target.\"") }
    
    var detailsTitle by remember { mutableStateOf("Tempatkan label untuk belajar") }
    var detailsDesc by remember { mutableStateOf("Silakan seret label di atas ke lingkaran yang tepat atau gunakan klik untuk memasangkannya.") }
    val primaryColor = CunnyColors.primary

    var detailsTitleColor by remember { mutableStateOf(primaryColor) }

    val completed = placedLevels.size >= levels.size

    LaunchedEffect(completed) {
        if (completed) {
            onWidgetCompleted(true)
        }
    }

    // Next target ID for pulse glow
    val order = listOf("ai", "ml", "dl", "genai")
    val nextTargetId = order.find { it !in placedLevels }

    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val circleBounds = remember { mutableMapOf<String, androidx.compose.ui.geometry.Rect>() }

    fun wrongPlace() {
        mascotMessage = "\"Ups, belum tepat! Ingat: lingkaran yang lebih dalam melambangkan teknologi yang lebih spesifik.\""
        selectedLabelId = null
    }

    fun showInfo(id: String) {
        val level = contentData[id] ?: return
        detailsTitle = level.name
        detailsDesc = level.description
        detailsTitleColor = levelColorMap[id] ?: primaryColor
        mascotMessage = "\"Kamu mempelajari tentang ${level.name}.\""
    }

    fun successPlace(id: String) {
        if (!placedLevels.contains(id)) {
            placedLevels.add(id)
        }
        selectedLabelId = null
        showInfo(id)
        mascotMessage = "\"Hebat! Kamu benar!\""
        if (placedLevels.size == levels.size) {
            mascotMessage = "\"Luar biasa! Semua bagian keluarga AI sudah tepat di posisinya!\""
        }
    }

    fun onCircleClicked(circleId: String) {
        if (placedLevels.contains(circleId)) {
            showInfo(circleId)
            return
        }
        if (selectedLabelId != null) {
            if (selectedLabelId == circleId && circleId == nextTargetId) {
                successPlace(circleId)
            } else {
                wrongPlace()
            }
        }
    }

    fun handleDragEnd(levelId: String, globalDropPos: Offset) {
        val targetBounds = circleBounds[levelId]
        if (targetBounds != null && targetBounds.contains(globalDropPos)) {
            successPlace(levelId)
        } else {
            val hitCircleId = circleBounds.entries.find { it.value.contains(globalDropPos) }?.key
            if (hitCircleId != null) {
                wrongPlace()
            }
        }
    }

    GlassSurface(
        shape = RoundedCornerShape(CunnyDimens.radiusLg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CunnyDimens.radiusLg))
                    .background(CunnyColors.primary.copy(alpha = 0.08f))
                    .border(1.dp, CunnyColors.primary.copy(alpha = 0.15f), RoundedCornerShape(CunnyDimens.radiusLg))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mascotComposition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))
                Box(modifier = Modifier.size(28.dp)) {
                    LottieAnimation(
                        composition = mascotComposition,
                        iterations = LottieConstants.IterateForever,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cunny berkata:",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = CunnyColors.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = mascotMessage,
                        fontFamily = DmSansFontFamily,
                        fontSize = 11.sp,
                        color = CunnyColors.textBody,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.size(280.dp),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val dashStroke = remember(density) {
                    val widthPx = with(density) { 3.dp.toPx() }
                    val dashPx = with(density) { 10.dp.toPx() }
                    Stroke(
                        width = widthPx,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, dashPx), 0f)
                    )
                }

                // 1. AI Circle (Outermost, 270dp)
                val isAiPlaced = placedLevels.contains("ai")
                val isAiPulse = nextTargetId == "ai"
                val aiScale = if (isAiPulse) pulseScale else 1f
                Box(
                    modifier = Modifier
                        .size(270.dp)
                        .graphicsLayer(scaleX = aiScale, scaleY = aiScale)
                        .onGloballyPositioned { coords ->
                            circleBounds["ai"] = coords.boundsInRoot()
                        }
                        .clip(CircleShape)
                        .background(if (isAiPlaced) Color(0xFFB3ABFA).copy(alpha = 0.25f) else Color(0xFFB3ABFA).copy(alpha = 0.03f))
                        .then(
                            if (isAiPlaced) Modifier.shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                clip = false,
                                ambientColor = Color(0xFFB3ABFA),
                                spotColor = Color(0xFFB3ABFA)
                            ) else Modifier
                        )
                        .drawBehind {
                            if (!isAiPlaced) {
                                drawCircle(
                                    color = Color(0xFFB3ABFA),
                                    radius = (size.minDimension / 2f) - 1.5.dp.toPx(),
                                    style = dashStroke
                                )
                            }
                        }
                        .then(
                            if (isAiPlaced) Modifier.border(4.dp, Color(0xFFB3ABFA), CircleShape)
                            else Modifier
                        )
                        .clickable { onCircleClicked("ai") }
                )

                // 2. ML Circle (200dp)
                val isMlPlaced = placedLevels.contains("ml")
                val isMlPulse = nextTargetId == "ml"
                val mlScale = if (isMlPulse) pulseScale else 1f
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer(scaleX = mlScale, scaleY = mlScale)
                        .onGloballyPositioned { coords ->
                            circleBounds["ml"] = coords.boundsInRoot()
                        }
                        .clip(CircleShape)
                        .background(if (isMlPlaced) Color(0xFF8B7FF2).copy(alpha = 0.30f) else Color(0xFF8B7FF2).copy(alpha = 0.04f))
                        .drawBehind {
                            if (!isMlPlaced) {
                                drawCircle(
                                    color = Color(0xFF8B7FF2),
                                    radius = (size.minDimension / 2f) - 1.5.dp.toPx(),
                                    style = dashStroke
                                )
                            }
                        }
                        .then(
                            if (isMlPlaced) Modifier.border(4.dp, Color(0xFF8B7FF2), CircleShape)
                            else Modifier
                        )
                        .clickable { onCircleClicked("ml") }
                )

                // 3. DL Circle (130dp)
                val isDlPlaced = placedLevels.contains("dl")
                val isDlPulse = nextTargetId == "dl"
                val dlScale = if (isDlPulse) pulseScale else 1f
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .graphicsLayer(scaleX = dlScale, scaleY = dlScale)
                        .onGloballyPositioned { coords ->
                            circleBounds["dl"] = coords.boundsInRoot()
                        }
                        .clip(CircleShape)
                        .background(if (isDlPlaced) Color(0xFF6C5CE7).copy(alpha = 0.35f) else Color(0xFF6C5CE7).copy(alpha = 0.05f))
                        .drawBehind {
                            if (!isDlPlaced) {
                                drawCircle(
                                    color = Color(0xFF6C5CE7),
                                    radius = (size.minDimension / 2f) - 1.5.dp.toPx(),
                                    style = dashStroke
                                )
                            }
                        }
                        .then(
                            if (isDlPlaced) Modifier.border(4.dp, Color(0xFF6C5CE7), CircleShape)
                            else Modifier
                        )
                        .clickable { onCircleClicked("dl") }
                )

                // 4. GenAI Circle (Innermost, 66dp)
                val isGenAiPlaced = placedLevels.contains("genai")
                val isGenAiPulse = nextTargetId == "genai"
                val genAiScale = if (isGenAiPulse) pulseScale else 1f
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .graphicsLayer(scaleX = genAiScale, scaleY = genAiScale)
                        .onGloballyPositioned { coords ->
                            circleBounds["genai"] = coords.boundsInRoot()
                        }
                        .clip(CircleShape)
                        .background(if (isGenAiPlaced) Color(0xFF4A3FB5).copy(alpha = 0.50f) else Color(0xFF4A3FB5).copy(alpha = 0.08f))
                        .drawBehind {
                            if (!isGenAiPlaced) {
                                drawCircle(
                                    color = Color(0xFF4A3FB5),
                                    radius = (size.minDimension / 2f) - 1.5.dp.toPx(),
                                    style = dashStroke
                                )
                            }
                        }
                        .then(
                            if (isGenAiPlaced) Modifier.border(4.dp, Color(0xFF4A3FB5), CircleShape)
                            else Modifier
                        )
                        .clickable { onCircleClicked("genai") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "GEN\nAI",
                        fontFamily = SoraFontFamily,
                        fontWeight = if (isGenAiPlaced) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isGenAiPlaced) CunnyColors.textOnPrimary else CunnyColors.textSubtle,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }

                // Overlays
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer(scaleX = aiScale, scaleY = aiScale),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "KECERDASAN BUATAN (AI)",
                        fontFamily = SoraFontFamily,
                        fontWeight = if (isAiPlaced) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isAiPlaced) CunnyColors.primary else CunnyColors.textSubtle,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .background(CunnyColors.glassBg.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer(scaleX = mlScale, scaleY = mlScale),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "MACHINE LEARNING (ML)",
                        fontFamily = SoraFontFamily,
                        fontWeight = if (isMlPlaced) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 8.6.sp,
                        color = if (isMlPlaced) CunnyColors.primary else CunnyColors.textSubtle,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(top = 50.dp)
                            .background(CunnyColors.glassBg.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .graphicsLayer(scaleX = dlScale, scaleY = dlScale),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        text = "DEEP LEARNING (DL)",
                        fontFamily = SoraFontFamily,
                        fontWeight = if (isDlPlaced) FontWeight.ExtraBold else FontWeight.Bold,
                        fontSize = 8.sp,
                        color = if (isDlPlaced) CunnyColors.primary else CunnyColors.textSubtle,
                        letterSpacing = 0.5.sp,
                        modifier = Modifier
                            .padding(top = 85.dp)
                            .background(CunnyColors.glassBg.copy(alpha = 0.72f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val chunks = levels.chunked(2)
                chunks.forEach { rowLevels ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        rowLevels.forEach { level ->
                            val isPlaced = placedLevels.contains(level.id)
                            val isSelected = selectedLabelId == level.id
                            DraggableLabel(
                                level = level,
                                isSelected = isSelected,
                                isPlaced = isPlaced,
                                onClick = {
                                    selectedLabelId = level.id
                                    mascotMessage = "\"Ketuk lingkaran yang berkedip untuk menempatkan label ini!\""
                                    detailsTitle = "Label dipilih: ${level.name}"
                                    detailsTitleColor = parseColorSafe(level.color, primaryColor)
                                    detailsDesc = "Ketuk lingkaran yang berkedip untuk menempatkan label ini!"
                                },
                                onDragEnd = { globalDropPos ->
                                    handleDragEnd(level.id, globalDropPos)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CunnyColors.glassBg)
                    .border(1.dp, CunnyColors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = detailsTitle,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = detailsTitleColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = parseMarkdownToAnnotatedString(detailsDesc, CunnyColors.textDark),
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textBody,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun DraggableLabel(
    level: TaxonomyLevel,
    isSelected: Boolean,
    isPlaced: Boolean,
    onClick: () -> Unit,
    onDragEnd: (Offset) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var initialCenter by remember { mutableStateOf(Offset.Zero) }
    var labelCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val labelColor = levelColorMap[level.id] ?: CunnyColors.primary
    val btnScale = if (isSelected || isDragging) 1.1f else 1.0f

    val shadowModifier = if (isSelected || isDragging) {
        Modifier.shadow(
            elevation = 6.dp,
            shape = RoundedCornerShape(16.dp),
            clip = false,
            ambientColor = labelColor,
            spotColor = labelColor
        )
    } else Modifier

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .graphicsLayer(scaleX = btnScale, scaleY = btnScale)
            .then(shadowModifier)
            .onGloballyPositioned { coords ->
                labelCoords = coords
            }
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPlaced) CunnyColors.backgroundSoft else CunnyColors.backgroundWarm)
            .border(
                width = if (isSelected || isDragging) 2.dp else 1.5.dp,
                color = if (isSelected || isDragging) labelColor else labelColor.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .pointerInput(isPlaced) {
                if (isPlaced) return@pointerInput
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        onClick()
                        labelCoords?.let { coords ->
                            if (coords.isAttached) {
                                initialCenter = coords.boundsInRoot().center
                            }
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    },
                    onDragEnd = {
                        isDragging = false
                        val finalCenter = initialCenter + Offset(offsetX, offsetY)
                        onDragEnd(finalCenter)
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDragCancel = {
                        isDragging = false
                        offsetX = 0f
                        offsetY = 0f
                    }
                )
            }
            .clickable(enabled = !isPlaced) { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = level.name,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = if (isPlaced) CunnyColors.textSubtle else labelColor,
            textAlign = TextAlign.Center
        )
    }
}

private fun parseColorSafe(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}
