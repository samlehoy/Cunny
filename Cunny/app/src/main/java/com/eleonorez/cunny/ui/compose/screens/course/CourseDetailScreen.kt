package com.eleonorez.cunny.ui.compose.screens.course

import androidx.compose.animation.core.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.Play
import com.eleonorez.cunny.ui.compose.components.CunnyBackButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleonorez.cunny.R
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import com.eleonorez.cunny.data.database.UserProgressEntity
import com.eleonorez.cunny.data.repository.HomeRepository
import com.eleonorez.cunny.di.Injection
import com.eleonorez.cunny.ui.compose.components.StaticAmbientBackground
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.coloredShadow
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import com.eleonorez.cunny.ui.course.CourseDetailViewModel
import com.eleonorez.cunny.ui.course.CourseDetailViewModelFactory
import com.eleonorez.cunny.ui.home.HomeViewModel
import com.eleonorez.cunny.ui.home.HomeViewModelFactory

@Composable
fun CourseDetailScreen(
    slug: String,
    onBack: () -> Unit,
    onStartLesson: (String) -> Unit,
    onStartPractice: (String) -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            HomeRepository(BookmarkRoomDatabase.getDatabase(LocalContext.current).bookmarkDao()),
            Injection.provideProgressRepository(LocalContext.current)
        )
    ),
    courseDetailViewModel: CourseDetailViewModel = viewModel(
        factory = CourseDetailViewModelFactory(
            Injection.provideProgressRepository(LocalContext.current),
            Injection.provideCourseRepository()
        )
    )
) {
    val progressState by homeViewModel.userProgress.collectAsState(initial = null)
    val completionsState by courseDetailViewModel.lessonCompletions.collectAsState(initial = emptyList())
    val completedSlugs = completionsState.map { it.lessonSlug }

    val courseTitle by courseDetailViewModel.courseTitle.observeAsState("")
    val courseDescription by courseDetailViewModel.courseDescription.observeAsState("")
    val chapters by courseDetailViewModel.chapters.observeAsState(emptyList())

    LaunchedEffect(slug) {
        courseDetailViewModel.fetchCourseJourney(slug)
    }

    val allLessons = chapters.flatMap { it.lessons ?: emptyList() }

    // Find first uncompleted lesson
    val activeSlug = allLessons.find { lesson ->
        !completedSlugs.contains(lesson.slug)
    }?.slug ?: "what-is-ai"

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9FD), Color(0xFFF0ECF8))
    )

    com.eleonorez.cunny.ui.compose.components.AmbientBackground(isHome = false, modifier = modifier) {
        Column(
        modifier = Modifier
            .fillMaxSize()
            .cunnyStatusBarPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Custom Header Bar with Back Button, Streak & Energy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CunnyBackButton(onClick = onBack)

            Spacer(modifier = Modifier.weight(1f))

            // Streak Pill
            StatPillSmall(text = "🔑 ${progressState?.streak ?: 0}")
            Spacer(modifier = Modifier.width(8.dp))
            // Energy Pill
            StatPillSmall(text = "⚡ ${progressState?.energy ?: 5}")
        }

        // Hero Illustration (Custom Canvas drawing matching SVG)
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(120.dp)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            CourseDetailHeroGraphic(modifier = Modifier.fillMaxSize())
        }

        // Title and Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (courseTitle.isNotEmpty()) courseTitle else "Introduction to AI",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.5).sp
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (courseDescription.isNotEmpty()) {
                    courseDescription
                } else {
                    when (slug) {
                        "ai-ethics-course" -> "Memahami batasan AI, mendeteksi bias data, dan menganalisis sistem rekomendasi media sosial."
                        "intro-to-ai" -> "Mempelajari konsep dasar kecerdasan buatan, bagaimana AI mengenali pola, dan perbedaannya dengan pemrograman biasa."
                        else -> "Mempelajari konsep dasar kecerdasan buatan."
                    }
                },
                fontFamily = DmSansFontFamily,
                fontSize = 15.sp,
                color = CunnyColors.textBody,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${allLessons.size} lessons",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CunnyColors.textDark
                )
                Text(
                    text = "·",
                    fontFamily = DmSansFontFamily,
                    fontSize = 14.sp,
                    color = CunnyColors.textSubtle
                )
                val exercisesCount = when (slug) {
                    "intro-to-ai" -> 12
                    "how-ai-learns-course" -> 12
                    "generative-ai-course" -> 6
                    "ai-ethics-course" -> 15
                    "image-classification-cnn" -> 6
                    else -> 12
                }
                Text(
                    text = "$exercisesCount exercises",
                    fontFamily = DmSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = CunnyColors.textDark
                )
            }
        }

        // Level Section and Journey Node Path List
        chapters.forEach { chapter ->
            Spacer(modifier = Modifier.height(20.dp))

            // Level Section Glass Card with left plum indicator stripe
            val levelShape = RoundedCornerShape(CunnyDimens.radiusSurface)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 4.dp)
                    .background(
                        color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                        shape = levelShape
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-4).dp)
                        .clip(levelShape)
                        .background(CunnyColors.glassBg)
                        .border(
                            width = 1.dp,
                            color = CunnyColors.glassBorder,
                            shape = levelShape
                        )
                        .height(IntrinsicSize.Min)
                ) {
                    // Plum stripe on the left edge
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.verticalGradient(CunnyColors.gradPlum)
                            )
                    )

                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .padding(start = 6.dp)
                ) {
                    Text(
                        text = "LEVEL ${chapter.level ?: 1}",
                        fontFamily = SoraFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CunnyColors.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = chapter.title ?: "Getting Started",
                        fontFamily = SoraFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = CunnyColors.textDark
                    )
                }
            }
        }

            Spacer(modifier = Modifier.height(16.dp))

            // Journey Path Nodes
            val lessons = chapter.lessons.orEmpty()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                lessons.forEachIndexed { i, lesson ->
                    val isDone = completedSlugs.contains(lesson.slug)
                    val lessonIndex = allLessons.indexOfFirst { it.slug == lesson.slug }
                    
                    val isLocked = if (lessonIndex <= 0) false else {
                        val prevLesson = allLessons[lessonIndex - 1]
                        !completedSlugs.contains(prevLesson.slug)
                    }

                    val status = if (isDone) "done" else if (!isLocked) "active" else "locked"

                    JourneyStepNodeRow(
                        title = lesson.title,
                        status = status,
                        onClick = {
                            if (!isLocked) {
                                onStartLesson(lesson.slug)
                            }
                        }
                    )

                    // Draw connector line if not last step
                    if (i < lessons.size - 1) {
                        JourneyConnectorLine(status = status)
                    }
                }
            }
        }

        if (allLessons.isNotEmpty()) {
            Spacer(Modifier.height(32.dp))
            CunnyPrimaryButton(
                text = "Continue lesson",
                onClick = { onStartLesson(activeSlug) },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(100.dp))
        }
    }
    }
}

@Composable
private fun StatPillSmall(text: String) {
    val shape = RoundedCornerShape(CunnyDimens.radiusFull)
    Box(
        modifier = Modifier
            .height(36.dp)
            .padding(bottom = 3.dp)
            .background(
                color = CunnyColors.tactileShadow, // Solid 3D warm plum base shadow
                shape = shape
            )
    ) {
        GlassSurface(
            shape = shape,
            modifier = Modifier
                .fillMaxHeight()
                .offset(y = (-3).dp)
        ) {
            Text(
                text = text,
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = CunnyColors.textDark,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun JourneyStepNodeRow(
    title: String,
    status: String, // "done", "active", "locked"
    onClick: () -> Unit
) {
    val opacity = if (status == "locked") 0.72f else 1.0f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 3D press offset for the core circle (matches CunnyPrimaryButton pattern)
    val coreOffsetY by animateDpAsState(
        targetValue = if (isPressed && status != "locked") 0.dp else (-3).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nodePress"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CunnyDimens.radiusMd))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = status != "locked",
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        // Dot graphic wrapper
        Box(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer(alpha = opacity),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                "active" -> {
                    val infiniteTransition = rememberInfiniteTransition(label = "activeNode")

                    // Smooth arc rotation
                    val arcRotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "arcRotation"
                    )

                    // Pulsing glow
                    val pulseScale by infiniteTransition.animateFloat(
                        initialValue = 0.92f,
                        targetValue = 1.10f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseScale"
                    )

                    val pulseAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.55f,
                        targetValue = 0.18f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulseAlpha"
                    )

                    val density = LocalDensity.current

                    // Layer 1: Pulsing radial glow halo
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .graphicsLayer(
                                scaleX = pulseScale,
                                scaleY = pulseScale,
                                alpha = pulseAlpha
                            )
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        CunnyColors.primaryLight.copy(alpha = 0.50f),
                                        CunnyColors.primary.copy(alpha = 0.18f),
                                        Color.Transparent
                                    ),
                                    radius = with(density) { 38.dp.toPx() }
                                ),
                                shape = CircleShape
                            )
                    )

                    val canvasPrimary = CunnyColors.primary
                    val canvasPrimaryLight = CunnyColors.primaryLight
                    // Layer 2: Animated gradient arc over subtle track
                    Canvas(
                        modifier = Modifier.size(56.dp)
                    ) {
                        val strokeWidth = 3.5.dp.toPx()
                        val arcSize = Size(
                            size.width - strokeWidth,
                            size.height - strokeWidth
                        )
                        val arcTopLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val trackRadius = (size.width - strokeWidth) / 2f

                        // Subtle background track ring
                        drawCircle(
                            color = canvasPrimaryLight.copy(alpha = 0.18f),
                            radius = trackRadius,
                            center = Offset(size.width / 2f, size.height / 2f),
                            style = Stroke(width = strokeWidth)
                        )

                        // Rotating gradient arc (240° sweep with round caps)
                        rotate(arcRotation) {
                            drawArc(
                                brush = Brush.sweepGradient(
                                    0.0f to Color.Transparent,
                                    0.10f to canvasPrimaryLight.copy(alpha = 0.35f),
                                    0.35f to canvasPrimary,
                                    0.55f to canvasPrimaryLight,
                                    0.67f to canvasPrimaryLight.copy(alpha = 0.35f),
                                    0.68f to Color.Transparent,
                                    1.0f to Color.Transparent
                                ),
                                startAngle = 0f,
                                sweepAngle = 240f,
                                useCenter = false,
                                topLeft = arcTopLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Layer 3: Core circle with Play icon (3D elevated + press)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .padding(bottom = 3.dp)
                            .background(
                                color = CunnyColors.primaryShadow,
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = coreOffsetY)
                                .background(
                                    brush = Brush.linearGradient(CunnyColors.gradPlum),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Play,
                                contentDescription = "Mulai pelajaran",
                                tint = CunnyColors.textOnPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                "done" -> {
                    // Done charcoal checkmark dot with 3D base shadow, border & press
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .padding(bottom = 3.dp)
                            .background(
                                color = CunnyColors.primaryShadow,
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .offset(y = coreOffsetY)
                                .background(
                                    brush = Brush.linearGradient(listOf(Color(0xFF36324E), Color(0xFF1A1830))),
                                    shape = CircleShape
                                )
                                .border(1.dp, CunnyColors.glassBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✓",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CunnyColors.textOnDarkSurface
                            )
                        }
                    }
                }
                else -> {
                    // Locked grey dot (diameter 40dp)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(1.dp, CunnyColors.borderLight, CircleShape)
                            .background(CunnyColors.backgroundSoft, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .border(2.dp, CunnyColors.textSubtle, CircleShape)
                        )
                    }
                }
            }
        }



        Spacer(modifier = Modifier.width(18.dp))

        // Label matching DM Sans, font-weight 500, active/done 600 (SemiBold), colored by status
        val labelColor = when (status) {
            "active" -> CunnyColors.textDark
            "done" -> CunnyColors.textDark
            else -> CunnyColors.textSubtle
        }
        val labelWeight = if (status == "active" || status == "done") FontWeight.SemiBold else FontWeight.Medium

        Text(
            text = title,
            fontFamily = DmSansFontFamily,
            fontWeight = labelWeight,
            fontSize = 16.sp,
            color = labelColor,
            lineHeight = 22.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun JourneyConnectorLine(status: String) {
    val parentBg = when (status) {
        "done" -> CunnyColors.primaryPale
        else -> CunnyColors.borderLight
    }

    Box(
        modifier = Modifier
            .padding(start = 37.dp)
            .width(2.dp)
            .height(28.dp)
            .background(parentBg, shape = RoundedCornerShape(2.dp))
            .clip(RoundedCornerShape(2.dp))
    ) {
        if (status == "done") {
            val scaleY = remember { Animatable(0f) }
            LaunchedEffect(Unit) {
                scaleY.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(600, easing = FastOutSlowInEasing)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 0f)
                        this.scaleY = scaleY.value
                    }
                    .background(
                        Brush.verticalGradient(
                            listOf(CunnyColors.primaryLight, CunnyColors.accentGreen)
                        )
                    )
            )
        } else if (status == "active") {
            val infiniteTransition = rememberInfiniteTransition(label = "connectorActive")
            val hintAlpha by infiniteTransition.animateFloat(
                initialValue = 0.35f,
                targetValue = 0.85f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hintAlpha"
            )
            val hintTranslation by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "hintTranslation"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.45f)
                    .offset(y = hintTranslation.dp)
                    .graphicsLayer(alpha = hintAlpha)
                    .background(
                        Brush.verticalGradient(
                            listOf(CunnyColors.primaryLight, Color.Transparent)
                        )
                    )
            )
        }
    }
}
@Composable
private fun CourseDetailHeroGraphic(modifier: Modifier = Modifier) {
    val boardColor = Color(0xFFE8E3F4) // Matches --bg-warm
    val barOrange = CunnyColors.accentOrange
    val barPurple = CunnyColors.primary

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val boardW = w * 0.75f
        val boardH = h * 0.6f
        val boardX = (w - boardW) / 2f
        val boardY = h * 0.1f

        // Board rectangle
        drawRoundRect(
            color = boardColor,
            topLeft = Offset(boardX, boardY),
            size = Size(boardW, boardH),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Text lines illustrations
        drawRoundRect(
            color = barOrange.copy(alpha = 0.35f),
            topLeft = Offset(boardX + 10.dp.toPx(), boardY + 10.dp.toPx()),
            size = Size(boardW * 0.55f, 6.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        drawRoundRect(
            color = barPurple.copy(alpha = 0.35f),
            topLeft = Offset(boardX + 10.dp.toPx(), boardY + 22.dp.toPx()),
            size = Size(boardW * 0.4f, 6.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        drawRoundRect(
            color = barOrange.copy(alpha = 0.25f),
            topLeft = Offset(boardX + 10.dp.toPx(), boardY + 34.dp.toPx()),
            size = Size(boardW * 0.5f, 6.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        // Circle and play button representation
        val cx = boardX + boardW - 16.dp.toPx()
        val cy = boardY + boardH / 2f
        drawCircle(
            color = barPurple.copy(alpha = 0.12f),
            radius = 12.dp.toPx(),
            center = Offset(cx, cy)
        )

        val triPath = Path().apply {
            moveTo(cx - 4.dp.toPx(), cy - 4.dp.toPx())
            lineTo(cx + 5.dp.toPx(), cy)
            lineTo(cx - 4.dp.toPx(), cy + 4.dp.toPx())
            close()
        }
        drawPath(path = triPath, color = barPurple.copy(alpha = 0.5f))

        // Lower shelf
        val bbY = boardY + boardH + 8.dp.toPx()
        drawRoundRect(
            color = barPurple.copy(alpha = 0.2f),
            topLeft = Offset(boardX + 35.dp.toPx(), bbY),
            size = Size(boardW * 0.3f, 8.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun CourseDetailPreview() {
    CunnyTheme {
        CourseDetailScreen(
            slug = "intro-to-ai",
            onBack = {},
            onStartLesson = {},
            onStartPractice = {}
        )
    }
}
