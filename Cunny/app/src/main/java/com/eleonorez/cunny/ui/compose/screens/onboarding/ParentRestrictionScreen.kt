package com.eleonorez.cunny.ui.compose.screens.onboarding

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eleonorez.cunny.data.model.ParentConsentRequest
import com.eleonorez.cunny.data.retrofit.ApiConfig
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import com.eleonorez.cunny.ui.compose.components.CunnyFormField
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import kotlinx.coroutines.launch

@Composable
fun ParentRestrictionScreen(
    birthYear: Int,
    onBack: () -> Unit,
    onEmailSent: (parentEmail: String) -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var childName by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val isFormValid = childName.isNotBlank() && parentEmail.contains("@") && !isLoading

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(CunnyColors.backgroundWarm, CunnyColors.background)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(start = 32.dp, end = 32.dp, top = 48.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back button
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            CunnyBackButton(onClick = onBack)
        }

        Spacer(Modifier.height(32.dp))

        // Lottie mascot
        Box(
            modifier = Modifier.size(96.dp),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(Modifier.height(16.dp))

        // Title
        Text(
            text = "Perlu izin orang tua",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "Karena kamu berumur 12 tahun atau kurang, orang tua kamu perlu membuat akun untukmu.",
            fontFamily = DmSansFontFamily,
            fontSize = 14.sp,
            color = CunnyColors.textBody,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(32.dp))

        // "Nama kamu" field
        CunnyFormField(
            label = "Nama kamu",
            value = childName,
            onValueChange = { childName = it },
            placeholder = "Masukkan namamu"
        )

        Spacer(Modifier.height(12.dp))

        // "Email orang tua" field
        CunnyFormField(
            label = "Email orang tua",
            value = parentEmail,
            onValueChange = { parentEmail = it },
            placeholder = "parent@email.com",
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(Modifier.weight(1f))

        // Gradient "Kirim email ke orang tua" button
        CunnyPrimaryButton(
            text = "Kirim email ke orang tua",
            enabled = isFormValid,
            isLoading = isLoading,
            onClick = {
                scope.launch {
                    isLoading = true
                    try {
                        val response = ApiConfig.getApiService().requestParentConsent(
                            ParentConsentRequest(
                                child_name = childName.trim(),
                                parent_email = parentEmail.trim(),
                                birth_year = birthYear
                            )
                        )
                        if (response.error == true) {
                            Toast.makeText(
                                context,
                                response.message ?: "Gagal mengirim email",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            onEmailSent(parentEmail.trim())
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Gagal mengirim: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    } finally {
                        isLoading = false
                    }
                }
            }
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun ParentRestrictionScreenPreview() {
    CunnyTheme {
        ParentRestrictionScreen(
            birthYear = 2015,
            onBack = {},
            onEmailSent = {}
        )
    }
}
