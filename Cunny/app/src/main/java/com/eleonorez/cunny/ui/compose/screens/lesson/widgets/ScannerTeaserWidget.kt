package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun ScannerTeaserWidget(
    block: WidgetBlock,
    onOpenWidget: (String) -> Unit,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val widgetLabel = (block.config?.get("label") as? String) ?: "Image Classifier"
    val widgetDesc = (block.config?.get("desc") as? String) ?: "Tap to scan a fruit with your camera or upload from gallery."

    val primaryColor = CunnyColors.primary
    val density = LocalDensity.current
    val stroke = remember(density) {
        Stroke(
            width = with(density) { 2.dp.toPx() },
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(with(density) { 12.dp.toPx() }, with(density) { 12.dp.toPx() }),
                0f
            )
        )
    }

    val teaserShape = RoundedCornerShape(CunnyDimens.radiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 4.dp)
            .background(
                color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                shape = teaserShape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .drawBehind {
                    drawRoundRect(
                        color = primaryColor.copy(alpha = 0.34f),
                        style = stroke,
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
                .clip(teaserShape)
                .background(CunnyColors.backgroundWarm)
                .clickable {
                    onWidgetCompleted(true)
                    onOpenWidget(block.widgetType)
                }
                .padding(24.dp)
        ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "📷",
                fontSize = 40.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = widgetLabel,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = widgetDesc,
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))
            CunnyOutlineButton(
                text = "Launch Scanner",
                onClick = {
                    onWidgetCompleted(true)
                    onOpenWidget(block.widgetType)
                }
            )
        }
    }
}
}
