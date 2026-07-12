package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

data class PredictorOption(
    val word: String,
    val probability: Int,
    val isCorrect: Boolean
)

data class PredictorStep(
    val currentText: String,
    val options: List<PredictorOption>
)

@Composable
fun NextWordPredictorWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val stepsRaw = block.config?.get("steps") as? List<Map<String, Any>>
    var currentStepIdx by remember { mutableIntStateOf(0) }
    var selectedOptionIdx by remember { mutableStateOf<Int?>(null) }
    var showIncorrectText by remember { mutableStateOf(false) }
    var completedSentence by remember { mutableStateOf("") }
    var isDone by remember { mutableStateOf(false) }

    val steps = remember(stepsRaw) {
        stepsRaw?.map { stepMap ->
            val curText = stepMap["current_text"] as? String ?: ""
            val optionsRaw = stepMap["options"] as? List<Map<String, Any>>
            val options = optionsRaw?.map { optMap ->
                val word = optMap["word"] as? String ?: ""
                val prob = (optMap["probability"] as? Number)?.toInt() ?: 0
                val correct = optMap["is_correct"] as? Boolean ?: false
                PredictorOption(word, prob, correct)
            } ?: emptyList()
            PredictorStep(curText, options)
        } ?: emptyList()
    }

    val currentStep = steps.getOrNull(currentStepIdx)

    LaunchedEffect(isDone) {
        if (isDone) onWidgetCompleted(true)
    }

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
                Text("📝", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Prediksi Kata Berikutnya",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bantu AI menyelesaikan kalimat dengan memilih kata berdasarkan probabilitas tertinggi.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

            if (!isDone && currentStep != null) {
                // Display current sentence box
                val sentenceShape = RoundedCornerShape(CunnyDimens.radiusMd)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 19.dp) // 16.dp bottom margin + 3.dp shadow space
                        .background(
                            color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                            shape = sentenceShape
                        )
                ) {
                    GlassSurface(
                        shape = sentenceShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-3).dp)
                    ) {
                        Text(
                            text = currentStep.currentText + "...",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CunnyColors.textDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                if (showIncorrectText) {
                    Text(
                        text = "Kata tersebut memiliki probabilitas terlalu rendah atau salah konteks. Coba kata lain!",
                        color = CunnyColors.accentRed,
                        fontFamily = DmSansFontFamily,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Display options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentStep.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOptionIdx == idx
                        val buttonColor = when {
                            isSelected && opt.isCorrect -> CunnyColors.primary
                            isSelected && !opt.isCorrect -> Color(0xFFA84860) // Solid Berry Red
                            else -> CunnyColors.glassBg
                        }
                        val borderColor = when {
                            isSelected && opt.isCorrect -> Color.White.copy(alpha = 0.24f)
                            isSelected && !opt.isCorrect -> Color.White.copy(alpha = 0.24f)
                            else -> CunnyColors.borderLight
                        }
                        val textColor = when {
                            isSelected && opt.isCorrect -> Color.White
                            isSelected && !opt.isCorrect -> Color.White
                            else -> CunnyColors.textDark
                        }

                        val optShape = RoundedCornerShape(CunnyDimens.radiusMd)
                        val optBaseColor = when {
                            isSelected && opt.isCorrect -> CunnyColors.primaryShadow // Solid warm plum shadow
                            isSelected && !opt.isCorrect -> Color(0xFF8C354C) // Solid berry shadow
                            else -> CunnyColors.tactileShadow // Solid warm plum shadow
                        }

                        val optInteractionSource = remember { MutableInteractionSource() }
                        val optIsPressed by optInteractionSource.collectIsPressedAsState()
                        val optOffsetY by animateDpAsState(
                            targetValue = if (optIsPressed) 0.dp else (-3).dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "optOffset"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                                .background(optBaseColor, shape = optShape)
                                .clickable(
                                    interactionSource = optInteractionSource,
                                    indication = null,
                                    onClick = {
                                        selectedOptionIdx = idx
                                        if (opt.isCorrect) {
                                            showIncorrectText = false
                                        } else {
                                            showIncorrectText = true
                                        }
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = optOffsetY)
                                    .clip(optShape)
                                    .background(buttonColor)
                                    .border(1.dp, borderColor, optShape)
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opt.word,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    text = "${opt.probability}% Probabilitas",
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 12.sp,
                                    color = if (isSelected) textColor else CunnyColors.textSubtle
                                )
                            }
                        }
                    }
                }

                // Continue button to proceed to next step
                selectedOptionIdx?.let { idx ->
                    val opt = currentStep.options[idx]
                    if (opt.isCorrect) {
                        Spacer(modifier = Modifier.height(16.dp))
                        CunnyPrimaryButton(
                            text = "Lanjutkan",
                            onClick = {
                                val nextText = currentStep.currentText + " " + opt.word
                                selectedOptionIdx = null
                                if (currentStepIdx == steps.lastIndex) {
                                    completedSentence = nextText
                                    isDone = true
                                } else {
                                    currentStepIdx += 1
                                }
                            }
                        )
                    }
                }
            } else {
                // Completed State
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val successShape = RoundedCornerShape(CunnyDimens.radiusMd)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(successShape)
                            .background(CunnyColors.primaryPale)
                            .border(
                                width = 1.dp,
                                color = CunnyColors.primary.copy(alpha = 0.24f),
                                shape = successShape
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Sukses Menyusun!",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = CunnyColors.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = completedSentence,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = CunnyColors.textDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Generative AI memprediksi kalimat di atas kata demi kata berdasarkan probabilitas kecocokan tertinggi.",
                        fontFamily = DmSansFontFamily,
                        fontSize = 12.sp,
                        color = CunnyColors.textSubtle,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
}
