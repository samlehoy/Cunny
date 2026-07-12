package com.eleonorez.cunny.ui.compose.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.CaretDown
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eleonorez.cunny.ui.compose.components.CunnyDarkButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import java.util.Calendar

@Composable
fun AgeGateScreen(
    onBack: () -> Unit,
    onAgeConfirmed: (birthYear: Int, isMinor: Boolean) -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))
    val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }

    var selectedYear by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CunnyColors.backgroundWarm, CunnyColors.background)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(start = CunnyDimens.space3xl, end = CunnyDimens.space3xl, top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            CunnyBackButton(onClick = onBack)
        }

        Spacer(Modifier.height(CunnyDimens.space3xl))

        // Lottie mascot
        Box(
            modifier = Modifier.size(72.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(CunnyDimens.spaceLg))

        // Title
        Text(
            text = "Berapa umur kamu?",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(CunnyDimens.spaceSm))

        // Subtitle
        Text(
            text = "Kami perlu tahu untuk menjaga keamananmu",
            fontFamily = DmSansFontFamily,
            fontSize = 14.sp,
            color = CunnyColors.textBody,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(CunnyDimens.space3xl))

        // Year dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val offsetY by animateDpAsState(
                targetValue = if (isPressed) 4.dp else 0.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "dropdownOffset"
            )

            val shape = RoundedCornerShape(CunnyDimens.radiusSm)
            val shadowColor = CunnyColors.tactileShadow // Solid 3D warm plum base shadow

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp) // 56.dp field height + 4.dp shadow
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { expanded = true }
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                // Shadow base layer
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .background(color = shadowColor, shape = shape)
                )

                // Floating glass surface layer
                GlassSurface(
                    shape = shape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .offset(y = offsetY)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedYear == 0) "Pilih tahun lahir" else selectedYear.toString(),
                            fontFamily = DmSansFontFamily,
                            fontSize = 15.sp,
                            color = if (selectedYear == 0) CunnyColors.textSubtle else CunnyColors.textDark
                        )
                        Icon(
                            imageVector = PhosphorIcons.Regular.CaretDown,
                            contentDescription = "Expand",
                            tint = CunnyColors.textSubtle,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .background(CunnyColors.background)
            ) {
                (currentYear downTo 1990).forEach { year ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = year.toString(),
                                fontFamily = DmSansFontFamily,
                                fontSize = 15.sp,
                                color = CunnyColors.textDark
                            )
                        },
                        onClick = {
                            selectedYear = year
                            expanded = false
                        }
                    )
                }
            }
        }

        // Age display
        AnimatedVisibility(
            visible = selectedYear != 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val age = currentYear - selectedYear
            Text(
                text = "Umur: $age tahun",
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = CunnyColors.primary,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        // Gradient "Lanjutkan" button
        CunnyDarkButton(
            text = "Lanjutkan",
            enabled = selectedYear != 0,
            onClick = {
                val age = currentYear - selectedYear
                onAgeConfirmed(selectedYear, age <= 12)
            }
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun AgeGateScreenPreview() {
    CunnyTheme { AgeGateScreen(onBack = {}, onAgeConfirmed = { _, _ -> }) }
}
