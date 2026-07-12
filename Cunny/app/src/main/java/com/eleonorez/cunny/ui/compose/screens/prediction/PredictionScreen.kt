package com.eleonorez.cunny.ui.compose.screens.prediction

import android.net.Uri
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.eleonorez.cunny.ml.LabelConfidence
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.StaticAmbientBackground
import com.eleonorez.cunny.ui.compose.components.CunnyProgressTrack
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun PredictionScreen(
    imageUri: String?,
    predictedLabel: String?,
    confidence: Double,
    rationale: String?,
    topKJson: String?,
    onBack: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Deserialize topK list using Gson
    val topKList = remember(topKJson) {
        try {
            if (!topKJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<LabelConfidence>>() {}.type
                Gson().fromJson<List<LabelConfidence>>(topKJson, type)
            } else emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }



    StaticAmbientBackground(isCourseDetail = false, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
                .verticalScroll(rememberScrollState())
        ) {
        // Custom Header Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CunnyBackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Hasil",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CunnyColors.textDark
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            // Preview Image matching HTML .pred-img
            if (!imageUri.isNullOrEmpty()) {
                val parsedUri = remember(imageUri) { Uri.parse(imageUri) }
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                    },
                    update = { view ->
                        view.setImageURI(parsedUri)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(CunnyDimens.radiusSurface))
                        .border(
                            width = 1.dp,
                            color = CunnyColors.border,
                            shape = RoundedCornerShape(CunnyDimens.radiusSurface)
                        )
                )
            }

            Spacer(Modifier.height(20.dp))

            // Prediction Result Solid Green Card matching CSS success boxes
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CunnyDimens.radiusSurface))
                    .background(Brush.horizontalGradient(CunnyColors.gradProgress))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Prediction: ${predictedLabel?.uppercase() ?: "UNKNOWN"}",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(confidence * 100).toInt()}% confidence",
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CunnyProgressTrack(
                        progress = confidence.toFloat()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Top-K predictions list card
            if (topKList.isNotEmpty()) {
                GlassSurface(
                    shape = RoundedCornerShape(CunnyDimens.radiusLg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Top Predictions",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CunnyColors.textDark
                        )

                        topKList.forEach { prediction ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = prediction.label.uppercase(),
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textDark
                                )
                                Text(
                                    text = "${(prediction.confidence * 100).toInt()}%",
                                    fontFamily = DmSansFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textBody
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rationale Callout Card
            if (!rationale.isNullOrEmpty()) {
                GlassSurface(
                    shape = RoundedCornerShape(CunnyDimens.radiusLg),
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "💡 Rationale",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CunnyColors.accentOrange,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = rationale,
                                fontFamily = DmSansFontFamily,
                                fontSize = 14.sp,
                                color = CunnyColors.textBody,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            CunnyPrimaryButton(
                text = "Done",
                onClick = onDone
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun PredictionPreview() {
    CunnyTheme {
        PredictionScreen(
            imageUri = null,
            predictedLabel = "apple",
            confidence = 0.82,
            rationale = "Gambar terlihat bulat dan merah — mirip pola apel dari data latihan.",
            topKJson = null,
            onBack = {},
            onDone = {}
        )
    }
}
