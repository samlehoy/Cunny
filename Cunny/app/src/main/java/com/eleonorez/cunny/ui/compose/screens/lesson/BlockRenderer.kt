package com.eleonorez.cunny.ui.compose.screens.lesson

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.Block
import com.eleonorez.cunny.data.model.CalloutBlock
import com.eleonorez.cunny.data.model.ImageBlock
import com.eleonorez.cunny.data.model.QuizBlock
import com.eleonorez.cunny.data.model.TextBlock
import com.eleonorez.cunny.data.model.VideoBlock
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontStyle
import com.eleonorez.cunny.helper.SoundSynthesizer
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Lightbulb
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.ChalkboardTeacher
import com.adamglin.phosphoricons.regular.CirclesThree
import com.eleonorez.cunny.ui.compose.screens.lesson.widgets.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.rotate

@Composable
fun BlockRenderer(
    block: Block,
    lang: String = "id",
    onBlockCompleted: (Boolean) -> Unit,
    onOpenWidget: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when (block) {
            is TextBlock -> TextBlockRenderer(block = block)
            is ImageBlock -> ImageBlockRenderer(block = block)
            is CalloutBlock -> CalloutBlockRenderer(block = block)
            is QuizBlock -> QuizBlockRenderer(block = block, onQuizCorrect = onBlockCompleted)
            is WidgetBlock -> WidgetBlockRenderer(block = block, lang = lang, onOpenWidget = onOpenWidget, onWidgetCompleted = onBlockCompleted)
            is VideoBlock -> Spacer(Modifier.size(0.dp))
        }
    }
}

fun parseMarkdownToAnnotatedString(
    text: String,
    textColor: Color,
    codeColor: Color = Color(0xFF6C5CE7),
    codeBgColor: Color = Color(0xFFF4F1F6)
): AnnotatedString {
    val cleanedText = text.replace("\\n", "\n")
    val lines = cleanedText.split("\n")
    return buildAnnotatedString {
        lines.forEachIndexed { lineIndex, line ->
            val isHeader3 = line.trimStart().startsWith("### ")
            val isHeader2 = line.trimStart().startsWith("## ")
            val isHeader1 = line.trimStart().startsWith("# ")
            
            val (cleanLine, headerStyle) = when {
                isHeader3 -> {
                    line.trimStart().removePrefix("### ") to SpanStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SoraFontFamily,
                        color = textColor
                    )
                }
                isHeader2 -> {
                    line.trimStart().removePrefix("## ") to SpanStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SoraFontFamily,
                        color = textColor
                    )
                }
                isHeader1 -> {
                    line.trimStart().removePrefix("# ") to SpanStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SoraFontFamily,
                        color = textColor
                    )
                }
                else -> line to null
            }
            
            if (headerStyle != null) {
                withStyle(style = headerStyle) {
                    append(parseInlineMarkdown(cleanLine, codeColor, codeBgColor))
                }
            } else {
                append(parseInlineMarkdown(cleanLine, codeColor, codeBgColor))
            }
            
            if (lineIndex < lines.lastIndex) {
                append("\n")
            }
        }
    }
}

fun parseInlineMarkdown(
    text: String,
    codeColor: Color = Color(0xFF6C5CE7),
    codeBgColor: Color = Color(0xFFF4F1F6)
): AnnotatedString {
    val bulletMatches = Regex("^(\\s*)[*•-]\\s+").find(text)
    val numberedMatches = Regex("^(\\s*)(\\d+\\.)\\s+").find(text)
    
    val indentCleaned = when {
        bulletMatches != null -> {
            val spaces = bulletMatches.groupValues[1]
            val indentLevel = when {
                spaces.length >= 4 -> "\u2003\u2003• "
                spaces.length >= 2 -> "\u2003• "
                else -> "• "
            }
            text.replaceFirst(Regex("^\\s*[*•-]\\s+"), indentLevel)
        }
        numberedMatches != null -> {
            val spaces = numberedMatches.groupValues[1]
            val number = numberedMatches.groupValues[2]
            val indentLevel = when {
                spaces.length >= 4 -> "\u2003\u2003$number "
                spaces.length >= 2 -> "\u2003$number "
                else -> "$number "
            }
            text.replaceFirst(Regex("^\\s*\\d+\\.\\s+"), indentLevel)
        }
        else -> text
    }

    val cleanedText = indentCleaned
        .replace("\\rightarrow", "→")
        .replace("\\theta", "θ")
        .replace("$\\rightarrow$", "→")
        .replace("$\\theta$", "θ")
        .replace("\$x\$", "x")
        .replace("\$w\$", "w")
        .replace("\$", "")

    return buildAnnotatedString {
        var currentText = cleanedText
        var index = 0
        while (index < currentText.length) {
            val nextBold = currentText.indexOf("**", index)
            val nextItalicStar = currentText.indexOf("*", index)
            val nextItalicUnderscore = currentText.indexOf("_", index)
            val nextCode = currentText.indexOf("`", index)
            
            var foundMarker = ""
            var markerStart = -1
            var markerEnd = -1
            var minStart = Int.MAX_VALUE
            
            if (nextBold != -1 && nextBold >= index && nextBold < minStart) {
                val closeBold = currentText.indexOf("**", nextBold + 2)
                if (closeBold != -1) {
                    minStart = nextBold
                    foundMarker = "**"
                    markerStart = nextBold
                    markerEnd = closeBold
                }
            }
            if (nextItalicStar != -1 && nextItalicStar >= index && nextItalicStar < minStart) {
                if (nextItalicStar + 1 >= currentText.length || currentText[nextItalicStar + 1] != '*') {
                    val closeItalicStar = currentText.indexOf("*", nextItalicStar + 1)
                    if (closeItalicStar != -1 && (closeItalicStar + 1 >= currentText.length || currentText[closeItalicStar + 1] != '*')) {
                        minStart = nextItalicStar
                        foundMarker = "*"
                        markerStart = nextItalicStar
                        markerEnd = closeItalicStar
                    }
                }
            }
            if (nextItalicUnderscore != -1 && nextItalicUnderscore >= index && nextItalicUnderscore < minStart) {
                val closeItalicUnderscore = currentText.indexOf("_", nextItalicUnderscore + 1)
                if (closeItalicUnderscore != -1) {
                    minStart = nextItalicUnderscore
                    foundMarker = "_"
                    markerStart = nextItalicUnderscore
                    markerEnd = closeItalicUnderscore
                }
            }
            if (nextCode != -1 && nextCode >= index && nextCode < minStart) {
                val closeCode = currentText.indexOf("`", nextCode + 1)
                if (closeCode != -1) {
                    minStart = nextCode
                    foundMarker = "`"
                    markerStart = nextCode
                    markerEnd = closeCode
                }
            }
            
            if (markerStart != -1) {
                append(currentText.substring(index, markerStart))
                val innerText = currentText.substring(markerStart + foundMarker.length, markerEnd)
                
                when (foundMarker) {
                    "**" -> {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parseInlineMarkdown(innerText, codeColor, codeBgColor))
                        }
                    }
                    "*", "_" -> {
                        withStyle(style = SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(parseInlineMarkdown(innerText, codeColor, codeBgColor))
                        }
                    }
                    "`" -> {
                        withStyle(style = SpanStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 13.5.sp,
                            color = codeColor,
                            background = codeBgColor
                        )) {
                            append(innerText)
                        }
                    }
                }
                index = markerEnd + foundMarker.length
            } else {
                append(currentText.substring(index))
                break
            }
        }
    }
}

private fun String.stripEmojis(): String {
    val emojiRegex = "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF\\u2600-\\u27BF\\u2300-\\u23FF\\uFE0F\\u200D]".toRegex()
    return this.replace(emojiRegex, "")
        .replace("  ", " ")
        .trim()
}

@Composable
fun TextBlockRenderer(block: TextBlock) {
    val cleanMarkdown = remember(block.markdown) { block.markdown.stripEmojis() }
    val segments = remember(cleanMarkdown) { splitTextIntoSegments(cleanMarkdown) }
    val textColor = CunnyColors.textDark

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        segments.forEach { segment ->
            when (segment) {
                is TextSegment.PlainText -> {
                    if (segment.text.isNotBlank()) {
                        val annotatedString = remember(segment.text, textColor) {
                            parseMarkdownToAnnotatedString(segment.text, textColor)
                        }
                        Text(
                            text = annotatedString,
                            fontFamily = DmSansFontFamily,
                            fontSize = 15.sp,
                            color = textColor,
                            lineHeight = 24.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is TextSegment.NumberedList -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberedStepCards(items = segment.items)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                is TextSegment.BulletList -> {
                    Spacer(modifier = Modifier.height(4.dp))
                    segment.items.forEach { item ->
                        val annotated = remember(item, textColor) {
                            parseMarkdownToAnnotatedString("• $item", textColor)
                        }
                        Text(
                            text = annotated,
                            fontFamily = DmSansFontFamily,
                            fontSize = 15.sp,
                            color = textColor,
                            lineHeight = 24.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 4.dp, bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

// ── Segment types ────────────────────────────────────────────────────────────

private sealed class TextSegment {
    data class PlainText(val text: String) : TextSegment()
    data class NumberedList(val items: List<NumberedItem>) : TextSegment()
    data class BulletList(val items: List<String>) : TextSegment()
}

private data class NumberedItem(
    val number: Int,
    val content: String
)

// ── Segment parser ───────────────────────────────────────────────────────────

private fun splitTextIntoSegments(markdown: String): List<TextSegment> {
    val cleanedText = markdown.replace("\\n", "\n")
    val lines = cleanedText.split("\n")
    val segments = mutableListOf<TextSegment>()
    val currentPlainLines = mutableListOf<String>()
    val currentNumberedItems = mutableListOf<NumberedItem>()
    val currentBulletItems = mutableListOf<String>()

    val numberedRegex = Regex("^\\s*(\\d+)\\.\\s+(.*)")
    val bulletRegex = Regex("^\\s*[*•-]\\s+(.*)")

    fun flushPlain() {
        if (currentPlainLines.isNotEmpty()) {
            segments.add(TextSegment.PlainText(currentPlainLines.joinToString("\n")))
            currentPlainLines.clear()
        }
    }

    fun flushNumbered() {
        if (currentNumberedItems.isNotEmpty()) {
            segments.add(TextSegment.NumberedList(currentNumberedItems.toList()))
            currentNumberedItems.clear()
        }
    }

    fun flushBullet() {
        if (currentBulletItems.isNotEmpty()) {
            segments.add(TextSegment.BulletList(currentBulletItems.toList()))
            currentBulletItems.clear()
        }
    }

    for (line in lines) {
        val numberedMatch = numberedRegex.find(line)
        val bulletMatch = bulletRegex.find(line)

        when {
            numberedMatch != null -> {
                flushPlain()
                flushBullet()
                val num = numberedMatch.groupValues[1].toIntOrNull() ?: 1
                val content = numberedMatch.groupValues[2]
                currentNumberedItems.add(NumberedItem(num, content))
            }
            bulletMatch != null -> {
                flushPlain()
                flushNumbered()
                currentBulletItems.add(bulletMatch.groupValues[1])
            }
            else -> {
                flushNumbered()
                flushBullet()
                currentPlainLines.add(line)
            }
        }
    }

    flushPlain()
    flushNumbered()
    flushBullet()

    return segments
}

// ── Numbered step cards ──────────────────────────────────────────────────────

@Composable
private fun NumberedStepCards(items: List<NumberedItem>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            val stepShape = RoundedCornerShape(CunnyDimens.radiusMd)

            // 3D tactile step card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 3.dp)
                    .background(CunnyColors.tactileShadow, shape = stepShape)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-3).dp)
                        .clip(stepShape)
                        .background(CunnyColors.glassBg)
                        .border(1.dp, CunnyColors.borderLight, stepShape)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Number circle badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(CunnyColors.gradPlum)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${item.number}",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textOnPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Step content text
                    val codeColor = CunnyColors.primary
                    val codeBgColor = CunnyColors.primaryPale
                    val annotated = remember(item.content, codeColor, codeBgColor) {
                        parseInlineMarkdown(item.content, codeColor, codeBgColor)
                    }
                    Text(
                        text = annotated,
                        fontFamily = DmSansFontFamily,
                        fontSize = 14.sp,
                        color = CunnyColors.textDark,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageBlockRenderer(block: ImageBlock) {
    if (block.imageUrl == "custom://supervised-vs-unsupervised") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SupervisedVsUnsupervisedVisual()
            if (block.caption != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = block.caption,
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GlassSurface(
            shape = RoundedCornerShape(CunnyDimens.radiusMd),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.Camera,
                contentDescription = "Image placeholder",
                tint = CunnyColors.textSubtle,
                modifier = Modifier.align(Alignment.Center).size(48.dp)
            )
        }
        if (block.caption != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = block.caption,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SupervisedVsUnsupervisedVisual() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left Column (Supervised)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(CunnyColors.tactileShadow, shape = RoundedCornerShape(CunnyDimens.radiusMd))
        ) {
            GlassSurface(
                shape = RoundedCornerShape(CunnyDimens.radiusMd),
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-3).dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.ChalkboardTeacher,
                            contentDescription = "Supervised Learning",
                            tint = CunnyColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Supervised",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CunnyColors.textDark
                        )
                    }
                    
                    // Coordinate grid
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                color = CunnyColors.backgroundWarm.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = CunnyColors.borderLight.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        val gridLinesColor = CunnyColors.borderLight
                        val boundaryColor = CunnyColors.primary
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            
                            // Draw grid lines (3x3 grid)
                            val gridLines = 4
                            for (i in 1 until gridLines) {
                                val fraction = i.toFloat() / gridLines
                                // Horizontal
                                drawLine(
                                    color = gridLinesColor.copy(alpha = 0.4f),
                                    start = Offset(0f, height * fraction),
                                    end = Offset(width, height * fraction),
                                    strokeWidth = 1.dp.toPx()
                                )
                                // Vertical
                                drawLine(
                                    color = gridLinesColor.copy(alpha = 0.4f),
                                    start = Offset(width * fraction, 0f),
                                    end = Offset(width * fraction, height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            // Decision Boundary line
                            drawLine(
                                color = boundaryColor,
                                start = Offset(0f, 0f),
                                end = Offset(width, height),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        }
                        
                        // Rotated label for boundary
                        Text(
                            text = "Garis AI Model",
                            fontFamily = SoraFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = CunnyColors.primary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .rotate(45f)
                                .background(CunnyColors.glassBg, shape = RoundedCornerShape(4.dp))
                                .border(0.5.dp, CunnyColors.glassBorder, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                        
                        // Apples (Top-Right, red)
                        GlowingCircle(
                            color = CunnyColors.accentRed,
                            modifier = Modifier.align(BiasAlignment(0.5f, -0.6f))
                        )
                        GlowingCircle(
                            color = CunnyColors.accentRed,
                            modifier = Modifier.align(BiasAlignment(0.8f, -0.3f))
                        )
                        GlowingCircle(
                            color = CunnyColors.accentRed,
                            modifier = Modifier.align(BiasAlignment(0.3f, -0.8f))
                        )
                        
                        // Oranges (Bottom-Left, orange)
                        GlowingCircle(
                            color = CunnyColors.accentOrange,
                            modifier = Modifier.align(BiasAlignment(-0.5f, 0.6f))
                        )
                        GlowingCircle(
                            color = CunnyColors.accentOrange,
                            modifier = Modifier.align(BiasAlignment(-0.8f, 0.3f))
                        )
                        GlowingCircle(
                            color = CunnyColors.accentOrange,
                            modifier = Modifier.align(BiasAlignment(-0.3f, 0.8f))
                        )
                        
                        // Tags
                        PlotTag(
                            text = "Apel",
                            color = CunnyColors.accentRed,
                            modifier = Modifier.align(BiasAlignment(0.4f, -0.1f))
                        )
                        PlotTag(
                            text = "Jeruk",
                            color = CunnyColors.accentOrange,
                            modifier = Modifier.align(BiasAlignment(-0.4f, 0.1f))
                        )
                    }
                }
            }
        }

        // Right Column (Unsupervised)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(CunnyColors.tactileShadow, shape = RoundedCornerShape(CunnyDimens.radiusMd))
        ) {
            GlassSurface(
                shape = RoundedCornerShape(CunnyDimens.radiusMd),
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = (-3).dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Regular.CirclesThree,
                            contentDescription = "Unsupervised Learning",
                            tint = CunnyColors.accentGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Unsupervised",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = CunnyColors.textDark
                        )
                    }
                    
                    // Coordinate grid
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                color = CunnyColors.backgroundWarm.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = CunnyColors.borderLight.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        val gridLinesColor = CunnyColors.borderLight
                        val klasterAColor = CunnyColors.primary
                        val klasterBColor = CunnyColors.accentGreen
                        
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val width = size.width
                            val height = size.height
                            
                            // Draw grid lines (3x3 grid)
                            val gridLines = 4
                            for (i in 1 until gridLines) {
                                val fraction = i.toFloat() / gridLines
                                // Horizontal
                                drawLine(
                                    color = gridLinesColor.copy(alpha = 0.4f),
                                    start = Offset(0f, height * fraction),
                                    end = Offset(width, height * fraction),
                                    strokeWidth = 1.dp.toPx()
                                )
                                // Vertical
                                drawLine(
                                    color = gridLinesColor.copy(alpha = 0.4f),
                                    start = Offset(width * fraction, 0f),
                                    end = Offset(width * fraction, height),
                                    strokeWidth = 1.dp.toPx()
                                )
                            }
                            
                            // Faint circular backdrops
                            // Cluster A: top-right
                            drawCircle(
                                color = klasterAColor.copy(alpha = 0.08f),
                                center = Offset(width * 0.72f, height * 0.28f),
                                radius = 30.dp.toPx()
                            )
                            drawCircle(
                                color = klasterAColor.copy(alpha = 0.25f),
                                center = Offset(width * 0.72f, height * 0.28f),
                                radius = 30.dp.toPx(),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            )
                            
                            // Cluster B: bottom-left
                            drawCircle(
                                color = klasterBColor.copy(alpha = 0.08f),
                                center = Offset(width * 0.28f, height * 0.72f),
                                radius = 30.dp.toPx()
                            )
                            drawCircle(
                                color = klasterBColor.copy(alpha = 0.25f),
                                center = Offset(width * 0.28f, height * 0.72f),
                                radius = 30.dp.toPx(),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            )
                        }
                        
                        // Cluster A points (Neutral/primary cluster color)
                        GlowingCircle(
                            color = klasterAColor,
                            modifier = Modifier.align(BiasAlignment(0.5f, -0.6f))
                        )
                        GlowingCircle(
                            color = klasterAColor,
                            modifier = Modifier.align(BiasAlignment(0.8f, -0.3f))
                        )
                        GlowingCircle(
                            color = klasterAColor,
                            modifier = Modifier.align(BiasAlignment(0.3f, -0.8f))
                        )
                        
                        // Cluster B points (Neutral/accentGreen cluster color)
                        GlowingCircle(
                            color = klasterBColor,
                            modifier = Modifier.align(BiasAlignment(-0.5f, 0.6f))
                        )
                        GlowingCircle(
                            color = klasterBColor,
                            modifier = Modifier.align(BiasAlignment(-0.8f, 0.3f))
                        )
                        GlowingCircle(
                            color = klasterBColor,
                            modifier = Modifier.align(BiasAlignment(-0.3f, 0.8f))
                        )
                        
                        // Tags
                        PlotTag(
                            text = "Klaster A",
                            color = klasterAColor,
                            modifier = Modifier.align(BiasAlignment(0.4f, -0.1f))
                        )
                        PlotTag(
                            text = "Klaster B",
                            color = klasterBColor,
                            modifier = Modifier.align(BiasAlignment(-0.4f, 0.1f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlowingCircle(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(18.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing aura
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.2f), shape = CircleShape)
        )
        // Inner core
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, shape = CircleShape)
        )
    }
}

@Composable
private fun PlotTag(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(CunnyColors.glassBg, shape = RoundedCornerShape(6.dp))
            .border(1.dp, CunnyColors.glassBorder, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored dot indicator
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = CunnyColors.textDark
        )
    }
}

@Composable
fun CalloutBlockRenderer(block: CalloutBlock) {
    val isTip = block.style == "tip" || block.style == "fun_fact"
    val isWarning = block.style == "warning"

    val shape = RoundedCornerShape(CunnyDimens.radiusMd)
    val bg = if (isWarning) CunnyColors.accentRed.copy(alpha = 0.08f) else CunnyColors.glassBg
    val borderCol = if (isWarning) CunnyColors.accentRed.copy(alpha = 0.24f) else CunnyColors.glassBorder
    val barColor = when {
        isTip -> CunnyColors.primary
        isWarning -> CunnyColors.accentRed
        else -> CunnyColors.primary
    }

    val shadowColor = CunnyColors.tactileShadow

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(bottom = 4.dp)
            .background(
                color = shadowColor,
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .clip(shape)
        ) {
            // Blur background layer
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .then(
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            Modifier.graphicsLayer {
                                renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                    30f,
                                    30f,
                                    android.graphics.Shader.TileMode.CLAMP
                                ).asComposeRenderEffect()
                            }
                        } else {
                            Modifier
                        }
                    )
                    .background(bg)
                    .border(
                        width = 1.5.dp,
                        color = borderCol,
                        shape = shape
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Hilangkan bar vertikal tebal 4dp jika tipe tip
                if (!isTip) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(barColor)
                    )
                }
                
                Column(
                    modifier = Modifier.padding(vertical = 16.dp, horizontal = 20.dp)
                ) {
                    Text(
                        text = when {
                            isTip -> "Konsep Kunci"
                            isWarning -> "Perhatian"
                            else -> "Info"
                        },
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = barColor,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    val cleanMarkdown = remember(block.markdown) { block.markdown.stripEmojis() }
                    Text(
                        text = parseMarkdownToAnnotatedString(cleanMarkdown, CunnyColors.textDark, CunnyColors.primary, CunnyColors.primaryPale),
                        fontFamily = DmSansFontFamily,
                        fontSize = 14.sp,
                        color = CunnyColors.textBody,
                        lineHeight = 24.sp
                    )
                }
            }
        }
    }
}

@Composable
fun QuizBlockRenderer(
    block: QuizBlock,
    onQuizCorrect: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedChoiceIndex by remember { mutableStateOf<Int?>(null) }
    var quizPassed by remember { mutableStateOf(false) }
    val shakeOffset = remember { Animatable(0f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = block.question,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = CunnyColors.textDark,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        block.choices.forEachIndexed { index, choice ->
            val isSelected = selectedChoiceIndex == index
            val isCorrect = index == block.answerIndex

            val borderColor = when {
                isSelected && isCorrect -> CunnyColors.textOnPrimary.copy(alpha = 0.24f)
                isSelected && !isCorrect -> CunnyColors.textOnPrimary.copy(alpha = 0.24f)
                else -> CunnyColors.borderLight
            }

            val baseShadowColor = when {
                isSelected && isCorrect -> CunnyColors.primaryShadow
                isSelected && !isCorrect -> CunnyColors.accentRed.copy(alpha = 0.7f)
                else -> CunnyColors.tactileShadow // Normal base shadow
            }

            val bgBrush = when {
                isSelected && isCorrect -> SolidColor(CunnyColors.primary) // Solid Plum background
                isSelected && !isCorrect -> SolidColor(CunnyColors.accentRed)
                else -> SolidColor(CunnyColors.glassBg)
            }

            val shakeModifier = if (isSelected && !isCorrect) {
                Modifier.offset(x = shakeOffset.value.dp)
            } else Modifier

            val itemInteractionSource = remember { MutableInteractionSource() }
            val itemIsPressed by itemInteractionSource.collectIsPressedAsState()
            val itemOffsetY by animateDpAsState(
                targetValue = if (isSelected || (itemIsPressed && !quizPassed)) 0.dp else (-4).dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "choiceOffset"
            )

            val shape = RoundedCornerShape(CunnyDimens.radiusMd)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .padding(bottom = 4.dp)
                    .then(shakeModifier)
                    .background(
                        color = baseShadowColor,
                        shape = shape
                    )
                    .clickable(
                        interactionSource = itemInteractionSource,
                        indication = null,
                        enabled = !quizPassed,
                        onClick = {
                            selectedChoiceIndex = index
                            if (isCorrect) {
                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.CORRECT)
                                quizPassed = true
                                onQuizCorrect(true)
                            } else {
                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.INCORRECT)
                                scope.launch {
                                    shakeOffset.snapTo(0f)
                                    shakeOffset.animateTo(6f, tween(80))
                                    shakeOffset.animateTo(-6f, tween(80))
                                    shakeOffset.animateTo(4f, tween(80))
                                    shakeOffset.animateTo(-4f, tween(80))
                                    shakeOffset.animateTo(0f, tween(80))
                                }
                            }
                        }
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = itemOffsetY)
                        .clip(shape)
                        .background(bgBrush)
                        .border(1.5.dp, borderColor, shape)
                        .padding(16.dp)
                ) {
                    Text(
                        text = choice,
                        fontFamily = DmSansFontFamily,
                        fontSize = 15.sp,
                        color = if (isSelected) CunnyColors.textOnPrimary else CunnyColors.textDark,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // Hint box shown if user answered incorrectly
        AnimatedVisibility(visible = selectedChoiceIndex != null && selectedChoiceIndex != block.answerIndex) {
            val shape = RoundedCornerShape(CunnyDimens.radiusMd)
            val shadowColor = CunnyColors.tactileShadow
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .background(
                        color = shadowColor,
                        shape = shape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                        .clip(shape)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(shape)
                            .then(
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    Modifier.graphicsLayer {
                                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                            30f,
                                            30f,
                                            android.graphics.Shader.TileMode.CLAMP
                                        ).asComposeRenderEffect()
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .background(CunnyColors.glassBg)
                            .border(1.5.dp, CunnyColors.glassBorder, shape)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(CunnyColors.accentOrange)
                        )
                        
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Lightbulb,
                                contentDescription = "Petunjuk",
                                tint = CunnyColors.accentYellow,
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Column {
                                Text(
                                    text = "Petunjuk",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CunnyColors.accentOrange,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = parseMarkdownToAnnotatedString(block.hint ?: "", CunnyColors.textDark, CunnyColors.primary, CunnyColors.primaryPale),
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textBody,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Explanation box shown if correct answer is selected
        AnimatedVisibility(visible = quizPassed) {
            val shape = RoundedCornerShape(CunnyDimens.radiusMd)
            val shadowColor = CunnyColors.tactileShadow // Solid 3D warm plum base shadow
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
                    .background(
                        color = shadowColor,
                        shape = shape
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                        .clip(shape)
                ) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(shape)
                            .then(
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                    Modifier.graphicsLayer {
                                        renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                            30f,
                                            30f,
                                            android.graphics.Shader.TileMode.CLAMP
                                        ).asComposeRenderEffect()
                                    }
                                } else {
                                    Modifier
                                }
                            )
                            .background(CunnyColors.glassBg)
                            .border(1.5.dp, CunnyColors.glassBorder, shape)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .fillMaxHeight()
                                .background(CunnyColors.primary)
                        )
                        
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Check,
                                contentDescription = "Benar",
                                tint = CunnyColors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            
                            Column {
                                Text(
                                    text = "Penjelasan",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CunnyColors.primary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = parseMarkdownToAnnotatedString(block.explanation ?: "", CunnyColors.textDark, CunnyColors.primary, CunnyColors.primaryPale),
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textBody,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WidgetBlockRenderer(
    block: WidgetBlock,
    lang: String = "id",
    onOpenWidget: (String) -> Unit,
    onWidgetCompleted: (Boolean) -> Unit
) {
    when (block.widgetType) {
        "sorting_game" -> SortingGameWidget(lang = lang, onWidgetCompleted = onWidgetCompleted)
        "train_your_own_ai" -> TrainAiWidget(block, onWidgetCompleted)
        "prompt_evaluator" -> PromptEvaluatorWidget(block, onWidgetCompleted)
        "bias_game" -> BiasGameWidget(block, onWidgetCompleted)
        "neuron_sandbox" -> NeuronSandboxWidget(block, onWidgetCompleted)
        "next_word_predictor" -> NextWordPredictorWidget(block, onWidgetCompleted)
        "recommendation_engine" -> RecommendationEngineWidget(block, onWidgetCompleted)
        "rule_vs_learning" -> RuleVsLearningWidget(block, onWidgetCompleted)
        "sensory_sandbox" -> SensorySandboxWidget(block, onWidgetCompleted)
        "reward_trainer" -> RewardTrainerWidget(block, onWidgetCompleted)
        "diffusion_sandbox" -> DiffusionSandboxWidget(block, onWidgetCompleted)
        "privacy_auditor" -> PrivacyAuditorWidget(block, onWidgetCompleted)
        "spot_the_fake" -> ClaimDetectiveWidget(block, onWidgetCompleted)
        "claim_detective" -> ClaimDetectiveWidget(block, onWidgetCompleted)
        "pixel_zoom" -> PixelZoomWidget(block, onWidgetCompleted)
        "taxonomy_concentric_circles" -> TaxonomyConcentricCirclesWidget(block, onWidgetCompleted)
        "grocery_sorter" -> GrocerySorterWidget(block, onWidgetCompleted)
        "data_cleaner" -> DataCleanerWidget(block, onWidgetCompleted)
        else -> ScannerTeaserWidget(block, onOpenWidget, onWidgetCompleted)
    }
}
