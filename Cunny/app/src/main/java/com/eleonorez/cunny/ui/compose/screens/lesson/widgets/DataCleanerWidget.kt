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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
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
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun DataCleanerWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val rowsRaw = block.config?.get("rows") as? List<Map<String, Any>>
    val rows = remember(rowsRaw) {
        rowsRaw?.map { rMap ->
            val id = rMap["id"] as? Number ?: 0
            val prod = rMap["product"] as? String ?: ""
            val price = rMap["price"] as? String ?: ""
            val status = rMap["status"] as? String ?: ""
            val issue = rMap["issue"] as? String ?: ""
            CleanRow(id.toInt(), prod, price, status, issue)
        } ?: emptyList()
    }

    val rowFlags = remember { mutableStateMapOf<Int, String>() }
    var checked by remember { mutableStateOf(false) }
    var showIncorrectText by remember { mutableStateOf(false) }

    val isActuallyCorrect = rows.all { r ->
        val flag = rowFlags[r.id]
        if (r.status == "clean") {
            flag == "clean"
        } else {
            flag == "removed" || flag == "fixed"
        }
    }

    LaunchedEffect(checked, isActuallyCorrect) {
        if (checked && isActuallyCorrect) {
            onWidgetCompleted(true)
        }
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
                Text("\uD83E\uDDF9", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Data Cleaner (GIGO)",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bersihkan data kotor (duplikat, missing value, outlier) di tabel bawah ini.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rows.forEach { row ->
                    val flag = rowFlags[row.id]
                    val isCheckedCorrect = checked && (if (row.status == "clean") flag == "clean" else flag == "removed" || flag == "fixed")

                    val cardBg = when {
                        checked && isCheckedCorrect -> CunnyColors.primaryPale
                        checked && !isCheckedCorrect -> CunnyColors.accentRed.copy(alpha = 0.12f)
                        else -> Color.White.copy(alpha = 0.3f)
                    }

                    val rowShape = RoundedCornerShape(CunnyDimens.radiusMd)
                    val rowBaseColor = when {
                        checked && isCheckedCorrect -> CunnyColors.primaryShadow // Solid warm plum shadow for correct
                        checked && !isCheckedCorrect -> Color(0xFFC98E8E) // Solid red shadow for incorrect
                        else -> CunnyColors.tactileShadow // Solid warm plum shadow
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 3.dp)
                            .background(
                                color = rowBaseColor,
                                shape = rowShape
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-3).dp)
                                .clip(rowShape)
                                .background(cardBg)
                                .border(
                                    width = 1.dp,
                                    color = if (checked && isCheckedCorrect) CunnyColors.primary.copy(alpha = 0.24f) else CunnyColors.borderLight,
                                    shape = rowShape
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${row.product} - ${if (row.price.isEmpty()) "[KOSONG]" else row.price}",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CunnyColors.textDark
                                )
                                if (checked && !isCheckedCorrect) {
                                    Text(text = "Kotor!", fontSize = 11.sp, color = CunnyColors.accentRed, fontWeight = FontWeight.Bold)
                                }
                            }

                            if (checked && !isCheckedCorrect && row.issue.isNotEmpty()) {
                                Text(
                                    text = "Masalah: " + row.issue,
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 11.sp,
                                    color = CunnyColors.accentRed,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val keepActive = flag == "clean"
                                val fixActive = flag == "fixed" || flag == "removed"

                                val keepInteractionSource = remember { MutableInteractionSource() }
                                val keepIsPressed by keepInteractionSource.collectIsPressedAsState()
                                val keepOffsetY by animateDpAsState(
                                    targetValue = if (keepIsPressed && (!checked || !isCheckedCorrect)) 0.dp else (-2).dp,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "keepOffset"
                                )
                                val keepBaseColor = if (keepActive) Color(0xFF6C5CE7) else CunnyColors.tactileShadow
                                val keepBg = if (keepActive) CunnyColors.primary else Color.Transparent
                                val keepBorderColor = if (keepActive) Color.Transparent else CunnyColors.border

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 2.dp)
                                        .background(keepBaseColor, RoundedCornerShape(CunnyDimens.radiusSm))
                                        .clickable(
                                            interactionSource = keepInteractionSource,
                                            indication = null,
                                            enabled = !checked || !isCheckedCorrect,
                                            onClick = {
                                                rowFlags[row.id] = "clean"
                                                showIncorrectText = false
                                            }
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = keepOffsetY)
                                            .clip(RoundedCornerShape(CunnyDimens.radiusSm))
                                            .background(keepBg)
                                            .border(1.dp, keepBorderColor, RoundedCornerShape(CunnyDimens.radiusSm))
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Simpan (Bersih)",
                                            fontSize = 11.sp,
                                            color = if (keepActive) Color.White else CunnyColors.textDark,
                                            fontWeight = if (keepActive) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }

                                val fixInteractionSource = remember { MutableInteractionSource() }
                                val fixIsPressed by fixInteractionSource.collectIsPressedAsState()
                                val fixOffsetY by animateDpAsState(
                                    targetValue = if (fixIsPressed && (!checked || !isCheckedCorrect)) 0.dp else (-2).dp,
                                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                                    label = "fixOffset"
                                )
                                val fixBaseColor = if (fixActive) Color(0xFFC78743) else CunnyColors.tactileShadow
                                val fixBg = if (fixActive) CunnyColors.accentOrange else Color.Transparent
                                val fixBorderColor = if (fixActive) Color.Transparent else CunnyColors.border

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 2.dp)
                                        .background(fixBaseColor, RoundedCornerShape(CunnyDimens.radiusSm))
                                        .clickable(
                                            interactionSource = fixInteractionSource,
                                            indication = null,
                                            enabled = !checked || !isCheckedCorrect,
                                            onClick = {
                                                rowFlags[row.id] = if (row.status == "duplicate") "removed" else "fixed"
                                                showIncorrectText = false
                                            }
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .offset(y = fixOffsetY)
                                            .clip(RoundedCornerShape(CunnyDimens.radiusSm))
                                            .background(fixBg)
                                            .border(1.dp, fixBorderColor, RoundedCornerShape(CunnyDimens.radiusSm))
                                            .padding(vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Hapus / Perbaiki",
                                            fontSize = 11.sp,
                                            color = if (fixActive) Color.White else CunnyColors.textDark,
                                            fontWeight = if (fixActive) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (showIncorrectText) {
                  Text(
                      text = "Beberapa baris kotor belum dibersihkan dengan benar!",
                      color = CunnyColors.accentRed,
                      fontFamily = DmSansFontFamily,
                      fontSize = 12.sp,
                      textAlign = TextAlign.Center,
                      modifier = Modifier.padding(bottom = 10.dp)
                  )
              }

              if (!checked) {
                  CunnyPrimaryButton(
                      text = "Periksa Data",
                      onClick = {
                          checked = true
                          if (!isActuallyCorrect) {
                              showIncorrectText = true
                          }
                      }
                  )
              } else if (!isActuallyCorrect) {
                  CunnyOutlineButton(
                      text = "Coba Lagi",
                      onClick = {
                          checked = false
                          showIncorrectText = false
                      }
                  )
              } else {
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
                          .padding(12.dp),
                      contentAlignment = Alignment.Center
                  ) {
                      Text(
                          text = "Sempurna! Data bersih, model AI siap dilatih.",
                          fontFamily = SoraFontFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          color = CunnyColors.primary
                      )
                  }
              }
        }
    }
}
}

  data class CleanRow(
      val id: Int,
      val product: String,
      val price: String,
      val status: String,
      val issue: String
  )
  
