package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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

data class SensoryVisionItem(
    val btn_label: String,
    val emoji: String,
    val detect_label: String,
    val confidence: Int,
    val box_x: Float,
    val box_y: Float,
    val box_w: Float,
    val box_h: Float
)

data class SensoryAudioItem(
    val btn_label: String,
    val emoji: String,
    val waveform: List<Float>,
    val transcription: String
)

data class SensorySandboxConfig(
    val vision_label: String = "Mata AI (Visi)",
    val audio_label: String = "Telinga AI (Audio)",
    val vision_items: List<SensoryVisionItem> = listOf(
        SensoryVisionItem("Deteksi Apel", "🍎", "Apel", 98, 0.2f, 0.2f, 0.6f, 0.6f)
    ),
    val audio_items: List<SensoryAudioItem> = listOf(
        SensoryAudioItem("Eja Apel", "🔊", listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.9f, 0.3f, 0.6f), "A-p-e-l")
    )
)

@Composable
fun SensorySandboxWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val config = remember(block.config) {
        val visionLabel = block.config?.get("vision_label") as? String ?: "Mata AI (Visi)"
        val audioLabel = block.config?.get("audio_label") as? String ?: "Telinga AI (Audio)"
        
        val visionRaw = block.config?.get("vision_items") as? List<Map<String, Any>>
        val visionItems = visionRaw?.map { item ->
            SensoryVisionItem(
                btn_label = item["btn_label"] as? String ?: "Deteksi Apel",
                emoji = item["emoji"] as? String ?: "🍎",
                detect_label = item["detect_label"] as? String ?: "Apel",
                confidence = (item["confidence"] as? Number)?.toInt() ?: 98,
                box_x = (item["box_x"] as? Number)?.toFloat() ?: 0.2f,
                box_y = (item["box_y"] as? Number)?.toFloat() ?: 0.2f,
                box_w = (item["box_w"] as? Number)?.toFloat() ?: 0.6f,
                box_h = (item["box_h"] as? Number)?.toFloat() ?: 0.6f
            )
        } ?: listOf(SensoryVisionItem("Deteksi Apel", "🍎", "Apel", 98, 0.2f, 0.2f, 0.6f, 0.6f))
        
        val audioRaw = block.config?.get("audio_items") as? List<Map<String, Any>>
        val audioItems = audioRaw?.map { item ->
            val waveformRaw = item["waveform"] as? List<Any>
            val waveform = waveformRaw?.map { (it as? Number)?.toFloat() ?: 0.5f } ?: listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.9f, 0.3f, 0.6f)
            SensoryAudioItem(
                btn_label = item["btn_label"] as? String ?: "Eja Apel",
                emoji = item["emoji"] as? String ?: "🔊",
                waveform = waveform,
                transcription = item["transcription"] as? String ?: "A-p-e-l"
            )
        } ?: listOf(SensoryAudioItem("Eja Apel", "🔊", listOf(0.2f, 0.5f, 0.8f, 0.4f, 0.9f, 0.3f, 0.6f), "A-p-e-l"))
        
        SensorySandboxConfig(
            vision_label = visionLabel,
            audio_label = audioLabel,
            vision_items = visionItems,
            audio_items = audioItems
        )
    }

    var visionActive by remember { mutableStateOf(false) }
    var audioActive by remember { mutableStateOf(false) }

    LaunchedEffect(visionActive, audioActive) {
        if (visionActive && audioActive) {
            onWidgetCompleted(true)
        }
    }

    GlassSurface(
        shape = RoundedCornerShape(CunnyDimens.radiusLg),
        modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sensory Lab: Simulasi Input Sensorik",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = CunnyColors.textDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ==================== COLUMN 1: VISION LAB ====================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF9F9FB), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.vision_label,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CunnyColors.textDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E24))
                            .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (visionActive) {
                            val activeItem = config.vision_items.firstOrNull()
                            if (activeItem != null) {
                                Text(
                                    text = activeItem.emoji,
                                    fontSize = 48.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 2.dp.toPx()
                                    val rectX = size.width * activeItem.box_x
                                    val rectY = size.height * activeItem.box_y
                                    val rectW = size.width * activeItem.box_w
                                    val rectH = size.height * activeItem.box_h

                                    drawRect(
                                        color = Color.Red,
                                        topLeft = Offset(rectX, rectY),
                                        size = Size(rectW, rectH),
                                        style = Stroke(width = strokeWidth)
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                        .background(Color(0xE6D97706), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${activeItem.detect_label} (${activeItem.confidence}%)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                    text = "📷 Kamera Off",
                                    color = Color(0xFF71717A),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeItem = config.vision_items.firstOrNull()
                    Button(
                        onClick = { visionActive = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (visionActive) Color(0xFF22C55E) else CunnyColors.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            text = activeItem?.btn_label ?: "Simulasikan Kamera",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // ==================== COLUMN 2: AUDIO LAB ====================
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF9F9FB), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = config.audio_label,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CunnyColors.textDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1E24))
                            .border(1.dp, Color(0xFF3F3F46), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (audioActive) {
                            val activeAudio = config.audio_items.firstOrNull()
                            if (activeAudio != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .weight(1f)
                                            .padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        activeAudio.waveform.forEachIndexed { i, valPct ->
                                            val infiniteTransition = rememberInfiniteTransition(label = "wave")
                                            val heightMultiplier by infiniteTransition.animateFloat(
                                                initialValue = 0.6f,
                                                targetValue = 1.2f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(400 + i * 80, easing = LinearEasing),
                                                    repeatMode = RepeatMode.Reverse
                                                ),
                                                label = "height"
                                            )

                                            val barHeight = (40.dp * valPct * heightMultiplier).coerceIn(4.dp, 50.dp)
                                            Box(
                                                modifier = Modifier
                                                    .width(4.dp)
                                                    .height(barHeight)
                                                    .background(Color(0xFF6C5CE7), shape = RoundedCornerShape(2.dp))
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${activeAudio.transcription} -> Hasil: Apel",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                    text = "🎤 Mikrofon Off",
                                    color = Color(0xFF71717A),
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center
                                )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeAudio = config.audio_items.firstOrNull()
                    Button(
                        onClick = { audioActive = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (audioActive) Color(0xFF22C55E) else CunnyColors.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text(
                            text = activeAudio?.btn_label ?: "Simulasikan Audio",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
