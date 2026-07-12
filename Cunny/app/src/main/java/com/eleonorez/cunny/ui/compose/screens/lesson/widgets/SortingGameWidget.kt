package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

data class SortItem(
    val name: String,
    val isSupervised: Boolean
)

@Composable
fun SortingGameWidget(onWidgetCompleted: (Boolean) -> Unit) {
    val items = remember {
        listOf(
            SortItem("Foto Apel (Label: 'Apel')", true),
            SortItem("Kumpulan Transaksi Bank Tanpa Nama", false),
            SortItem("Email (Label: 'Spam' / 'Bukan')", true),
            SortItem("Koleksi Foto Hewan Tanpa Nama", false)
        )
    }

    var currentIndex by remember { mutableStateOf(0) }
    var gameCompleted by remember { mutableStateOf(false) }
    var showIncorrectText by remember { mutableStateOf(false) }

    val currentItem = items.getOrNull(currentIndex)

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
                    text = "📊",
                    fontSize = 40.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Game Pengelompokan",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Kelompokkan data ke dalam pembelajaran Supervised (Terbimbing) vs Unsupervised (Mandiri).",
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (!gameCompleted && currentItem != null) {
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
                            Text(
                                text = currentItem.name,
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
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
                        text = "Oops, coba lagi!",
                        color = CunnyColors.accentRed,
                        fontFamily = DmSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CunnyOutlineButton(
                        text = "Supervised",
                        onClick = {
                            if (currentItem.isSupervised) {
                                showIncorrectText = false
                                if (currentIndex == items.lastIndex) {
                                    gameCompleted = true
                                    onWidgetCompleted(true)
                                } else {
                                    currentIndex += 1
                                }
                            } else {
                                showIncorrectText = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CunnyOutlineButton(
                        text = "Unsupervised",
                        onClick = {
                            if (!currentItem.isSupervised) {
                                showIncorrectText = false
                                if (currentIndex == items.lastIndex) {
                                    gameCompleted = true
                                    onWidgetCompleted(true)
                                } else {
                                    currentIndex += 1
                                }
                            } else {
                                showIncorrectText = true
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
              } else {
                  val successShape = RoundedCornerShape(CunnyDimens.radiusMd)
                  Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      modifier = Modifier
                          .fillMaxWidth()
                          .clip(successShape)
                          .background(CunnyColors.primaryPale)
                          .border(
                              width = 1.dp,
                              color = CunnyColors.primary.copy(alpha = 0.24f),
                              shape = successShape
                          )
                          .padding(16.dp)
                  ) {
                      Text(
                          text = "Benar!",
                          fontFamily = SoraFontFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 16.sp,
                          color = CunnyColors.primary
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Text(
                          text = "Pembelajaran terbimbing (supervised) menggunakan data berlabel, sedangkan pembelajaran mandiri (unsupervised) menggunakan data tanpa label.",
                          fontFamily = DmSansFontFamily,
                          fontSize = 13.sp,
                          color = CunnyColors.textBody,
                          textAlign = TextAlign.Center
                      )
                  }
              }
        }
    }
}
}
