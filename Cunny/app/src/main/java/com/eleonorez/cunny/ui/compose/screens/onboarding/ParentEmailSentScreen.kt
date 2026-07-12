package com.eleonorez.cunny.ui.compose.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.EnvelopeSimple
import com.adamglin.phosphoricons.regular.Lightbulb
import com.adamglin.phosphoricons.regular.Warning
import com.eleonorez.cunny.ui.compose.components.CunnyDarkButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun ParentEmailSentScreen(
    parentEmail: String,
    onGoToLogin: () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CunnyColors.backgroundWarm, CunnyColors.background)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(1f))

        // Envelope icon in glass circle
        GlassSurface(
            shape = CircleShape,
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.EnvelopeSimple,
                contentDescription = "Email sent",
                tint = CunnyColors.primary,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        // Title
        Text(
            text = "Email terkirim!",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // Annotated subtitle with highlighted email
        val subtitleText = buildAnnotatedString {
            append("Kami sudah mengirim link ke ")
            withStyle(
                style = SpanStyle(
                    color = CunnyColors.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(parentEmail)
            }
            append(". Minta orang tua kamu untuk cek email dan membuat akun untukmu.")
        }

        Text(
            text = subtitleText,
            fontFamily = DmSansFontFamily,
            fontSize = 14.sp,
            color = CunnyColors.textBody,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(24.dp))

        // Glass info card
        GlassSurface(
            shape = RoundedCornerShape(CunnyDimens.radiusMd),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Tip row
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Lightbulb,
                        contentDescription = null,
                        tint = CunnyColors.accentYellow,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Setelah akun dibuat oleh orang tua, kamu bisa login di halaman login.",
                        fontFamily = DmSansFontFamily,
                        fontSize = 13.sp,
                        color = CunnyColors.textBody,
                        lineHeight = 18.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Spam folder warning
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = PhosphorIcons.Regular.Warning,
                        contentDescription = null,
                        tint = CunnyColors.accentOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Cek juga folder spam jika email tidak ditemukan.",
                        fontFamily = DmSansFontFamily,
                        fontSize = 12.sp,
                        color = CunnyColors.accentOrange,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.height(48.dp))

        // Gradient "Kembali ke Login" button
        CunnyDarkButton(
            text = "Kembali ke Login",
            onClick = onGoToLogin
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun ParentEmailSentScreenPreview() {
    CunnyTheme {
        ParentEmailSentScreen(
            parentEmail = "parent@example.com",
            onGoToLogin = {}
        )
    }
}
