package com.eleonorez.cunny.ui.compose.screens.celebration

import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Bold
import com.adamglin.phosphoricons.bold.Trophy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.eleonorez.cunny.ui.compose.components.CunnyDarkButton
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.NarrationManager
import com.eleonorez.cunny.ui.compose.components.NarrationState
import com.eleonorez.cunny.ui.compose.components.TypewriterText
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import com.eleonorez.cunny.helper.SoundSynthesizer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource


@Composable
fun CourseCelebrationScreen(
    courseSlug: String,
    onViewJourney: () -> Unit,
    onExploreCourses: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))
    val confettiComposition by rememberLottieComposition(LottieCompositionSpec.Asset("confetti.json"))
    var confettiTrigger by remember { mutableStateOf(0) }
    val mascotScale = remember { Animatable(1f) }
    var showCard1 by remember { mutableStateOf(false) }
    var showCard2 by remember { mutableStateOf(false) }
    var showCard3 by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showCard1 = true
        delay(150)
        showCard2 = true
        delay(150)
        showCard3 = true
    }

    // Narration
    val narrationManager = remember { NarrationManager(context) }
    val narrationState by narrationManager.state.collectAsState()
    var hasStartedNarration by remember { mutableStateOf(false) }
    val celebrationMessage = "Selamat! Kamu berhasil menyelesaikan kursus ini. Luar biasa!"

    DisposableEffect(Unit) {
        onDispose { narrationManager.shutdown() }
    }

    // Start TTS after confetti, typewriter syncs to onStart
    LaunchedEffect(Unit) {
        SoundSynthesizer.play(context, SoundSynthesizer.SoundType.SUCCESS)
        delay(1200) // Wait for confetti to play
        narrationManager.speak(celebrationMessage)
    }

    // Sync: typewriter starts only when TTS engine fires onStart
    LaunchedEffect(narrationState) {
        if (narrationState == NarrationState.SPEAKING && !hasStartedNarration) {
            hasStartedNarration = true
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = CunnyColors.gradPlumSoft
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        // Falling Confetti Layer (Lottie-based)
        key(confettiTrigger) {
            LottieAnimation(
                composition = confettiComposition,
                iterations = 1, // play once per tap/load
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Mascot Animation
            val scope = rememberCoroutineScope()
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = mascotScale.value
                        scaleY = mascotScale.value
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            scope.launch {
                                SoundSynthesizer.play(context, SoundSynthesizer.SoundType.SUCCESS)
                                confettiTrigger += 1
                                mascotScale.animateTo(1.25f, spring(stiffness = Spring.StiffnessMediumLow))
                                mascotScale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Banner complete text
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TypewriterText(
                    text = "KURSUS SELESAI!",
                    isPlaying = hasStartedNarration,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CunnyColors.accentOrange,
                    textAlign = TextAlign.Center,
                    charDelayMs = 50
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Introduction to AI",
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = CunnyColors.textDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.9).sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats grid matching CSS layout cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AnimatedVisibility(
                    visible = showCard1,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500)),
                    modifier = Modifier.weight(1f)
                ) {
                    StatCelebrationCard(
                        title = "+40 XP",
                        subtitle = "Diperoleh",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(
                    visible = showCard2,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500)),
                    modifier = Modifier.weight(1f)
                ) {
                    StatCelebrationCard(
                        title = "4",
                        subtitle = "Pelajaran",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                AnimatedVisibility(
                    visible = showCard3,
                    enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 }, animationSpec = tween(500)),
                    modifier = Modifier.weight(1f)
                ) {
                    StatCelebrationCard(
                        title = "12",
                        subtitle = "Latihan",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Badge Pill Card
            val badgeShape = RoundedCornerShape(CunnyDimens.radiusFull)
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = 3.dp)
                    .background(
                        color = CunnyColors.tactileShadow,
                        shape = badgeShape
                    )
            ) {
                GlassSurface(
                    shape = badgeShape,
                    modifier = Modifier
                        .wrapContentSize()
                        .offset(y = (-3).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = PhosphorIcons.Bold.Trophy,
                            contentDescription = null,
                            tint = CunnyColors.accentYellow,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Lencana: Penjelajah AI",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = CunnyColors.textDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // Action CTAs
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CunnyPrimaryButton(
                    text = "Lihat perjalanan",
                    onClick = onViewJourney
                )
                CunnyOutlineButton(
                    text = "Jelajahi kursus",
                    onClick = onExploreCourses
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatCelebrationCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(CunnyDimens.radiusSurface)
    Box(
        modifier = modifier
            .padding(bottom = 3.dp)
            .background(
                color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                shape = shape
            )
    ) {
        GlassSurface(
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-3).dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = CunnyColors.textDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontFamily = DmSansFontFamily,
                    fontSize = 12.sp,
                    color = CunnyColors.textSubtle
                )
            }
        }
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun CourseCelebrationPreview() {
    CunnyTheme {
        CourseCelebrationScreen(
            courseSlug = "intro-to-ai",
            onViewJourney = {},
            onExploreCourses = {}
        )
    }
}
