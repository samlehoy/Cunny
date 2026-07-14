package com.eleonorez.cunny.ui.compose.screens.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.eleonorez.cunny.ui.compose.components.*
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleonorez.cunny.data.source.Result
import com.eleonorez.cunny.ui.authen.AuthViewModel
import com.eleonorez.cunny.ui.compose.components.StaticAmbientBackground
import com.eleonorez.cunny.ui.compose.components.CunnyFormField
import com.eleonorez.cunny.ui.compose.components.CunnyGoogleButton
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.OrDivider
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}



@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginResult by viewModel.loginResult.observeAsState()
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))
    var isEmailLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    LaunchedEffect(loginResult) {
        when (val result = loginResult) {
            is Result.Success -> {
                isEmailLoading = false
                isGoogleLoading = false
                onLoginSuccess()
            }
            is Result.Error -> {
                isEmailLoading = false
                isGoogleLoading = false
                CunnyToast.show(result.message, CunnyToastType.ERROR)
            }
            is Result.Loading -> {
                if (!isEmailLoading && !isGoogleLoading) {
                    isEmailLoading = true
                }
            }
            else -> {
                isEmailLoading = false
                isGoogleLoading = false
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F7), Color(0xFFF4EFF4))
    )

    StaticAmbientBackground(isCourseDetail = false) {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .cunnyStatusBarPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Centered logo and header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Text(
                text = "Selamat datang kembali",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = CunnyColors.textDark,
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Masuk untuk melanjutkan belajar",
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center
            )
        }

        CunnyFormField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "your@email.com"
        )
        Spacer(Modifier.height(16.dp))
        CunnyFormField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            isPassword = true,
            placeholder = "Min. 8 karakter"
        )
        Spacer(Modifier.height(24.dp))

        CunnyPrimaryButton(
            text = "Masuk",
            onClick = {
                isEmailLoading = true
                viewModel.loginWithEmail(email, password)
            },
            isLoading = isEmailLoading
        )

        OrDivider()

        CunnyGoogleButton(
            text = "Masuk dengan Google",
            onClick = {
                val activity = context.findActivity()
                if (activity != null) {
                    isGoogleLoading = true
                    viewModel.signInWithGoogle(activity, isRegister = false)
                } else {
                    CunnyToast.show("Konteks Activity tidak ditemukan", CunnyToastType.ERROR)
                }
            },
            isLoading = isGoogleLoading
        )

        Spacer(Modifier.height(20.dp))

        val signUpLink = buildAnnotatedString {
            append("Belum punya akun? ")
            withStyle(
                style = SpanStyle(
                    color = CunnyColors.primary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append("Daftar")
            }
        }

        Text(
            text = signUpLink,
            fontFamily = DmSansFontFamily,
            fontSize = 14.sp,
            color = CunnyColors.textSubtle,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onNavigateRegister)
                .padding(8.dp)
        )
    }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val registerResult by viewModel.registerResult.observeAsState()
    val loginResult by viewModel.loginResult.observeAsState()
    var isEmailLoading by remember { mutableStateOf(false) }
    var isGoogleLoading by remember { mutableStateOf(false) }

    LaunchedEffect(registerResult, loginResult) {
        if (registerResult is Result.Success || loginResult is Result.Success) {
            isEmailLoading = false
            isGoogleLoading = false
            onRegisterSuccess()
        }
        if (registerResult is Result.Error) {
            isEmailLoading = false
            isGoogleLoading = false
            CunnyToast.show((registerResult as Result.Error).message, CunnyToastType.ERROR)
        }
        if (loginResult is Result.Error) {
            isEmailLoading = false
            isGoogleLoading = false
            CunnyToast.show((loginResult as Result.Error).message, CunnyToastType.ERROR)
        }
        if (registerResult is Result.Loading || loginResult is Result.Loading) {
            if (!isEmailLoading && !isGoogleLoading) {
                isEmailLoading = true
            }
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F7), Color(0xFFF4EFF4))
    )

    StaticAmbientBackground(isCourseDetail = false) {
        Column(
        modifier = Modifier
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
            CunnyBackButton(onClick = onNavigateLogin)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Buat Akun",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = CunnyColors.textDark
            )
        }

        Column(modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)) {
            CunnyFormField(
                label = "Nama",
                value = name,
                onValueChange = { name = it },
                placeholder = "Nama kamu"
            )
            Spacer(Modifier.height(16.dp))
            CunnyFormField(
                label = "Email",
                value = email,
                onValueChange = { email = it },
                placeholder = "your@email.com"
            )
            Spacer(Modifier.height(16.dp))
            CunnyFormField(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                isPassword = true,
                placeholder = "Min. 8 karakter"
            )
            Spacer(Modifier.height(24.dp))

            CunnyPrimaryButton(
                text = "Daftar",
                onClick = {
                    isEmailLoading = true
                    viewModel.registerWithEmail(name, email, password)
                },
                isLoading = isEmailLoading
            )

            OrDivider()

            CunnyGoogleButton(
                text = "Daftar dengan Google",
                onClick = {
                    val activity = context.findActivity()
                    if (activity != null) {
                        isGoogleLoading = true
                        viewModel.signInWithGoogle(activity, isRegister = true)
                    } else {
                        CunnyToast.show("Konteks Activity tidak ditemukan", CunnyToastType.ERROR)
                    }
                },
                isLoading = isGoogleLoading
            )

            Spacer(Modifier.height(20.dp))

            val logInLink = buildAnnotatedString {
                append("Sudah punya akun? ")
                withStyle(
                    style = SpanStyle(
                        color = CunnyColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append("Masuk")
                }
            }

            Text(
                text = logInLink,
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateLogin)
                    .padding(8.dp)
            )
        }
    }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun LoginPreview() {
    CunnyTheme { LoginScreen(onLoginSuccess = {}, onNavigateRegister = {}) }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun RegisterPreview() {
    CunnyTheme { RegisterScreen(onRegisterSuccess = {}, onNavigateLogin = {}) }
}
