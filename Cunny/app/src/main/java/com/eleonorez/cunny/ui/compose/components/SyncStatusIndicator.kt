package com.eleonorez.cunny.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.eleonorez.cunny.data.sync.SyncManager
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

/**
 * Small composable indicator that shows ⚠️ + badge count
 * when there are pending/failed sync items in the WorkManager queue.
 */
@Composable
fun SyncStatusIndicator(
    pendingSyncCount: Int,
    modifier: Modifier = Modifier
) {
    var showPopup by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = pendingSyncCount > 0,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box {
            // Warning badge (3D elevated claymorphic glass style matching StreakEnergyBar)
            val shape = RoundedCornerShape(CunnyDimens.radiusFull)
            val shadowColor = CunnyColors.tactileShadow // Theme-aware 3D shadow base

            Box(
                modifier = Modifier
                    .clickable { showPopup = !showPopup }
                    .padding(bottom = 3.dp)
                    .background(
                        color = shadowColor,
                        shape = shape
                    )
            ) {
                GlassSurface(
                    shape = shape,
                    modifier = Modifier
                        .offset(y = (-3).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⚠️", fontSize = 14.sp)
                        Text(
                            text = "$pendingSyncCount",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                    }
                }
            }

            // Popup explanation
            if (showPopup) {
                Popup(
                    alignment = Alignment.TopCenter,
                    onDismissRequest = { showPopup = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 36.dp)
                            .width(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFBF9FD).copy(alpha = 0.96f),
                                        Color(0xFFEDE9FF).copy(alpha = 0.96f)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = CunnyColors.border,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "⚠️ Sinkronisasi Tertunda",
                                fontFamily = SoraFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = CunnyColors.textDark
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "$pendingSyncCount item belum tersinkronisasi. Data akan otomatis dikirim saat koneksi tersedia.",
                                fontFamily = DmSansFontFamily,
                                fontSize = 12.sp,
                                color = CunnyColors.textSubtle,
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Tutup",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CunnyColors.primary,
                                modifier = Modifier
                                    .clickable { showPopup = false }
                                    .align(Alignment.End)
                            )
                        }
                    }
                }
            }
        }
    }
}
