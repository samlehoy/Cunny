package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
private val TypeOptions = listOf(
    Pair("kontinu", "Kontinu (Desimal)"),
    Pair("diskrit", "Diskrit (Bulat)"),
    Pair("nominal", "Nominal (Nama)"),
    Pair("ordinal", "Ordinal (Urutan)")
)

@Composable
fun GrocerySorterWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    val itemsRaw = block.config?.get("items") as? List<Map<String, Any>>
    val items = remember(itemsRaw) {
        itemsRaw?.map { iMap ->
            val id = iMap["id"] as? Number ?: 0
            val name = iMap["name"] as? String ?: ""
            val type = iMap["type"] as? String ?: ""
            val emoji = iMap["emoji"] as? String ?: "\uD83C\uDF4E"
            val hint = iMap["hint"] as? String ?: ""
            GroceryItem(id.toInt(), name, type, emoji, hint)
        } ?: emptyList()
    }

    var currentIdx by remember { mutableIntStateOf(0) }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var showIncorrectText by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }

    val currentItem = items.getOrNull(currentIdx)

    LaunchedEffect(completed) {
        if (completed) {
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
                Text("\uD83D\uDED2", fontSize = 40.sp, modifier = Modifier.padding(bottom = 8.dp))
                Text(
                    text = "Grocery Sorter (Tipe Data)",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tentukan tipe data yang benar untuk karakteristik kelontong di bawah.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (!completed && currentItem != null) {
                    // Active Item Card
                    val activeShape = RoundedCornerShape(CunnyDimens.radiusMd)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 19.dp)
                            .background(
                                color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                                shape = activeShape
                            )
                    ) {
                        GlassSurface(
                            shape = activeShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-3).dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = currentItem.emoji, fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentItem.name,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = CunnyColors.textDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                if (showIncorrectText) {
                    Text(
                        text = "Petunjuk: " + currentItem.hint,
                        color = CunnyColors.accentRed,
                        fontFamily = DmSansFontFamily,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                // Grid of 4 options
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TypeOptions.forEach { (typeKey, typeLabel) ->
                        CunnyOutlineButton(
                            text = typeLabel,
                            onClick = {
                                if (currentItem.type == typeKey) {
                                    showIncorrectText = false
                                    if (currentIdx == items.lastIndex) {
                                        completed = true
                                    } else {
                                        currentIdx += 1
                                    }
                                } else {
                                    showIncorrectText = true
                                }
                            }
                        )
                    }
                }
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
                          .padding(16.dp),
                      contentAlignment = Alignment.Center
                  ) {
                      Text(
                          text = "Sukses! Kamu telah berhasil memilah semua tipe data dengan benar.",
                          fontFamily = SoraFontFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 13.sp,
                          color = CunnyColors.primary,
                          textAlign = TextAlign.Center
                      )
                  }
              }
        }
    }
}
}

data class GroceryItem(
    val id: Int,
    val name: String,
    val type: String,
    val emoji: String,
    val hint: String
)
