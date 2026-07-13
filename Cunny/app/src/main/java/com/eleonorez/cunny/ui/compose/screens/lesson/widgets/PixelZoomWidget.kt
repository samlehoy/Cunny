package com.eleonorez.cunny.ui.compose.screens.lesson.widgets

import com.adamglin.phosphoricons.Fill

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
fun PixelZoomWidget(
    block: WidgetBlock,
    onWidgetCompleted: (Boolean) -> Unit
) {
    // User needs to click on the color coordinate samples: Red, Green, Blue
    var redChecked by remember { mutableStateOf(false) }
    var greenChecked by remember { mutableStateOf(false) }
    var blueChecked by remember { mutableStateOf(false) }
    val completed = redChecked && greenChecked && blueChecked

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
                text = "Pixel Zoom RGB",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = CunnyColors.textDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Periksa ketiga koordinat warna piksel (Red, Green, Blue) pada gambar apel.",
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                // Red Sample Pixel
                val redShape = RoundedCornerShape(CunnyDimens.radiusMd)
                val redBaseColor = if (redChecked) CunnyColors.primaryShadow else Color(0xFFC98E8E)
                val redBg = if (redChecked) CunnyColors.primaryPale else CunnyColors.accentRed.copy(alpha = 0.12f)
                val redInteractionSource = remember { MutableInteractionSource() }
                val redIsPressed by redInteractionSource.collectIsPressedAsState()
                val redOffsetY by animateDpAsState(
                    targetValue = if (redIsPressed) 0.dp else (-3).dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "redOffset"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 3.dp)
                        .background(redBaseColor, shape = redShape)
                        .clickable(
                            interactionSource = redInteractionSource,
                            indication = null,
                            onClick = { redChecked = true }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = redOffsetY)
                            .clip(redShape)
                            .background(redBg)
                            .border(
                                width = 1.dp,
                                color = if (redChecked) CunnyColors.primary.copy(alpha = 0.24f) else CunnyColors.borderLight,
                                shape = redShape
                            )
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(24.dp).background(Color.Red, CircleShape))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Red Pixel", fontSize = 11.sp, fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
                        Text("R: 255, G: 0, B: 0", fontSize = 9.sp, fontFamily = DmSansFontFamily, color = CunnyColors.textSubtle)
                    }
                }

                // Green Sample Pixel
                val greenShape = RoundedCornerShape(CunnyDimens.radiusMd)
                val greenBaseColor = if (greenChecked) CunnyColors.primaryShadow else CunnyColors.tactileShadow
                val greenBg = if (greenChecked) CunnyColors.primaryPale else CunnyColors.accentGreen.copy(alpha = 0.12f)
                val greenInteractionSource = remember { MutableInteractionSource() }
                val greenIsPressed by greenInteractionSource.collectIsPressedAsState()
                val greenOffsetY by animateDpAsState(
                    targetValue = if (greenIsPressed) 0.dp else (-3).dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "greenOffset"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 3.dp)
                        .background(greenBaseColor, shape = greenShape)
                        .clickable(
                            interactionSource = greenInteractionSource,
                            indication = null,
                            onClick = { greenChecked = true }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = greenOffsetY)
                            .clip(greenShape)
                            .background(greenBg)
                            .border(
                                width = 1.dp,
                                color = if (greenChecked) CunnyColors.primary.copy(alpha = 0.24f) else CunnyColors.borderLight,
                                shape = greenShape
                            )
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(24.dp).background(Color.Green, CircleShape))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Green Pixel", fontSize = 11.sp, fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
                        Text("R: 0, G: 255, B: 0", fontSize = 9.sp, fontFamily = DmSansFontFamily, color = CunnyColors.textSubtle)
                    }
                }

                // Blue Sample Pixel
                val blueShape = RoundedCornerShape(CunnyDimens.radiusMd)
                val blueBaseColor = if (blueChecked) CunnyColors.primaryShadow else CunnyColors.tactileShadow
                val blueBg = if (blueChecked) CunnyColors.primaryPale else CunnyColors.primary.copy(alpha = 0.12f)
                val blueInteractionSource = remember { MutableInteractionSource() }
                val blueIsPressed by blueInteractionSource.collectIsPressedAsState()
                val blueOffsetY by animateDpAsState(
                    targetValue = if (blueIsPressed) 0.dp else (-3).dp,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "blueOffset"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 3.dp)
                        .background(blueBaseColor, shape = blueShape)
                        .clickable(
                            interactionSource = blueInteractionSource,
                            indication = null,
                            onClick = { blueChecked = true }
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = blueOffsetY)
                            .clip(blueShape)
                            .background(blueBg)
                            .border(
                                width = 1.dp,
                                color = if (blueChecked) CunnyColors.primary.copy(alpha = 0.24f) else CunnyColors.borderLight,
                                shape = blueShape
                            )
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(modifier = Modifier.size(24.dp).background(Color.Blue, CircleShape))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Blue Pixel", fontSize = 11.sp, fontFamily = SoraFontFamily, fontWeight = FontWeight.Bold)
                        Text("R: 0, G: 0, B: 255", fontSize = 9.sp, fontFamily = DmSansFontFamily, color = CunnyColors.textSubtle)
                    }
                }
            }
        }
    }
}
}
