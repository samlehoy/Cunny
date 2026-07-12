package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.compose.screens.lesson.parseMarkdownToAnnotatedString
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TrainAiWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    var datasetSize by remember { mutableStateOf(500f) }
    var learningRate by remember { mutableStateOf(0.05f) }
    
    var isTraining by remember { mutableStateOf(false) }
    var trainingProgress by remember { mutableStateOf(0f) }
    var trainingAccuracy by remember { mutableStateOf(0.50f) }
    var trainingCompleted by remember { mutableStateOf(false) }

    var explanationTitle by remember { mutableStateOf("") }
    var explanationBody by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val outerShape = RoundedCornerShape(CunnyDimens.radiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .padding(bottom = 4.dp)
            .background(
                color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
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
            Text(
                text = "🤖",
                fontSize = 40.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Latih AI Kamu Sendiri",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = CunnyColors.textDark
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Sesuaikan parameter dan simulasikan proses latihan untuk melihat peningkatan akurasi AI.",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(18.dp))

            if (!trainingCompleted) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Ukuran Dataset",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CunnyColors.textBody
                            )
                            Text(
                                text = "${datasetSize.toInt()} contoh",
                                fontFamily = DmSansFontFamily,
                                fontSize = 13.sp,
                                color = CunnyColors.textSubtle
                            )
                        }
                        Slider(
                            value = datasetSize,
                            onValueChange = { datasetSize = it },
                            valueRange = 100f..1000f,
                            steps = 8,
                            enabled = !isTraining
                        )
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Kecepatan Belajar (Learning Rate)",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CunnyColors.textBody
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.2f", learningRate),
                                fontFamily = DmSansFontFamily,
                                fontSize = 13.sp,
                                color = CunnyColors.textSubtle
                            )
                        }
                        Slider(
                            value = learningRate,
                            onValueChange = { learningRate = it },
                            valueRange = 0.01f..0.10f,
                            steps = 8,
                            enabled = !isTraining
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isTraining) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(
                            progress = trainingProgress,
                            color = CunnyColors.primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Akurasi: ${(trainingAccuracy * 100).toInt()}%",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                    }
                } else {
                    CunnyPrimaryButton(
                        text = "Latih Model",
                        onClick = {
                            isTraining = true
                            trainingProgress = 0f
                            trainingAccuracy = 0.50f
                            
                            scope.launch {
                                val delayTime = (80 + (datasetSize / 10).toLong())
                                
                                // Deterministic target accuracy calculation
                                val targetAccuracy = if (learningRate >= 0.08f) {
                                    0.52f + (datasetSize / 1000f) * 0.08f
                                } else if (learningRate <= 0.02f) {
                                    0.68f + (datasetSize / 1000f) * 0.10f
                                } else {
                                    if (datasetSize < 400f) {
                                        0.70f + (datasetSize / 400f) * 0.08f
                                    } else if (datasetSize < 700f) {
                                        0.80f + ((datasetSize - 400f) / 300f) * 0.08f
                                    } else {
                                        0.90f + ((datasetSize - 700f) / 300f) * 0.08f
                                    }
                                }
                                // Set text explanation values
                                val accPercent = String.format(java.util.Locale.US, "%.1f%%", targetAccuracy * 100)
                                if (learningRate >= 0.08f) {
                                    explanationTitle = "Latihan Kurang Optimal (Overshooting)"
                                    explanationBody = "Akurasi rendah ($accPercent) karena **Kecepatan Belajar (Learning Rate) terlalu tinggi**! AI mencoba belajar terburu-buru dan melompati pola-pola penting. Ini membuat AI bingung dan gagal mengenali pola secara stabil."
                                } else if (learningRate <= 0.02f) {
                                    explanationTitle = "Latihan Terlalu Lambat (Undershooting)"
                                    explanationBody = "Akurasi sedang ($accPercent). **Kecepatan Belajar (Learning Rate) terlalu lambat** membuat AI melangkah sangat kecil dan hati-hati. AI butuh waktu jauh lebih lama (lebih banyak epoch/latihan) untuk bisa pintar!"
                                } else {
                                    if (datasetSize < 400f) {
                                        explanationTitle = "Ukuran Data Kurang Memadai (Dataset Terbatas)"
                                        explanationBody = "Akurasi sedang ($accPercent) karena **Ukuran Dataset terlalu kecil**. Kecepatan belajar sudah pas, tetapi AI kekurangan contoh untuk dikenali. Semakin sedikit contoh, semakin sulit AI menebak dengan benar!"
                                    } else if (datasetSize < 700f) {
                                        explanationTitle = "Latihan Cukup Baik (Hasil Memuaskan)"
                                        explanationBody = "Akurasi bagus ($accPercent)! Dengan ukuran dataset sedang dan kecepatan belajar yang pas, AI berhasil mengenali sebagian besar pola dengan baik."
                                    } else {
                                        explanationTitle = "Latihan Optimal (Hasil Sempurna)"
                                        explanationBody = "Akurasi sangat tinggi ($accPercent)! Kombinasi **dataset besar** (banyak contoh belajar) dan **kecepatan belajar yang pas** membuat AI memahami pola data secara optimal dan menjadi sangat cerdas!"
                                    }
                                }

                                for (epoch in 1..10) {
                                    delay(delayTime)
                                    trainingProgress = epoch / 10f
                                    trainingAccuracy = 0.50f + (targetAccuracy - 0.50f) * (epoch / 10f)
                                }
                                isTraining = false
                                trainingCompleted = true
                                onWidgetCompleted(true)
                            }
                        }
                    )
                }
            } else {
                val successShape = RoundedCornerShape(CunnyDimens.radiusMd)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp)
                        .background(
                            color = CunnyColors.primaryShadow, // Warm Plum shadow
                            shape = successShape
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-3).dp)
                            .clip(successShape)
                            .background(CunnyColors.primaryPale) // Warm Plum background
                            .border(
                                width = 1.dp,
                                color = CunnyColors.primary.copy(alpha = 0.24f),
                                shape = successShape
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Latihan Selesai",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CunnyColors.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(java.util.Locale.US, "Akurasi Akhir: %.1f%%", trainingAccuracy * 100),
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CunnyColors.textDark
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Pedagogical Explanation Box
                        val explanationShape = RoundedCornerShape(CunnyDimens.radiusSm)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 2.dp)
                                .background(
                                    color = CunnyColors.tactileShadow, // Solid warm plum shadow
                                    shape = explanationShape
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-2).dp)
                                    .clip(explanationShape)
                                    .background(CunnyColors.background)
                                    .border(
                                        width = 1.dp,
                                        color = CunnyColors.borderLight,
                                        shape = explanationShape
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = explanationTitle,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CunnyColors.textDark
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parseMarkdownToAnnotatedString(explanationBody, CunnyColors.textDark), // Parse markdown dynamic text!
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 12.sp,
                                    color = CunnyColors.textBody,
                                    lineHeight = 18.sp
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
