package com.eleonorez.cunny.ui.compose.screens.onboarding

import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eleonorez.cunny.ui.compose.components.CunnyDarkButton
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun OnboardingStep1Screen(
    onContinue: () -> Unit,
    onSkip: () -> Unit = {}
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))

    // Radial gradient representation in Compose using linear gradient fallback that mimics HTML
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CunnyColors.backgroundWarm, CunnyColors.background)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(176.dp)
                    .padding(bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "Hi, I'm Coji.\nYour AI study buddy.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                letterSpacing = (-0.9).sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Short lessons, real practice — built for curious learners.",
                fontFamily = DmSansFontFamily,
                fontSize = 16.sp,
                color = CunnyColors.textBody,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }

        // Onboard dots (3 dots, first active)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(CunnyColors.gradPlum))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.border)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.border)
            )
        }

        CunnyDarkButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
fun OnboardingStep2Screen(
    onContinue: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CunnyColors.backgroundWarm, CunnyColors.background, Color(0xFFFAF7FC))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Text(
                text = "Learn by doing.\nNot by watching.",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp,
                letterSpacing = (-0.9).sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Classify images, answer quizzes, and see how models actually work.",
                fontFamily = DmSansFontFamily,
                fontSize = 16.sp,
                color = CunnyColors.textBody,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                modifier = Modifier.widthIn(max = 280.dp)
            )
        }

        // Onboard dots (3 dots, second active)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.border)
            )
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(CunnyColors.gradPlum))
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.border)
            )
        }

        CunnyDarkButton(text = "Continue", onClick = onContinue)
    }
}

@Composable
fun OnboardingRoleScreen(
    onRoleSelected: (String) -> Unit,
    onLoginClicked: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CunnyColors.backgroundWarm, CunnyColors.background)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(start = 32.dp, end = 32.dp, top = 64.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Kamu seorang...",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Pilih peranmu untuk pengalaman belajar terbaik",
                fontFamily = DmSansFontFamily,
                fontSize = 15.sp,
                color = CunnyColors.textBody,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.widthIn(max = 280.dp)
            )

            Spacer(Modifier.height(40.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleCard(
                    emoji = "👩🏫",
                    label = "Guru",
                    isSelected = selectedRole == "guru",
                    onClick = { selectedRole = "guru" }
                )
                RoleCard(
                    emoji = "🎓",
                    label = "Siswa",
                    isSelected = selectedRole == "siswa",
                    onClick = { selectedRole = "siswa" }
                )
            }
        }

        // Onboard dots (3 dots, third active)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.border)
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CunnyColors.border)
            )
            Box(
                modifier = Modifier
                    .size(width = 24.dp, height = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(CunnyColors.gradPlum))
            )
        }

        CunnyDarkButton(
            text = "Lanjutkan",
            onClick = { selectedRole?.let { onRoleSelected(it) } },
            modifier = Modifier.alpha(if (selectedRole != null) 1f else 0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login link for existing users
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Sudah punya akun? ",
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = CunnyColors.textSubtle
            )
            Text(
                text = "Login",
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = CunnyColors.primary,
                modifier = Modifier.clickable { onLoginClicked() }
            )
        }
    }
}

@Composable
private fun RoleCard(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderBrush = if (isSelected) {
        Brush.linearGradient(CunnyColors.gradPlum)
    } else {
        Brush.linearGradient(listOf(CunnyColors.border, CunnyColors.border))
    }
    val cardBackground = if (isSelected) {
        Color(0xFFF0EBF4)
    } else {
        Color(0xFFF0EBF4).copy(alpha = 0.5f)
    }
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Box(
        modifier = Modifier
            .size(width = 155.dp, height = 160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(borderBrush)
            .padding(borderWidth)
            .clip(RoundedCornerShape(19.dp))
            .background(cardBackground)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = emoji,
                fontSize = 48.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = label,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = CunnyColors.textDark
            )
        }

        // Checkmark in top-right corner when selected
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(CunnyColors.gradPlum)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingStep1Preview() {
    CunnyTheme { OnboardingStep1Screen(onContinue = {}, onSkip = {}) }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingRolePreview() {
    CunnyTheme { OnboardingRoleScreen(onRoleSelected = {}) }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun OnboardingStep2Preview() {
    CunnyTheme { OnboardingStep2Screen(onContinue = {}) }
}

