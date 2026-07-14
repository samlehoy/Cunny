package com.eleonorez.cunny.ui.compose.screens.playground

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.Package
import com.adamglin.phosphoricons.regular.Brain
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.R
import com.eleonorez.cunny.ui.compose.components.BorderedCard
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun PlaygroundScreen(
    onBack: (() -> Unit)? = null,
    onOpenPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .cunnyStatusBarPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Custom Header Bar with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                CunnyBackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
            }
            Text(
                text = "Playground",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CunnyColors.textDark
            )
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            Text(
                text = "v1: pemindai buah saja. Yang lainnya akan hadir di v2.",
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = CunnyColors.textSubtle
            )
            Spacer(Modifier.height(12.dp))
            PlaygroundCard(
                title = "Pemindai Buah",
                subtitle = "Klasifikasikan gambar dengan AI",
                icon = PhosphorIcons.Regular.Camera,
                iconTint = Color(0xFF2E7D32),
                bg = Brush.linearGradient(listOf(Color(0xFFE0F5E0), Color(0xFFC0ECC0))),
                enabled = true,
                onClick = onOpenPractice
            )
            Spacer(Modifier.height(12.dp))
            PlaygroundCard(
                title = "Game Mengurutkan",
                subtitle = "Latih pengurutan secara visual",
                icon = PhosphorIcons.Regular.Package,
                iconTint = Color(0xFFE65100),
                bg = Brush.linearGradient(listOf(Color(0xFFFFF0E0), Color(0xFFFFE0C0))),
                enabled = false,
                onClick = {},
                badge = "v2"
            )
            Spacer(Modifier.height(12.dp))
            PlaygroundCard(
                title = "Latih AI Anda Sendiri",
                subtitle = "Ajarkan model dari contoh-contoh",
                icon = PhosphorIcons.Regular.Brain,
                iconTint = Color(0xFF6A1B9A),
                bg = Brush.linearGradient(listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7))),
                enabled = false,
                onClick = {},
                badge = "v2"
            )
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PlaygroundCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    bg: Brush,
    enabled: Boolean,
    onClick: () -> Unit,
    badge: String? = null
) {
    val opacity = if (enabled) 1f else 0.5f
    Box {
        BorderedCard(
            onClick = { if (enabled) onClick() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(bg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = if (enabled) CunnyColors.textDark else CunnyColors.textSubtle
                    )
                    Text(
                        text = subtitle,
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = CunnyColors.textSubtle
                    )
                }
                if (enabled) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.CaretRight,
                        contentDescription = null,
                        tint = CunnyColors.textSubtle,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CunnyColors.primary)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = badge,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun PlaygroundPreview() {
    CunnyTheme { PlaygroundScreen(onBack = {}, onOpenPractice = {}) }
}
