package com.eleonorez.cunny.ui.compose.screens.practice

import android.net.Uri
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eleonorez.cunny.R
import com.eleonorez.cunny.ml.PredictionResult
import com.eleonorez.cunny.ui.compose.components.*
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import com.eleonorez.cunny.ui.practice.PracticeUiState
import com.eleonorez.cunny.ui.practice.PracticeViewModel
import com.eleonorez.cunny.utils.getImageUri
import java.io.File

@Composable
fun PracticeScreen(
    lessonSlug: String,
    onBack: () -> Unit,
    onPredictionReady: (PredictionResult, Uri) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PracticeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.observeAsState()
    val imageUri by viewModel.selectedImageUri.observeAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setImageUri(uri)
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (!success) viewModel.setImageUri(null)
    }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is PracticeUiState.Success -> {
                com.eleonorez.cunny.helper.GamificationManager(context).checkBadgeUnlock("first-scan")
                com.eleonorez.cunny.helper.GamificationManager(context).addXp(10)
                onPredictionReady(state.result, state.imageUri)
                viewModel.resetState()
            }
            is PracticeUiState.Error -> {
                CunnyToast.show(state.message, CunnyToastType.ERROR)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))

    StaticAmbientBackground(isCourseDetail = false, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CunnyBackButton(onClick = onBack)

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Latihan",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = CunnyColors.textDark
                )

                Spacer(modifier = Modifier.weight(1f))

                Box(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Mascot / Preview Image
                if (imageUri != null) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).apply {
                                scaleType = ImageView.ScaleType.CENTER_CROP
                            }
                        },
                        update = { view ->
                            view.setImageURI(imageUri)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(CunnyDimens.radiusSurface))
                            .border(
                                width = 1.dp,
                                color = CunnyColors.borderLight,
                                shape = RoundedCornerShape(CunnyDimens.radiusSurface)
                            )
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(160.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Pemindai Buah",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = CunnyColors.textDark,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Ambil foto buah dan lihat bagaimana AI mengklasifikasikannya — dengan confidence dan tebakan lainnya.",
                    fontFamily = DmSansFontFamily,
                    fontSize = 14.sp,
                    color = CunnyColors.textBody,
                    lineHeight = 22.sp
                )

                Spacer(Modifier.height(20.dp))

                // Steps card
                GlassSurface(
                    shape = RoundedCornerShape(CunnyDimens.radiusLg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Langkah-langkah",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = CunnyColors.textDark
                        )

                        val steps = listOf(
                            "Ambil foto atau pilih dari galeri",
                            "Pratinjau gambar yang dipilih",
                            "Kirim ke model AI untuk klasifikasi"
                        )

                        steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(CunnyColors.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontFamily = SoraFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = step,
                                    fontFamily = DmSansFontFamily,
                                    fontSize = 14.sp,
                                    color = CunnyColors.textDark,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Camera / Gallery Buttons Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CunnyOutlineButton(
                        text = "Kamera",
                        icon = PhosphorIcons.Regular.Camera,
                        onClick = {
                            val uri = getImageUri(context)
                            viewModel.setImageUri(uri)
                            cameraLauncher.launch(uri)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    CunnyOutlineButton(
                        text = "Galeri",
                        icon = PhosphorIcons.Regular.Image,
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Classify button
                CunnyPrimaryButton(
                    text = "Unggah & Klasifikasikan",
                    onClick = {
                        val uri = imageUri
                        if (uri != null) {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val tempFile = File.createTempFile("classify_", ".jpg", context.cacheDir)
                            inputStream?.use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            viewModel.uploadImage(tempFile, lessonSlug)
                        } else {
                            CunnyToast.show("Silakan pilih gambar terlebih dahulu", CunnyToastType.INFO)
                        }
                    },
                    isLoading = uiState is PracticeUiState.Loading
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}