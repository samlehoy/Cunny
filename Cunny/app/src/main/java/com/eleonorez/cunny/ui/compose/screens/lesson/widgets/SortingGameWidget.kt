package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import com.adamglin.phosphoricons.Regular
import androidx.compose.foundation.layout.size

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Sliders
import com.adamglin.phosphoricons.regular.Image
import com.adamglin.phosphoricons.regular.EnvelopeSimple
import com.adamglin.phosphoricons.regular.Database
import com.adamglin.phosphoricons.regular.Cube
import androidx.compose.ui.draw.shadow
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import com.eleonorez.cunny.helper.SoundSynthesizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun SortingGameWidget(
    lang: String = "id",
    onWidgetCompleted: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val items = remember(lang) {
        if (lang == "en") {
            listOf(
                SortItem("Photo of Apple (Labeled: 'Apple')", true),
                SortItem("Unlabeled Bank Transactions", false),
                SortItem("Email (Labeled: 'Spam' / 'Inbox')", true),
                SortItem("Collection of Unlabeled Animal Photos", false),
                SortItem("House Features (Price Labeled)", true),
                SortItem("Unlabeled Customer Purchase History", false)
            )
        } else {
            listOf(
                SortItem("Foto Apel (Label: 'Apel')", true),
                SortItem("Kumpulan Transaksi Bank Tanpa Nama", false),
                SortItem("Email (Label: 'Spam' / 'Inbox')", true),
                SortItem("Koleksi Foto Hewan Tanpa Nama", false),
                SortItem("Ciri Rumah (Label Harga Terlampir)", true),
                SortItem("Riwayat Belanja Pelanggan Tanpa Nama", false)
            )
        }
    }

    var currentIndex by remember { mutableStateOf(0) }
    var gameCompleted by remember { mutableStateOf(false) }
    var showIncorrectText by remember { mutableStateOf(false) }
    var selectedButtonIndex by remember { mutableStateOf<Int?>(null) }

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
            .border(
                width = 1.5.dp,
                color = CunnyColors.border,
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
                    imageVector = PhosphorIcons.Regular.Sliders,
                    contentDescription = null,
                    tint = CunnyColors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (lang == "en") "Sorting Game" else "Game Pengelompokan",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (lang == "en") {
                        "Sort data into Supervised (Guided) vs Unsupervised (Self-directed) learning."
                    } else {
                        "Kelompokkan data ke dalam pembelajaran Supervised (Terbimbing) vs Unsupervised (Mandiri)."
                    },
                    fontFamily = DmSansFontFamily,
                    fontSize = 13.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (!gameCompleted && currentItem != null) {
                    val contentIcon = remember(currentItem.name) {
                        val nameLower = currentItem.name.lowercase()
                        when {
                            nameLower.contains("foto") || nameLower.contains("photo") || nameLower.contains("hewan") || nameLower.contains("animal") -> PhosphorIcons.Regular.Image
                            nameLower.contains("email") || nameLower.contains("inbox") || nameLower.contains("spam") -> PhosphorIcons.Regular.EnvelopeSimple
                            nameLower.contains("transaksi") || nameLower.contains("riwayat") || nameLower.contains("ciri") || nameLower.contains("features") || nameLower.contains("history") || nameLower.contains("purchase") -> PhosphorIcons.Regular.Database
                            else -> PhosphorIcons.Regular.Cube
                        }
                    }

                    val activeShape = RoundedCornerShape(CunnyDimens.radiusMd)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 19.dp)
                            .background(
                                color = CunnyColors.tactileShadow,
                                shape = activeShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-3).dp)
                                .background(
                                    color = CunnyColors.backgroundSoft,
                                    shape = activeShape
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = CunnyColors.border,
                                    shape = activeShape
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Icon(
                                    imageVector = contentIcon,
                                    contentDescription = null,
                                    tint = CunnyColors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = currentItem.name,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textDark,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (showIncorrectText) {
                        Text(
                            text = if (lang == "en") "Oops, try again!" else "Oops, coba lagi!",
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
                        // Supervised Button (index 0)
                        val interactionSourceSup = remember { MutableInteractionSource() }
                        val isPressedSup by interactionSourceSup.collectIsPressedAsState()
                        val offsetYSup by animateDpAsState(
                            targetValue = if (isPressedSup) 0.dp else (-4).dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "supervisedOffset"
                        )

                        val isIncorrectSup = showIncorrectText && selectedButtonIndex == 0
                        val buttonBgSup = if (isIncorrectSup) CunnyColors.accentRed.copy(alpha = 0.24f) else CunnyColors.glassBg
                        val borderColSup = if (isIncorrectSup) CunnyColors.accentRed else CunnyColors.glassBorder

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CunnyColors.tactileShadow, shape = RoundedCornerShape(CunnyDimens.radiusFull))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = offsetYSup)
                                    .clip(RoundedCornerShape(CunnyDimens.radiusFull))
                                    .background(buttonBgSup)
                                    .border(1.5.dp, borderColSup, RoundedCornerShape(CunnyDimens.radiusFull))
                                    .clickable(
                                        interactionSource = interactionSourceSup,
                                        indication = null,
                                        onClick = {
                                            selectedButtonIndex = 0
                                            if (currentItem.isSupervised) {
                                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.CORRECT)
                                                showIncorrectText = false
                                                if (currentIndex == items.lastIndex) {
                                                    gameCompleted = true
                                                    onWidgetCompleted(true)
                                                } else {
                                                    currentIndex += 1
                                                    selectedButtonIndex = null
                                                }
                                            } else {
                                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.INCORRECT)
                                                showIncorrectText = true
                                            }
                                        }
                                    )
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "Supervised",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Unsupervised Button (index 1)
                        val interactionSourceUnsup = remember { MutableInteractionSource() }
                        val isPressedUnsup by interactionSourceUnsup.collectIsPressedAsState()
                        val offsetYUnsup by animateDpAsState(
                            targetValue = if (isPressedUnsup) 0.dp else (-4).dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "unsupervisedOffset"
                        )

                        val isIncorrectUnsup = showIncorrectText && selectedButtonIndex == 1
                        val buttonBgUnsup = if (isIncorrectUnsup) CunnyColors.accentRed.copy(alpha = 0.24f) else CunnyColors.glassBg
                        val borderColUnsup = if (isIncorrectUnsup) CunnyColors.accentRed else CunnyColors.glassBorder

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(CunnyColors.tactileShadow, shape = RoundedCornerShape(CunnyDimens.radiusFull))
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = offsetYUnsup)
                                    .clip(RoundedCornerShape(CunnyDimens.radiusFull))
                                    .background(buttonBgUnsup)
                                    .border(1.5.dp, borderColUnsup, RoundedCornerShape(CunnyDimens.radiusFull))
                                    .clickable(
                                        interactionSource = interactionSourceUnsup,
                                        indication = null,
                                        onClick = {
                                            selectedButtonIndex = 1
                                            if (!currentItem.isSupervised) {
                                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.CORRECT)
                                                showIncorrectText = false
                                                if (currentIndex == items.lastIndex) {
                                                    gameCompleted = true
                                                    onWidgetCompleted(true)
                                                } else {
                                                    currentIndex += 1
                                                    selectedButtonIndex = null
                                                }
                                            } else {
                                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.INCORRECT)
                                                showIncorrectText = true
                                            }
                                        }
                                    )
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "Unsupervised",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textDark,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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
                            text = if (lang == "en") "Correct!" else "Benar!",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = CunnyColors.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "en") {
                                "Supervised learning uses labeled data, while unsupervised learning uses unlabeled data."
                            } else {
                                "Pembelajaran terbimbing (supervised) menggunakan data berlabel, sedangkan pembelajaran mandiri (unsupervised) menggunakan data tanpa label."
                            },
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
