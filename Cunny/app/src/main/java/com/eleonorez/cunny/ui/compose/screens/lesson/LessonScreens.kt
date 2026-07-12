package com.eleonorez.cunny.ui.compose.screens.lesson

import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.EaseInOut
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
import com.adamglin.phosphoricons.regular.X
import com.eleonorez.cunny.ui.compose.components.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.di.Injection
import com.eleonorez.cunny.helper.GamificationManager
import com.eleonorez.cunny.ui.compose.components.CunnyDarkButton
import com.eleonorez.cunny.ui.compose.components.CunnyOutlineButton
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.StaticAmbientBackground
import com.eleonorez.cunny.ui.compose.components.AmbientBackground
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import com.eleonorez.cunny.helper.SoundSynthesizer
import com.eleonorez.cunny.ui.lesson.LessonUiState
import com.eleonorez.cunny.ui.lesson.LessonViewModel
import com.eleonorez.cunny.ui.lesson.LessonViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun LessonIntroScreen(
    slug: String,
    onBack: () -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = viewModel(
        factory = LessonViewModelFactory(
            Injection.provideLessonRepository(),
            Injection.provideCourseRepository(),
            Injection.provideProgressRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val narrationManager = remember { NarrationManager(context) }

    DisposableEffect(Unit) {
        onDispose { narrationManager.shutdown() }
    }

    LaunchedEffect(slug) {
        viewModel.loadLesson(slug)
    }

    val state by viewModel.uiState.observeAsState(LessonUiState.Loading)
    val courseLessons by viewModel.courseLessons.observeAsState(emptyList())
    val progressState by viewModel.userProgress.collectAsState(initial = null)
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val energy = progressState?.energy ?: 5
    val lastRefillTime = progressState?.lastRefillTime ?: 0L

    LaunchedEffect(energy, lastRefillTime) {
        if (energy < 5 && lastRefillTime > 0L) {
            while (true) {
                currentTime = System.currentTimeMillis()
                kotlinx.coroutines.delay(1000L)
            }
        }
    }

    val timerText = if (energy < 5 && lastRefillTime > 0L) {
        val elapsedMs = currentTime - lastRefillTime
        val intervalMs = 30 * 60 * 1000L
        val nextRefillIn = maxOf(0L, intervalMs - (elapsedMs % intervalMs))
        val minutes = (nextRefillIn / 1000) / 60
        val seconds = (nextRefillIn / 1000) % 60
        val minutesStr = minutes.toString().padStart(2, '0')
        val secondsStr = seconds.toString().padStart(2, '0')
        " ($minutesStr:$secondsStr)"
    } else {
        ""
    }

    AmbientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
        ) {
            Crossfade(
                targetState = state,
                animationSpec = tween(500),
                label = "lessonIntroCrossfade",
                modifier = Modifier.fillMaxSize()
            ) { uiState ->
            when (uiState) {
                is LessonUiState.Loading -> {
                    CunnyLessonSkeleton()
                }
                is LessonUiState.Error -> {
                    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("cunny-mascot.json"))
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LottieAnimation(
                                composition = composition,
                                iterations = LottieConstants.IterateForever,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "Koneksi Terputus",
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = CunnyColors.textDark,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Gagal memuat detail pelajaran. Hubungkan perangkat Anda ke internet lalu coba lagi.",
                            fontFamily = DmSansFontFamily,
                            fontSize = 14.sp,
                            color = CunnyColors.textSubtle,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        CunnyPrimaryButton(
                            text = "Coba Lagi",
                            onClick = { viewModel.loadLesson(slug) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        CunnyOutlineButton(
                            text = "Kembali",
                            onClick = onBack,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                is LessonUiState.Success -> {
                    val lesson = uiState.lesson
                    val courseSlug = lesson.courseSlug
                    val index = courseLessons.indexOf(slug).coerceAtLeast(0)

                    val narrationState by narrationManager.state.collectAsState()
                    var hasStartedNarration by remember { mutableStateOf(false) }

                    // Start TTS first, typewriter waits for audio to actually begin
                    LaunchedEffect(lesson) {
                        kotlinx.coroutines.delay(150) // Short delay to let the screen settle
                        lesson.summary?.let { narrationManager.speak(it) }
                    }

                    // Sync: typewriter starts only when TTS engine fires onStart
                    LaunchedEffect(narrationState) {
                        if (narrationState == NarrationState.SPEAKING && !hasStartedNarration) {
                            hasStartedNarration = true
                        }
                    }

                    val closeInteractionSource = remember { MutableInteractionSource() }
                    val closeIsPressed by closeInteractionSource.collectIsPressedAsState()
                    val closeOffsetY by animateDpAsState(
                        targetValue = if (closeIsPressed) 0.dp else (-3).dp,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "closeOffset"
                    )

                    Column(modifier = Modifier.fillMaxSize()) {
                    // Header close & energy matching HTML exactly (centered GlassSurface elements)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .padding(bottom = 3.dp)
                                .background(
                                    color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = closeInteractionSource,
                                    indication = null,
                                    onClick = onBack
                                )
                        ) {
                            GlassSurface(
                                shape = CircleShape,
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = closeOffsetY)
                            ) {
                                Icon(
                                    imageVector = PhosphorIcons.Regular.X,
                                    contentDescription = "Close",
                                    tint = CunnyColors.textDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        val energyShape = RoundedCornerShape(CunnyDimens.radiusFull)
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .padding(bottom = 3.dp)
                                .background(
                                    color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                                    shape = energyShape
                                )
                        ) {
                            GlassSurface(
                                shape = energyShape,
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .offset(y = (-3).dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "⚡",
                                        fontSize = 16.sp,
                                        color = CunnyColors.textDark
                                    )
                                    Text(
                                        text = "$energy$timerText",
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CunnyColors.textDark
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Large Emoji Art Box or 3D WebP Illustration inside glass circle with 3D tactile elevation
                        val emojiShape = CircleShape
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .padding(bottom = 4.dp)
                                .background(
                                    color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                                    shape = emojiShape
                                )
                        ) {
                            GlassSurface(
                                shape = emojiShape,
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .offset(y = (-4).dp)
                            ) {
                                val lessonDrawable = when (slug) {
                                    "what-is-ai" -> com.eleonorez.cunny.R.drawable.il_lesson_what_is_ai
                                    "ai-taxonomy" -> com.eleonorez.cunny.R.drawable.il_lesson_ai_taxonomy
                                    "ai-vs-traditional-program" -> com.eleonorez.cunny.R.drawable.il_lesson_ai_vs_traditional_program
                                    "ai-around-us" -> com.eleonorez.cunny.R.drawable.il_lesson_ai_around_us
                                    "ai-sensors-perception" -> com.eleonorez.cunny.R.drawable.il_lesson_ai_sensors_perception
                                    else -> null
                                }

                                if (lessonDrawable != null) {
                                    Image(
                                        painter = painterResource(id = lessonDrawable),
                                        contentDescription = lesson.title,
                                        modifier = Modifier.size(100.dp)
                                    )
                                } else {
                                    val emoji = when (slug) {
                                        "what-is-ai" -> "🤖"
                                        "how-ai-learns" -> "🍎"
                                        "supervised-vs-unsupervised" -> "📊"
                                        "image-classification" -> "📷"
                                        "ai-can-be-wrong" -> "⚠️"
                                        "ai-around-us" -> "🌍"
                                        else -> "🤖"
                                    }
                                    Text(
                                        text = emoji,
                                        fontSize = 64.sp,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Lesson ${index + 1} of ${courseLessons.size}",
                            fontFamily = DmSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CunnyColors.textSubtle,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = lesson.title,
                            fontFamily = SoraFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = CunnyColors.textDark,
                            textAlign = TextAlign.Center,
                            lineHeight = 34.sp,
                            letterSpacing = (-0.5).sp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        TypewriterText(
                            text = lesson.summary ?: "",
                            isPlaying = hasStartedNarration,
                            fontSize = 15.sp,
                            color = CunnyColors.textBody,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val hasEnergy = energy > 0
                        val buttonText = if (hasEnergy) {
                            "Spend 1 energy to start"
                        } else {
                            val minutesStr = if (energy < 5 && lastRefillTime > 0L) {
                                val elapsedMs = currentTime - lastRefillTime
                                val intervalMs = 30 * 60 * 1000L
                                val nextRefillIn = maxOf(0L, intervalMs - (elapsedMs % intervalMs))
                                val minutes = (nextRefillIn / 1000) / 60
                                val seconds = (nextRefillIn / 1000) % 60
                                "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
                            } else {
                                "30:00"
                            }
                            "No energy left — next in $minutesStr"
                        }

                        CunnyPrimaryButton(
                            text = buttonText,
                            enabled = hasEnergy,
                            textColor = CunnyColors.textOnDarkSurface,
                            onClick = {
                                if (hasEnergy) {
                                    scope.launch {
                                        SoundSynthesizer.play(context, SoundSynthesizer.SoundType.WHOOSH)
                                        val consumed = GamificationManager(context).consumeEnergy(1)
                                        if (consumed) {
                                            onStart()
                                        }
                                    }
                                }
                            },
                            brush = if (hasEnergy) {
                                Brush.linearGradient(CunnyColors.gradHeroDark)
                            } else {
                                null
                            },
                            pressedBrush = Brush.linearGradient(CunnyColors.gradNavActive),
                            shadowColor = if (hasEnergy) CunnyColors.tactileShadow else Color.Transparent
                        )
                    }
                    } // close inner Column
                }
            }
            } // close Crossfade
        }
    }
}

@Composable
fun LessonScreen(
    slug: String,
    onBack: () -> Unit,
    onNext: (String?, String) -> Unit,
    onOpenPractice: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LessonViewModel = viewModel(
        factory = LessonViewModelFactory(
            Injection.provideLessonRepository(),
            Injection.provideCourseRepository(),
            Injection.provideProgressRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(slug) {
        viewModel.loadLesson(slug)
    }

    val state by viewModel.uiState.observeAsState(LessonUiState.Loading)
    val courseLessons by viewModel.courseLessons.observeAsState(emptyList())
    var stepIndex by rememberSaveable { mutableIntStateOf(0) }

    AmbientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
        ) {
            Crossfade(
                targetState = state,
                animationSpec = tween(500),
                label = "lessonCrossfade"
            ) { uiState ->
            when (uiState) {
                is LessonUiState.Loading -> {
                    CunnyLessonSkeleton()
                }
                is LessonUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Failed to load lesson.", fontFamily = DmSansFontFamily, color = CunnyColors.textSubtle)
                    }
                }
                is LessonUiState.Success -> {
                    val lesson = uiState.lesson
                    val blocks = lesson.blocks
                    val currentBlock = blocks.getOrNull(stepIndex)

                    val courseSlug = lesson.courseSlug
                    val index = courseLessons.indexOf(slug).coerceAtLeast(0)

                    var toastText by remember { mutableStateOf<String?>(null) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top header bar matching HTML (Chevron & Title "Lesson")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CunnyBackButton(onClick = onBack)

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    text = "Lesson",
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = CunnyColors.textDark
                                )

                                Spacer(modifier = Modifier.weight(1f))

                                Box(modifier = Modifier.size(40.dp))
                            }

                            // Progress segments Row positioned below the header bar matching HTML
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                blocks.forEachIndexed { idx, _ ->
                                    val isDone = idx < stepIndex
                                    val isActive = idx == stepIndex
                                    val barColor = when {
                                        isActive -> CunnyColors.primary
                                        isDone -> CunnyColors.primaryLight
                                        else -> CunnyColors.borderLight
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(99.dp))
                                            .background(barColor)
                                    )
                                }
                            }

                            // Core content block renderer
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = lesson.title,
                                    fontFamily = SoraFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = CunnyColors.textDark,
                                    letterSpacing = (-0.5).sp,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                if (currentBlock != null) {
                                    BlockRenderer(
                                        block = currentBlock,
                                        onBlockCompleted = { completed ->
                                            viewModel.setBlockCompleted(stepIndex, completed)
                                        },
                                        onOpenWidget = { widgetType ->
                                            viewModel.setBlockCompleted(stepIndex, true)
                                            onOpenPractice(widgetType)
                                        }
                                    )
                                }
                            }

                            // Bottom glass footer with equal-width actions matching HTML
                            val isBlockCompleted = viewModel.completedBlocks[stepIndex] ?: (currentBlock?.type != "quiz" && currentBlock?.type != "widget")
                            val isLastBlock = stepIndex == blocks.lastIndex

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CunnyOutlineButton(
                                    text = "Back",
                                    onClick = {
                                        if (stepIndex > 0) {
                                            stepIndex -= 1
                                        } else {
                                            onBack()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )

                                val continueBtnText = when {
                                    !isBlockCompleted -> {
                                        val wType = (currentBlock as? WidgetBlock)?.widgetType
                                        when (wType) {
                                            "taxonomy_concentric_circles" -> "Place labels to continue"
                                            "sorting_game" -> "Complete game to continue"
                                            "train_your_own_ai" -> "Train model to continue"
                                            "bias_game" -> "Train and test to continue"
                                            "neuron_sandbox" -> "Align neuron to continue"
                                            "next_word_predictor" -> "Complete sentence to continue"
                                            "recommendation_engine" -> "Place recommendations to continue"
                                            "rule_vs_learning" -> "Run simulation to continue"
                                            "sensory_sandbox" -> "Activate sensors to continue"
                                            "reward_trainer" -> "Achieve success to continue"
                                            "diffusion_sandbox" -> "Denoise image to continue"
                                            "privacy_auditor" -> "Audit permissions to continue"
                                            "spot_the_fake" -> "Spot the fake face to continue"
                                            "pixel_zoom" -> "Inspect pixels to continue"
                                            "grocery_sorter" -> "Sort grocery items to continue"
                                            "data_cleaner" -> "Clean dirty data to continue"
                                            else -> {
                                                if (currentBlock?.type == "quiz") "Answer to continue"
                                                else "Complete activity to continue"
                                            }
                                        }
                                    }
                                    isLastBlock -> "Finish lesson"
                                    else -> "Continue"
                                }

                                CunnyPrimaryButton(
                                    text = continueBtnText,
                                    enabled = isBlockCompleted,
                                    onClick = {
                                        if (isBlockCompleted) {
                                            if (isLastBlock) {
                                                scope.launch {
                                                    SoundSynthesizer.play(context, SoundSynthesizer.SoundType.SUCCESS)
                                                    val gm = GamificationManager(context)
                                                    gm.completeLesson(slug, 100)
                                                    gm.checkBadgeUnlock("lesson-complete")
                                                    
                                                    val currentIndex = courseLessons.indexOf(slug)
                                                    if (currentIndex != -1 && currentIndex < courseLessons.lastIndex) {
                                                        val nextSlug = courseLessons[currentIndex + 1]
                                                        toastText = "🎉 Nice work! +10 XP — next lesson unlocked"
                                                        kotlinx.coroutines.delay(1200)
                                                        onNext(nextSlug, courseSlug)
                                                    } else {
                                                        onNext(null, courseSlug)
                                                    }
                                                }
                                            } else {
                                                stepIndex += 1
                                            }
                                        } else {
                                            val msg = if (currentBlock?.type == "quiz") {
                                                "Please select the correct answer first!"
                                            } else {
                                                "Please complete the scanner first!"
                                            }
                                            CunnyToast.show(msg, CunnyToastType.INFO)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Floating Toast
                        toastText?.let { textVal ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 90.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp)
                                        .background(
                                            color = Color(0xED1E1A1F),
                                            shape = RoundedCornerShape(CunnyDimens.radiusFull)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = Color(255, 252, 250, 40),
                                            shape = RoundedCornerShape(CunnyDimens.radiusFull)
                                        )
                                        .padding(horizontal = 20.dp, vertical = 12.dp)
                                ) {
                                    Text(
                                        text = textVal,
                                        color = Color.White,
                                        fontFamily = DmSansFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            } // close Crossfade
        }
    }
}

@Preview(widthDp = 393, heightDp = 852, name = "Intro")
@Composable
private fun LessonIntroPreview() {
    CunnyTheme {
        LessonIntroScreen(
            slug = "what-is-ai",
            onBack = {},
            onStart = {}
        )
    }
}

@Preview(widthDp = 393, heightDp = 852, name = "Lesson")
@Composable
private fun LessonPreview() {
    CunnyTheme {
        LessonScreen(
            slug = "what-is-ai",
            onBack = {},
            onNext = { _, _ -> },
            onOpenPractice = {}
        )
    }
}
