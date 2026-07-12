package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.*
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BiasItem(
    val id: Int,
    val emoji: String,
    val label: String,
    val bg: String,
    val isBiased: Boolean
)

@Composable
fun BiasGameWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val targetClass = remember(block.config) {
        (block.config?.get("target_class") as? String) ?: "Apel"
    }

    val items = remember(block.config) {
        @Suppress("UNCHECKED_CAST")
        val rawItems = (block.config?.get("items") as? List<*>)
            ?.filterIsInstance<Map<String, Any>>() ?: emptyList()
            
        rawItems.map { map ->
            val id = (map["id"] as? Number)?.toInt() ?: 0
            val label = (map["label"] as? String) ?: ""
            val bg = (map["background"] as? String) ?: ""
            val isBiased = (map["is_biased"] as? Boolean) ?: false
            
            val emoji = when {
                label.contains("Apel", ignoreCase = true) || label.contains("Apple", ignoreCase = true) -> {
                    if (bg.contains("Hijau", ignoreCase = true) || bg.contains("Green", ignoreCase = true)) "🍏" else "🍎"
                }
                label.contains("Nanas", ignoreCase = true) || label.contains("Pineapple", ignoreCase = true) -> "🍍"
                label.contains("Tomat", ignoreCase = true) || label.contains("Tomato", ignoreCase = true) -> "🍅"
                else -> "❓"
            }
            BiasItem(id, emoji, label, bg, isBiased)
        }.ifEmpty {
            listOf(
                BiasItem(1, "🍎", "Apel", "Merah", true),
                BiasItem(2, "🍎", "Apel", "Merah", true),
                BiasItem(3, "🍍", "Nanas", "Putih", false),
                BiasItem(4, "🍏", "Apel", "Hijau", false),
                BiasItem(5, "🍅", "Tomat", "Hijau", false)
            )
        }
    }

    val selectedIds = remember { mutableStateListOf<Int>() }
    var isTraining by remember { mutableStateOf(false) }
    var trainingCompleted by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testResultAccuracy by remember { mutableFloatStateOf(0f) }
    var testSuccess by remember { mutableStateOf(false) }

    val context = LocalContext.current
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
                Text("⚖️", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Game Bias",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Latihlah AI untuk mengenali $targetClass dengan memilih gambar latih.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (!trainingCompleted) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items.forEach { item ->
                            val isSelected = selectedIds.contains(item.id)
                            val rowShape = RoundedCornerShape(CunnyDimens.radiusMd)
                            val rowBaseColor = if (isSelected) Color(0xFF8B7FF2) else CunnyColors.tactileShadow
                            val rowBg = if (isSelected) CunnyColors.primaryPale else Color.White.copy(alpha = 0.7f)
                            val rowBorderColor = if (isSelected) CunnyColors.primary else CunnyColors.borderLight

                            val rowInteractionSource = remember { MutableInteractionSource() }
                            val rowIsPressed by rowInteractionSource.collectIsPressedAsState()
                            val rowOffsetY by animateDpAsState(
                                targetValue = if (rowIsPressed) 0.dp else (-3).dp,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                label = "biasRowOffset"
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 3.dp)
                                    .background(rowBaseColor, shape = rowShape)
                                    .clickable(
                                        interactionSource = rowInteractionSource,
                                        indication = null,
                                        enabled = !isTraining,
                                        onClick = {
                                            if (isSelected) selectedIds.remove(item.id)
                                            else selectedIds.add(item.id)
                                        }
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .offset(y = rowOffsetY)
                                        .clip(rowShape)
                                        .background(rowBg)
                                        .border(
                                            width = 1.dp,
                                            color = rowBorderColor,
                                            shape = rowShape
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = item.emoji, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.label,
                                            fontFamily = SoraFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = CunnyColors.textDark
                                        )
                                        Text(
                                            text = "Latar Belakang: ${item.bg}",
                                            fontFamily = DmSansFontFamily,
                                            fontSize = 12.sp,
                                            color = CunnyColors.textSubtle
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CunnyPrimaryButton(
                        text = "Latih Model AI",
                        onClick = {
                            if (selectedIds.isEmpty()) {
                                CunnyToast.show("Pilih minimal satu gambar untuk melatih!", CunnyToastType.INFO)
                                return@CunnyPrimaryButton
                            }
                            isTraining = true
                                scope.launch {
                                    delay(1200)
                                    isTraining = false
                                    trainingCompleted = true
                                    
                                    val trainedItems = items.filter { selectedIds.contains(it.id) }
                                    val hasRedApples = trainedItems.any { it.isBiased }
                                    val hasGreenApple = trainedItems.any { (it.label.contains("Apel", ignoreCase = true) || it.label.contains("Apple", ignoreCase = true)) && !it.isBiased }
                                    val hasWrongItems = trainedItems.any { !it.label.contains("Apel", ignoreCase = true) && !it.label.contains("Apple", ignoreCase = true) }
                                    
                                    if (hasRedApples && !hasGreenApple && !hasWrongItems) {
                                        testResultText = "Oh tidak! AI hanya dilatih menggunakan apel merah, sehingga ia menebak Apel Hijau sebagai Tomat (keyakinan 0% Apel) karena bias warna latar belakang!"
                                        testResultAccuracy = 0.40f
                                        testSuccess = false
                                    } else if (hasRedApples && hasGreenApple && !hasWrongItems) {
                                        testResultText = "Luar biasa! Melatih dengan apel merah dan hijau berhasil menghilangkan bias warna latar belakang. AI berhasil mengenali apel hijau sebagai Apel!"
                                        testResultAccuracy = 0.98f
                                        testSuccess = true
                                        onWidgetCompleted(true)
                                    } else {
                                        testResultText = "Latihan gagal. Dataset kamu tidak seimbang atau mengandung gambar buah lain yang salah."
                                        testResultAccuracy = 0.20f
                                        testSuccess = false
                                    }
                                }
                            },
                            isLoading = isTraining
                        )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Kasus Uji: Apel Hijau 🍏",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CunnyColors.textDark
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val resultShape = RoundedCornerShape(CunnyDimens.radiusMd)
                        val resultBaseColor = if (testSuccess) CunnyColors.primaryShadow else Color(0xFFC98E8E)
                        val resultBg = if (testSuccess) CunnyColors.primaryPale else Color(0xFFFFF5F5)
                        val resultBorderColor = if (testSuccess) CunnyColors.primary else CunnyColors.accentRed

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                                .background(resultBaseColor, shape = resultShape)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-3).dp)
                                    .clip(resultShape)
                                    .background(resultBg)
                                    .border(
                                        width = 1.dp,
                                        color = resultBorderColor,
                                        shape = resultShape
                                    )
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (testSuccess) "Sukses!" else "Gagal",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (testSuccess) CunnyColors.primary else CunnyColors.accentRed
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = testResultText ?: "",
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 13.sp,
                                    color = CunnyColors.textBody,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (!testSuccess) {
                            CunnyOutlineButton(
                                text = "Reset & Latih Kembali",
                                onClick = {
                                    selectedIds.clear()
                                    trainingCompleted = false
                                    testResultText = null
                                }
                            )
                        } else {
                            Text(
                                text = "Akurasi: ${(testResultAccuracy * 100).toInt()}%",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = CunnyColors.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
