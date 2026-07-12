package com.eleonorez.cunny.ui.compose.screens.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eleonorez.cunny.data.billing.BillingManager
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val billingManager = remember { BillingManager.getInstance(context) }
    val availableProducts by billingManager.availableProducts.collectAsState()
    val purchaseState by billingManager.purchaseState.collectAsState()

    // Get price string from ProductDetails, or show fallback
    val priceText = availableProducts.firstOrNull()
        ?.subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
        ?.formattedPrice
        ?: "Premium"

    // Navigate back on successful purchase
    LaunchedEffect(purchaseState) {
        if (purchaseState is BillingManager.PurchaseState.Success) {
            onDismiss()
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F7), Color(0xFFF4EFF4))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .cunnyStatusBarPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))

        // Premium badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(CunnyColors.gradGold)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🚀",
                fontSize = 36.sp
            )
        }

        Spacer(Modifier.height(20.dp))

        // Header
        Text(
            text = "Cunny Premium",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Buka potensi belajar tanpa batas",
            fontFamily = DmSansFontFamily,
            fontSize = 15.sp,
            color = CunnyColors.textSubtle,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Benefits card
        GlassSurface(
            shape = RoundedCornerShape(CunnyDimens.radiusLg),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BenefitItem(
                    emoji = "⚡",
                    title = "Energi tak terbatas",
                    subtitle = "Belajar sepuasnya tanpa menunggu"
                )
                BenefitItem(
                    emoji = "🏆",
                    title = "Badge eksklusif premium",
                    subtitle = "Koleksi badge spesial untuk member"
                )
                BenefitItem(
                    emoji = "🎯",
                    title = "Akses semua kursus tanpa batas",
                    subtitle = "Jelajahi seluruh materi pembelajaran"
                )
                BenefitItem(
                    emoji = "📊",
                    title = "Statistik belajar lanjutan",
                    subtitle = "Pantau progres dengan detail lengkap"
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Price display
        Text(
            text = priceText,
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )
        Text(
            text = "/ bulan",
            fontFamily = DmSansFontFamily,
            fontSize = 14.sp,
            color = CunnyColors.textSubtle,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // Subscribe button
        CunnyPrimaryButton(
            text = "Berlangganan Sekarang",
            onClick = {
                val activity = context as? Activity
                val productDetails = availableProducts.firstOrNull()
                if (activity != null && productDetails != null) {
                    billingManager.launchPurchaseFlow(activity, productDetails)
                }
            },
            brush = Brush.linearGradient(CunnyColors.gradGold),
            shadowColor = Color(0xFF866F40),
            enabled = availableProducts.isNotEmpty()
        )

        Spacer(Modifier.height(12.dp))

        // Maybe later button
        Text(
            text = "Nanti Saja",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = CunnyColors.textSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clickable(onClick = onDismiss)
                .padding(vertical = 12.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Disclaimer
        Text(
            text = "Langganan diperpanjang otomatis setiap bulan. Kamu bisa membatalkan kapan saja melalui Google Play Store.",
            fontFamily = DmSansFontFamily,
            fontSize = 11.sp,
            color = CunnyColors.textSubtle.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun BenefitItem(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(CunnyColors.primaryPale),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 20.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CunnyColors.textDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = DmSansFontFamily,
                fontSize = 12.sp,
                color = CunnyColors.textSubtle,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun PaywallPreview() {
    CunnyTheme { PaywallScreen(onDismiss = {}) }
}
