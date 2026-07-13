package com.eleonorez.cunny.ui.compose.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Fill
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.fill.CheckCircle
import com.adamglin.phosphoricons.fill.Lock
import com.adamglin.phosphoricons.fill.Play
import com.adamglin.phosphoricons.regular.Play
import com.adamglin.phosphoricons.regular.BookOpen
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import com.eleonorez.cunny.data.repository.HomeRepository
import com.eleonorez.cunny.di.Injection
import com.eleonorez.cunny.ui.compose.components.AmbientBackground
import com.eleonorez.cunny.ui.compose.components.BorderedCard
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.CunnyProgressTrack
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.PulsingShimmerEffect
import com.eleonorez.cunny.ui.compose.components.ShimmerEffect
import com.eleonorez.cunny.ui.compose.components.StreakEnergyBar
import com.eleonorez.cunny.ui.compose.components.coloredShadow
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import com.eleonorez.cunny.ui.home.HomeViewModel
import com.eleonorez.cunny.ui.home.HomeViewModelFactory
import com.eleonorez.cunny.ui.home.LessonWithStatus
import com.eleonorez.cunny.ui.home.LessonStatus
import kotlin.math.absoluteValue
import com.eleonorez.cunny.data.sync.SyncManager
import com.eleonorez.cunny.ui.compose.components.SyncStatusIndicator
import androidx.compose.runtime.collectAsState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onStartLesson: (String) -> Unit,
    onOpenCourse: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(
            HomeRepository(BookmarkRoomDatabase.getDatabase(LocalContext.current).bookmarkDao()),
            Injection.provideProgressRepository(LocalContext.current)
        )
    )
) {
    val context = LocalContext.current
    val syncManager = remember { SyncManager(context) }
    val pendingSyncCount by syncManager.getPendingSyncCount().collectAsState(initial = 0)

    val progressState by viewModel.userProgress.collectAsState(initial = null)
    val completions by viewModel.lessonCompletions.collectAsState(initial = emptyList())

    val allCourses by viewModel.courses.collectAsState()
    val recommendedCourse by viewModel.recommendedCourse.collectAsState()
    val allCourseLessons by viewModel.allCourseLessons.collectAsState()
    val courseProgressMap by viewModel.courseProgress.collectAsState()
    val isCoursesLoading by viewModel.isCoursesLoading.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    // Only show courses user has started (≥1 done) or the recommended course
    val activeCourses = remember(allCourses, courseProgressMap, recommendedCourse) {
        android.util.Log.d("HomeScreenDebug", "allCourses size: ${allCourses.size}, recommendedCourse: ${recommendedCourse?.slug}, progressMap keys: ${courseProgressMap.keys}")
        allCourses.filter { course ->
            val done = courseProgressMap[course.slug]?.first ?: 0
            val isActive = done > 0 || course.slug == recommendedCourse?.slug
            android.util.Log.d("HomeScreenDebug", "Course: ${course.slug}, done: $done, isActive: $isActive")
            isActive
        }
    }
    
    LaunchedEffect(allCourses, recommendedCourse, activeCourses, isCoursesLoading, errorMessage) {
        android.util.Log.d("HomeScreenDebug", "State updated -> isCoursesLoading: $isCoursesLoading, error: $errorMessage, activeCourses size: ${activeCourses.size}")
    }

    // Pager over active courses; auto-focus on the recommended course
    val activeCourseIndex = remember(activeCourses, recommendedCourse) {
        if (recommendedCourse != null) {
            activeCourses.indexOfFirst { it.slug == recommendedCourse?.slug }.coerceAtLeast(0)
        } else 0
    }
    val pagerState = rememberPagerState(
        initialPage = activeCourseIndex,
        pageCount = { activeCourses.size }
    )
    val selectedCourse = activeCourses.getOrNull(pagerState.currentPage)

    // Lessons for the selected course
    val courseLessons = remember(selectedCourse, allCourseLessons) {
        if (selectedCourse == null) emptyList()
        else allCourseLessons[selectedCourse.slug] ?: emptyList()
    }

    AmbientBackground(isHome = true, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
        ) {
        // Streak bar + sync indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            StreakEnergyBar(
                streak = progressState?.streak ?: 0,
                xp = progressState?.xp ?: 0,
                energy = progressState?.energy ?: 5,
                lastRefillTime = progressState?.lastRefillTime ?: 0L,
                modifier = Modifier.weight(1f)
            )
            SyncStatusIndicator(
                pendingSyncCount = pendingSyncCount,
                modifier = Modifier.padding(end = 24.dp)
            )
        }

        // Headline and Level — show selected course title
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(
                text = selectedCourse?.title ?: recommendedCourse?.title ?: "Loading...",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = CunnyColors.textDark,
                lineHeight = 34.sp,
                letterSpacing = (-0.9).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "LEVEL ${progressState?.level ?: 1} · RECOMMENDED",
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = CunnyColors.textSubtle,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        // Determine the current home state for crossfade
        val homeState = when {
            activeCourses.isNotEmpty() -> "content"
            errorMessage != null -> "error"
            isCoursesLoading -> "loading"
            else -> "empty"
        }

        Crossfade(
            targetState = homeState,
            animationSpec = tween(500),
            modifier = Modifier.weight(1f),
            label = "homeCrossfade"
        ) { currentState ->
            when (currentState) {
                "content" -> {
                    AnimatedVisibility(
                        visible = activeCourses.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                            initialOffsetY = { it / 6 },
                            animationSpec = tween(600)
                        ),
                        exit = fadeOut()
                    ) {
                    Column {
                        // ═══════════════════════════════════════
                        // CAROUSEL: Course/Material Cards
                        // ═══════════════════════════════════════
                        HorizontalPager(
                            state = pagerState,
                            contentPadding = PaddingValues(horizontal = 56.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            val course = activeCourses[page]
                            val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue

                            val scale = 1f - (pageOffset * 0.16f).coerceIn(0f, 0.16f)
                            val alpha = 1f - (pageOffset * 0.62f).coerceIn(0f, 0.62f)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    }
                                    .padding(8.dp)
                            ) {
                                CourseCarouselCard(
                                    courseSlug = course.slug,
                                    progress = courseProgressMap[course.slug]
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Carousel Indicator Dots
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            activeCourses.forEachIndexed { index, _ ->
                                val active = index == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp)
                                        .size(if (active) 24.dp else 8.dp, 8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            if (active) Brush.linearGradient(CunnyColors.gradPlum)
                                            else Brush.linearGradient(listOf(CunnyColors.border, CunnyColors.border))
                                        )
                                )
                            }
                        }

                        // Progress hint text
                        if (selectedCourse != null) {
                            val progress = courseProgressMap[selectedCourse.slug]
                            val done = progress?.first ?: 0
                            val total = progress?.second ?: 0
                            val allDone = done == total && total > 0
                            Text(
                                text = if (allDone) "Materi selesai! 🎉"
                                       else "$done/$total pelajaran selesai",
                                fontFamily = DmSansFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = CunnyColors.textSubtle,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // ═══════════════════════════════════════
                        // LESSON LIST: Lessons from selected course
                        // ═══════════════════════════════════════
                        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                            // 3D elevation container
                            val lessonCardShape = RoundedCornerShape(CunnyDimens.radiusSurface)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp)
                                    .background(
                                        color = CunnyColors.tactileShadow,
                                        shape = lessonCardShape
                                    )
                            ) {
                            GlassSurface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (-4).dp),
                                shape = lessonCardShape
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    // Show 2 lessons centered on current progress (active + next)
                                    val activeIdx = courseLessons.indexOfFirst { it.status == LessonStatus.ACTIVE }
                                    val displayLessons = if (activeIdx >= 0) {
                                        val start = activeIdx.coerceAtMost((courseLessons.size - 2).coerceAtLeast(0))
                                        courseLessons.subList(start, (start + 2).coerceAtMost(courseLessons.size))
                                    } else {
                                        // No active — show last 2 (most recent progress)
                                        courseLessons.takeLast(2)
                                    }
                                    displayLessons.forEachIndexed { index, lesson ->
                                        if (index > 0) Spacer(Modifier.height(8.dp))
                                        HomeLessonRow(
                                            title = lesson.title,
                                            status = lesson.status,
                                            onClick = {
                                                if (lesson.status != LessonStatus.LOCKED) {
                                                    onStartLesson(lesson.slug)
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Start button
                            val startLesson = courseLessons.firstOrNull { it.status == LessonStatus.ACTIVE }
                                ?: courseLessons.firstOrNull()
                            CunnyPrimaryButton(
                                text = when {
                                    courseLessons.any { it.status == LessonStatus.ACTIVE } -> "Mulai"
                                    courseLessons.all { it.status == LessonStatus.DONE } -> "Ulangi"
                                    else -> "Mulai"
                                },
                                onClick = {
                                    startLesson?.let { onStartLesson(it.slug) }
                                }
                            )

                            Spacer(Modifier.height(80.dp))
                        }
                    } // close Column
                    } // close AnimatedVisibility
                }

                "error" -> {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        HomeErrorState(
                            errorDetail = errorMessage,
                            onRetry = { viewModel.retry() }
                        )
                        Spacer(Modifier.height(100.dp))
                    }
                }

                "loading" -> {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        HomeLoadingSkeleton()
                        Spacer(Modifier.height(100.dp))
                    }
                }

                else -> {
                    // Fallback empty state
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        HomeLoadingSkeleton()
                        Spacer(Modifier.height(100.dp))
                    }
                }
            }
        }
    
        }
    }
}

// ═══════════════════════════════════════════
// Course Carousel Card
// ═══════════════════════════════════════════
@Composable
private fun CourseCarouselCard(
    courseSlug: String,
    progress: Pair<Int, Int>? // (done, total)
) {
    // Load course illustration from assets
    val assetPath = when (courseSlug) {
        "intro-to-ai" -> "images/courses/course_basic_ai.png"
        "how-ai-learns-course" -> "images/courses/course_ai_learning.png"
        "generative-ai-course" -> "images/courses/course_generative_ai.png"
        "ai-ethics-course" -> "images/courses/course_ai_ethics.png"
        else -> null
    }
    val context = LocalContext.current
    val bitmap = remember(assetPath) {
        assetPath?.let {
            try {
                context.assets.open(it).use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) { null }
        }
    }

    val done = progress?.first ?: 0
    val total = progress?.second ?: 0
    val progressFraction = if (total > 0) done.toFloat() / total else 0f

    // No card background — illustration floats directly
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Course illustration
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = courseSlug,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = PhosphorIcons.Regular.BookOpen,
                        contentDescription = null,
                        tint = CunnyColors.primary,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar for the course
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CunnyColors.borderLight)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressFraction)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Brush.linearGradient(CunnyColors.gradPlum))
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// Home Lesson Row (Brilliant-style node + title)
// ═══════════════════════════════════════════
@Composable
private fun HomeLessonRow(
    title: String,
    status: LessonStatus,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed && status != LessonStatus.LOCKED) 0.dp else (-3).dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "pressOffset"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = status != LessonStatus.LOCKED,
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(),
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        // Status node circle (size matching CourseDetailScreen timeline nodes)
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            when (status) {
                LessonStatus.ACTIVE -> {
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
                                .offset(y = pressOffset)
                                .background(
                                    brush = Brush.linearGradient(CunnyColors.gradPlum),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = PhosphorIcons.Regular.Play,
                                contentDescription = null,
                                tint = CunnyColors.textOnPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
                LessonStatus.DONE -> {
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
                                .offset(y = pressOffset)
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
                LessonStatus.LOCKED -> {
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

        Spacer(Modifier.width(18.dp))

        // Lesson title
        Text(
            text = title,
            fontFamily = DmSansFontFamily,
            fontWeight = if (status == LessonStatus.ACTIVE) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 15.sp,
            color = if (status == LessonStatus.LOCKED) CunnyColors.textSubtle else CunnyColors.textDark,
            lineHeight = 20.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeActiveLessonCard(
    title: String,
    status: String, // "done", "active", "locked"
    progress: Float,
    onClick: () -> Unit
) {
    BorderedCard(onClick = onClick) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left Status Icon matching HTML
            val isDone = status == "done"
            val iconBgBrush = if (isDone) {
                Brush.linearGradient(listOf(Color(0xFF36324E), Color(0xFF1A1830)))
            } else if (status == "active") {
                Brush.linearGradient(CunnyColors.gradPlum)
            } else {
                Brush.linearGradient(listOf(CunnyColors.borderLight, CunnyColors.borderLight))
            }
            val iconTint = Color.White

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .let {
                        if (isDone) {
                            it.coloredShadow(
                                color = Color(0xFF1A1830),
                                alpha = 0.24f,
                                borderRadius = 9999.dp,
                                shadowRadius = 12.dp,
                                offsetY = 4.dp
                            )
                        } else it
                    }
                    .background(brush = iconBgBrush, shape = CircleShape)
                    .let {
                        if (isDone) {
                            it.border(1.dp, CunnyColors.glassBorder, CircleShape)
                        } else it
                    },
                contentAlignment = Alignment.Center
            ) {
                when (status) {
                    "done" -> Icon(PhosphorIcons.Fill.CheckCircle, contentDescription = "Done", tint = iconTint, modifier = Modifier.size(20.dp))
                    "active" -> Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                    else -> Icon(PhosphorIcons.Fill.Lock, contentDescription = "Locked", tint = CunnyColors.textSubtle, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (status == "locked") CunnyColors.textSubtle else CunnyColors.textDark
                )
                if (status == "active") {
                    Spacer(Modifier.height(8.dp))
                    CunnyProgressTrack(progress)
                }
            }
        }
    }
}

@Composable
private fun HomeLoadingSkeleton() {
    val skeletonShape = RoundedCornerShape(12.dp)

    Column(modifier = Modifier.fillMaxWidth()) {
        // Title placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(28.dp)
                .clip(skeletonShape)
        )
        Spacer(Modifier.height(8.dp))
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(14.dp)
                .clip(skeletonShape)
        )

        Spacer(Modifier.height(24.dp))

        // Carousel card skeleton — uses pulsing shimmer
        PulsingShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 4f)
                .clip(RoundedCornerShape(CunnyDimens.radiusSurface))
        )

        Spacer(Modifier.height(12.dp))

        // Dots placeholder
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(3) { i ->
                ShimmerEffect(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (i == 0) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Progress text placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(14.dp)
                .clip(skeletonShape)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(16.dp))

        // Lesson list skeleton
        GlassSurface(
            shape = RoundedCornerShape(CunnyDimens.radiusSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(3) { index ->
                    if (index > 0) Spacer(Modifier.height(12.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Circle icon placeholder
                        ShimmerEffect(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            ShimmerEffect(
                                modifier = Modifier
                                    .fillMaxWidth(if (index == 0) 0.5f else 0.7f)
                                    .height(14.dp)
                                    .clip(skeletonShape)
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Button skeleton
        PulsingShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(CunnyDimens.radiusFull))
        )
    }
}

@Composable
private fun HomeErrorState(
    errorDetail: String?,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Text(
            text = "❌",
            fontSize = 40.sp
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Gagal memuat data",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = CunnyColors.textDark,
            textAlign = TextAlign.Center
        )

        if (!errorDetail.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = errorDetail,
                fontFamily = DmSansFontFamily,
                fontSize = 13.sp,
                color = CunnyColors.textSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        CunnyPrimaryButton(
            text = "Coba Lagi",
            onClick = onRetry,
            modifier = Modifier.width(180.dp)
        )
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun HomePreview() {
    CunnyTheme { HomeScreen(onStartLesson = {}, onOpenCourse = {}) }
}
