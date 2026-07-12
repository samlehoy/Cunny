package com.eleonorez.cunny.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val SoraFont = GoogleFont("Quicksand")
val SoraFontFamily = FontFamily(
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = SoraFont, fontProvider = provider, weight = FontWeight.ExtraBold)
)

val DmSansFont = GoogleFont("Quicksand")
val DmSansFontFamily = FontFamily(
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = DmSansFont, fontProvider = provider, weight = FontWeight.Bold)
)

fun getTypography(colors: CunnyColorPalette): Typography {
    return Typography(
        displayLarge = TextStyle(
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            color = colors.textDark
        ),
        displayMedium = TextStyle(
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            color = colors.textDark
        ),
        displaySmall = TextStyle(
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 28.sp,
            color = colors.textDark
        ),
        titleLarge = TextStyle(
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            color = colors.textDark
        ),
        titleMedium = TextStyle(
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            color = colors.textDark
        ),
        bodyLarge = TextStyle(
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = colors.textBody
        ),
        bodyMedium = TextStyle(
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = colors.textBody
        ),
        bodySmall = TextStyle(
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = colors.textSubtle
        ),
        labelLarge = TextStyle(
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = colors.textDark
        ),
        labelMedium = TextStyle(
            fontFamily = DmSansFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            color = colors.textDark
        )
    )
}
