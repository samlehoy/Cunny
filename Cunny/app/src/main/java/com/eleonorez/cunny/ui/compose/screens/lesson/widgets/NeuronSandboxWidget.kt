package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import com.adamglin.phosphoricons.Regular

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.regular.Brain
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

data class ParsedTruthTableRow(
    val fruit: String,
    val red: Int,
    val spots: Int,
    val expected: Int
)

data class RowEvaluation(
    val name: String,
    val red: Int,
    val spots: Int,
    val sum: Int,
    val pred: Int,
    val expected: Int,
    val isCorrect: Boolean
)

@Composable
fun NeuronSandboxWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val targetClass = (block.config?.get("target_class") as? String) ?: "Stroberi"
    val minWeight = (block.config?.get("min_weight") as? Number)?.toFloat() ?: -5f
    val maxWeight = (block.config?.get("max_weight") as? Number)?.toFloat() ?: 5f
    val minThreshold = (block.config?.get("min_threshold") as? Number)?.toFloat() ?: 0f
    val maxThreshold = (block.config?.get("max_threshold") as? Number)?.toFloat() ?: 10f

    var weightRed by remember { mutableFloatStateOf(1f) }
    var weightSpots by remember { mutableFloatStateOf(1f) }
    var threshold by remember { mutableFloatStateOf(3f) }

    // Active test case index
    var selectedIndex by remember { mutableIntStateOf(3) }

    // Parse truth table dynamically from config
    val truthTableRaw = block.config?.get("truth_table") as? List<Map<String, Any>>
    val truthTable = remember(truthTableRaw) {
        truthTableRaw?.map { map ->
            val fruitName = map["fruit"] as? String ?: ""
            val expectedVal = (map["expected"] as? Number)?.toInt() ?: 0
            val inputsMap = map["inputs"] as? Map<String, Any>
            val redVal = (inputsMap?.get("red") as? Number)?.toInt() ?: 0
            val spotsVal = (inputsMap?.get("spots") as? Number)?.toInt() ?: 0
            ParsedTruthTableRow(fruitName, redVal, spotsVal, expectedVal)
        } ?: listOf(
            ParsedTruthTableRow("Pisang", 0, 0, 0),
            ParsedTruthTableRow("Apel", 1, 0, 0),
            ParsedTruthTableRow("Kiwi", 0, 1, 0),
            ParsedTruthTableRow("Stroberi", 1, 1, 1)
        )
    }

    val evaluated = remember(truthTable, weightRed, weightSpots, threshold) {
        truthTable.map { row ->
            val sum = row.red * weightRed.toInt() + row.spots * weightSpots.toInt()
            val pred = if (sum >= threshold.toInt()) 1 else 0
            val isCorrect = pred == row.expected
            RowEvaluation(row.fruit, row.red, row.spots, sum, pred, row.expected, isCorrect)
        }
    }

    val completed = remember(evaluated) { evaluated.all { it.isCorrect } }

    LaunchedEffect(completed) {
        if (completed) onWidgetCompleted(true)
    }

    val currentCase = evaluated.getOrNull(selectedIndex) ?: evaluated.first()
    val isActivated = currentCase.pred == 1

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
                Icon(
                    imageVector = PhosphorIcons.Regular.Brain,
                    contentDescription = null,
                    tint = CunnyColors.primary,
                    modifier = Modifier.size(40.dp).padding(bottom = 8.dp)
                )
                Text(
                    text = "Lab Neuron Interaktif (LTU)",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sesuaikan Bobot (Weights) & Ambang Batas (Threshold) agar neuron hanya aktif (Output = 1) untuk $targetClass.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                // ==========================================
                // DIAGRAM NEURON (LTU)
                // ==========================================
                Text(
                    text = "Simulasi Neuron (Aktif: ${currentCase.name}):",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CunnyColors.textDark,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

                val diagramShape = RoundedCornerShape(CunnyDimens.radiusMd)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp)
                        .background(
                            color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                            shape = diagramShape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .offset(y = (-3).dp)
                            .clip(diagramShape)
                            .background(Color.White.copy(alpha = 0.4f))
                            .border(1.dp, CunnyColors.borderLight, diagramShape)
                            .padding(12.dp)
                    ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Inputs Column
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        // Input Red
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (currentCase.red == 1) Color(0xFFE57373) else Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentCase.red == 1) "M" else "P",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("x1 (Merah)", fontSize = 10.sp, fontFamily = DmSansFontFamily, color = CunnyColors.textSubtle)
                                Text("Val: ${currentCase.red}", fontSize = 12.sp, fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Input Spots
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (currentCase.spots == 1) Color(0xFFB89962) else Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentCase.spots == 1) "B" else "P",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("x2 (Bintik)", fontSize = 10.sp, fontFamily = DmSansFontFamily, color = CunnyColors.textSubtle)
                                Text("Val: ${currentCase.spots}", fontSize = 12.sp, fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Weight Lines Visual & Weights Display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Text(
                            text = "w1 = ${weightRed.toInt()}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CunnyColors.primary
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "w2 = ${weightSpots.toInt()}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CunnyColors.primary
                        )
                    }

                    // Central Summation Node
                    val nodeBorderColor = if (isActivated) CunnyColors.primary else CunnyColors.border
                    val nodeBg = if (isActivated) CunnyColors.primaryPale else CunnyColors.backgroundSoft
                    
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(nodeBg)
                            .border(2.dp, nodeBorderColor, CircleShape)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Σ = ${currentCase.sum}",
                                fontSize = 11.sp,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = CunnyColors.textDark
                            )
                            Text(
                                text = "θ = ${threshold.toInt()}",
                                fontSize = 10.sp,
                                fontFamily = DmSansFontFamily,
                                color = CunnyColors.textSubtle
                            )
                        }
                    }

                    // Output Connection
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (isActivated) CunnyColors.primary else Color.LightGray),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${currentCase.pred}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text("Output", fontSize = 9.sp, color = CunnyColors.textSubtle)
                    }
                }
            }
        }

            // Formula Scaffolding
            Spacer(modifier = Modifier.height(8.dp))
            val formulaText = "Rumus: (${currentCase.red} × ${weightRed.toInt()}) + (${currentCase.spots} × ${weightSpots.toInt()}) = ${currentCase.sum} " +
                              (if (isActivated) "≥" else "<") + " ${threshold.toInt()} (Threshold)"
            Text(
                text = formulaText,
                fontFamily = DmSansFontFamily,
                fontSize = 11.sp,
                color = if (isActivated) CunnyColors.primary else CunnyColors.textSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // SLIDERS FOR BOBOT & THRESHOLD
            // ==========================================
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Weight Red (w_merah)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bobot Merah (w_merah)",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CunnyColors.textBody
                        )
                        Text(
                            text = "${weightRed.toInt()}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 13.sp,
                            color = CunnyColors.textSubtle
                        )
                    }
                    Slider(
                        value = weightRed,
                        onValueChange = { weightRed = it },
                        valueRange = minWeight..maxWeight,
                        steps = (maxWeight - minWeight).toInt() - 1
                    )
                }

                // Weight Spots (w_bintik)
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Bobot Bintik (w_bintik)",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CunnyColors.textBody
                        )
                        Text(
                            text = "${weightSpots.toInt()}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 13.sp,
                            color = CunnyColors.textSubtle
                        )
                    }
                    Slider(
                        value = weightSpots,
                        onValueChange = { weightSpots = it },
                        valueRange = minWeight..maxWeight,
                        steps = (maxWeight - minWeight).toInt() - 1
                    )
                }

                // Threshold
                Column {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Ambang Batas (Threshold)",
                            fontFamily = DmSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = CunnyColors.textBody
                        )
                        Text(
                            text = "${threshold.toInt()}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 13.sp,
                            color = CunnyColors.textSubtle
                        )
                    }
                    Slider(
                        value = threshold,
                        onValueChange = { threshold = it },
                        valueRange = minThreshold..maxThreshold,
                        steps = (maxThreshold - minThreshold).toInt() - 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // TRUTH TABLE
            // ==========================================
            Text(
                text = "Tabel Evaluasi (Ketuk untuk Uji):",
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = CunnyColors.textBody,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                evaluated.forEachIndexed { idx, row ->
                    val isSelected = idx == selectedIndex

                    val rowShape = RoundedCornerShape(CunnyDimens.radiusSm)
                    val rowBaseColor = when {
                        isSelected -> CunnyColors.primaryLight
                        row.isCorrect -> CunnyColors.tactileShadow
                        else -> Color(0xFFE6D5DB)
                    }
                    val rowBg = when {
                        isSelected -> CunnyColors.primaryPale
                        row.isCorrect -> Color(0xFFFDFBFE)
                        else -> Color(0xFFFFF6F6)
                    }
                    val rowBorderColor = when {
                        isSelected -> CunnyColors.primary
                        else -> CunnyColors.borderLight
                    }

                    val rowInteractionSource = remember { MutableInteractionSource() }
                    val rowIsPressed by rowInteractionSource.collectIsPressedAsState()
                    val rowOffsetY by animateDpAsState(
                        targetValue = if (rowIsPressed) 0.dp else (-3).dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "rowOffset"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 3.dp)
                            .background(rowBaseColor, shape = rowShape)
                            .clickable(
                                interactionSource = rowInteractionSource,
                                indication = null,
                                onClick = { selectedIndex = idx }
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = rowOffsetY)
                                .clip(rowShape)
                                .background(rowBg)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = rowBorderColor,
                                    shape = rowShape
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = row.name,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CunnyColors.textDark
                                )

                                // Visual properties chip
                                Row(
                                    modifier = Modifier.padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Red Input Chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (row.red == 1) Color(0xFFE57373) else Color.LightGray)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (row.red == 1) "Merah" else "Polos",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Spots Input Chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (row.spots == 1) Color(0xFFB89962) else Color.LightGray)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (row.spots == 1) "Bintik" else "Polos",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Out: ${row.pred}",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (row.isCorrect) CunnyColors.primary else CunnyColors.accentRed
                                )
                                Text(
                                    text = "Target: ${row.expected}",
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 12.sp,
                                    color = CunnyColors.textSubtle
                                )
                                Text(
                                    text = if (row.isCorrect) "✅" else "❌",
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            if (completed) {
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CunnyDimens.radiusMd))
                        .background(CunnyColors.primaryPale)
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Neuron Teralinyasi!",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = CunnyColors.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Sempurna! Bobot dan threshold yang Anda pilih berhasil membedakan Stroberi dari buah lainnya.",
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
}
