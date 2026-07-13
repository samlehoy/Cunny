package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import com.adamglin.phosphoricons.Fill
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.fill.Rabbit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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

@Composable
fun SpotTheFakeWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    var selectedIdx by remember { mutableStateOf<Int?>(null) }
    val completed = selectedIdx == 1 // index 1 is fake (id=2 in JSON items list)

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
            Icon(
                imageVector = PhosphorIcons.Fill.Rabbit,
                contentDescription = null,
                tint = CunnyColors.primary,
                modifier = Modifier.size(40.dp).padding(bottom = 8.dp)
            )
            Text(
                text = "Spot The Fake (Deepfake)",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = CunnyColors.textDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ketuk gambar wajah hasil rekayasa AI (Deepfake) di bawah ini.",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                // Item 1 (Real)
                val faceAShape = RoundedCornerShape(CunnyDimens.radiusMd)
                val faceABaseColor = if (selectedIdx == 0) Color(0xFFC98E8E) else CunnyColors.tactileShadow
                val faceABg = if (selectedIdx == 0) CunnyColors.accentRed.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.3f)
                val faceAInteractionSource = remember { MutableInteractionSource() }
                val faceAIsPressed by faceAInteractionSource.collectIsPressedAsState()
                val faceAOffsetY by animateDpAsState(
                    targetValue = if (faceAIsPressed) 0.dp else (-3).dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "faceAOffset"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp)
                        .background(faceABaseColor, shape = faceAShape)
                        .clickable(
                            interactionSource = faceAInteractionSource,
                            indication = null,
                            onClick = { selectedIdx = 0 }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = faceAOffsetY)
                            .clip(faceAShape)
                            .background(faceABg)
                            .border(1.dp, CunnyColors.borderLight, faceAShape)
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Gambar Wajah A", fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Detail pencahayaan tampak sangat alami.", fontFamily = DmSansFontFamily, fontSize = 11.sp, color = CunnyColors.textSubtle)
                    }
                }

                // Item 2 (Fake)
                val faceBShape = RoundedCornerShape(CunnyDimens.radiusMd)
                val faceBBaseColor = if (selectedIdx == 1) CunnyColors.primaryShadow else CunnyColors.tactileShadow
                val faceBBg = if (selectedIdx == 1) CunnyColors.primaryPale else Color.White.copy(alpha = 0.3f)
                val faceBInteractionSource = remember { MutableInteractionSource() }
                val faceBIsPressed by faceBInteractionSource.collectIsPressedAsState()
                val faceBOffsetY by animateDpAsState(
                    targetValue = if (faceBIsPressed) 0.dp else (-3).dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "faceBOffset"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 3.dp)
                        .background(faceBBaseColor, shape = faceBShape)
                        .clickable(
                            interactionSource = faceBInteractionSource,
                            indication = null,
                            onClick = { selectedIdx = 1 }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = faceBOffsetY)
                            .clip(faceBShape)
                            .background(faceBBg)
                            .border(
                                width = 1.dp,
                                color = if (selectedIdx == 1) CunnyColors.primary.copy(alpha = 0.24f) else CunnyColors.borderLight,
                                shape = faceBShape
                            )
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Gambar Wajah B (Deepfake)", fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = if (completed) "Keanehan: Kacamata menyatu dengan pipi dan latar belakang melengkung janggal." else "Pilihlah gambar ini jika mencurigakan.",
                            fontFamily = DmSansFontFamily,
                            fontSize = 11.sp,
                            color = if (completed) CunnyColors.primary else CunnyColors.textSubtle,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
}
