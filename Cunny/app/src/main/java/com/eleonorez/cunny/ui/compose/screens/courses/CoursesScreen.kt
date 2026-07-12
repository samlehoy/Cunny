package com.eleonorez.cunny.ui.compose.screens.courses

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Brain
import com.adamglin.phosphoricons.regular.Code
import com.adamglin.phosphoricons.regular.ChartBar
import com.adamglin.phosphoricons.regular.Camera
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eleonorez.cunny.di.Injection
import com.eleonorez.cunny.ui.compose.components.AmbientBackground
import com.eleonorez.cunny.ui.compose.components.CourseListRow
import com.eleonorez.cunny.ui.compose.components.CunnyPrimaryButton
import com.eleonorez.cunny.ui.compose.components.GlassSurface
import com.eleonorez.cunny.ui.compose.components.ShimmerEffect
import com.eleonorez.cunny.ui.compose.components.cunnyStatusBarPadding
import com.eleonorez.cunny.ui.theme.CunnyColors
import com.eleonorez.cunny.ui.theme.CunnyDimens
import com.eleonorez.cunny.ui.theme.CunnyTheme
import com.eleonorez.cunny.ui.theme.DmSansFontFamily
import com.eleonorez.cunny.ui.theme.SoraFontFamily
import com.eleonorez.cunny.ui.course.CourseViewModel
import com.eleonorez.cunny.ui.course.CourseViewModelFactory

@Composable
fun CoursesScreen(
    onCourseClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = viewModel(
        factory = CourseViewModelFactory(Injection.provideCourseRepository())
    )
) {
    val categories by viewModel.categories.observeAsState(emptyList())
    val courses by viewModel.courses.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.error.observeAsState(null)

    val context = LocalContext.current
    val progressRepository = remember { Injection.provideProgressRepository(context) }
    val completionsState by progressRepository.lessonCompletions.collectAsState(initial = emptyList())
    val completedSlugs = remember(completionsState) { completionsState.map { it.lessonSlug }.toSet() }

    var selectedCategorySlug by remember { mutableStateOf("ai") }

    LaunchedEffect(Unit) {
        viewModel.fetchCategoriesAndCourses()
        viewModel.fetchCoursesForCategory("ai")
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF9F7), Color(0xFFF4EFF4))
    )

    AmbientBackground(isHome = false, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .cunnyStatusBarPadding()
                .verticalScroll(rememberScrollState())
        ) {
        // Safe inset top padding
        Spacer(modifier = Modifier.height(12.dp))

        // Title
        Text(
            text = "Courses",
            fontFamily = SoraFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = CunnyColors.textDark,
            letterSpacing = (-0.9).sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        // Category scroll bar
        if (categories.isNotEmpty()) {
            CategoryScrollSection(
                categories = categories,
                selectedSlug = selectedCategorySlug,
                onCategorySelected = { slug ->
                    selectedCategorySlug = slug
                    viewModel.fetchCoursesForCategory(slug)
                }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Section Hero Banner matching .section-hero in CSS
        val activeCategoryItem = categories.find { it.slug == selectedCategorySlug }
        if (activeCategoryItem != null) {
            CoursesSectionHero(
                title = activeCategoryItem.name,
                description = activeCategoryItem.description
            )
        }



        // Loading skeleton, error state, or course list
        val coursesState = when {
            isLoading -> "loading"
            errorMessage != null -> "error"
            courses.isEmpty() -> "empty"
            else -> "content"
        }

        Crossfade(
            targetState = coursesState,
            animationSpec = tween(500),
            label = "coursesCrossfade"
        ) { currentState ->
            when (currentState) {
                "loading" -> {
                    CoursesLoadingSkeleton()
                }
                "error" -> {
                    CoursesErrorState(
                        errorDetail = errorMessage,
                        onRetry = { viewModel.retry() }
                    )
                }
                "empty" -> {
                    CoursesEmptyState()
                }
                else -> {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Vertical timeline connector line matching Brilliant.org style
                        Spacer(
                            modifier = Modifier
                                .padding(start = 68.dp, top = 50.dp, bottom = 50.dp)
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(Color(0xFF6C5CE7).copy(alpha = 0.15f))
                                .align(Alignment.TopStart)
                        )

                        Column(modifier = Modifier.fillMaxWidth()) {
                            val courseLessonsMap = mapOf(
                                "intro-to-ai" to listOf(
                                    "what-is-ai",
                                    "ai-taxonomy",
                                    "ai-vs-traditional-program",
                                    "ai-around-us",
                                    "ai-sensors-perception"
                                ),
                                "how-ai-learns-course" to listOf(
                                    "how-ai-learns",
                                    "ai-data-types",
                                    "ai-data-gigo",
                                    "supervised-vs-unsupervised",
                                    "reinforcement-learning-basics",
                                    "thinking-like-a-neuron"
                                ),
                                "generative-ai-course" to listOf(
                                    "how-ai-makes-sentences",
                                    "how-ai-draws"
                                ),
                                "ai-ethics-course" to listOf(
                                    "ai-can-be-wrong",
                                    "bias-in-ai",
                                    "data-privacy-ethics",
                                    "spot-the-fake-deepfakes",
                                    "recommendation-systems"
                                ),
                                "image-classification-cnn" to listOf(
                                    "image-classification",
                                    "how-computers-see-pixels"
                                )
                            )

                            courses.forEach { course ->
                                val activeSlugs = setOf(
                                    "intro-to-ai",
                                    "how-ai-learns-course",
                                    "generative-ai-course",
                                    "ai-ethics-course",
                                    "image-classification-cnn"
                                )
                                val isActive = activeSlugs.contains(course.slug)

                                val subtitleText = when (course.slug) {
                                    "intro-to-ai" -> "5 Lessons \u00B7 12 Exercises"
                                    "how-ai-learns-course" -> "6 Lessons \u00B7 12 Exercises"
                                    "generative-ai-course" -> "2 Lessons \u00B7 6 Exercises"
                                    "ai-ethics-course" -> "5 Lessons \u00B7 15 Exercises"
                                    "image-classification-cnn" -> "2 Lessons \u00B7 6 Exercises"
                                    else -> "Coming Soon"
                                }

                                val lessons = courseLessonsMap[course.slug] ?: emptyList()
                                val completedCount = lessons.count { it in completedSlugs }
                                val progressVal = if (lessons.isNotEmpty()) {
                                    completedCount.toFloat() / lessons.size
                                } else {
                                    0f
                                }

                                val courseDrawable = when (course.slug) {
                                    "intro-to-ai" -> com.eleonorez.cunny.R.drawable.ic_course_intro_to_ai
                                    "how-ai-learns-course" -> com.eleonorez.cunny.R.drawable.ic_course_how_ai_learns
                                    "generative-ai-course" -> com.eleonorez.cunny.R.drawable.ic_course_generative_ai
                                    "ai-ethics-course" -> com.eleonorez.cunny.R.drawable.ic_course_ai_ethics
                                    else -> null
                                }

                                CourseListRow(
                                    title = course.title,
                                    subtitle = subtitleText,
                                    progress = progressVal,
                                    emoji = getCategoryEmoji(course.categorySlug),
                                    emojiBg = getCategoryColor(course.categorySlug),
                                    onClick = {
                                        if (isActive) {
                                            onCourseClick(course.slug)
                                        }
                                    },
                                    drawableResId = courseDrawable,
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Bottom padding for floating bottom nav pill
    
        }
    }
}

@Composable
private fun CoursesLoadingSkeleton() {
    val skeletonShape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        // Hero card placeholder
        ShimmerEffect(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(CunnyDimens.radiusSurface))
        )

        Spacer(Modifier.height(16.dp))

        // 4 course row skeletons
        repeat(4) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Circle placeholder for course icon
                ShimmerEffect(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Title placeholder (~60% width)
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(16.dp)
                            .clip(skeletonShape)
                    )

                    Spacer(Modifier.height(8.dp))

                    // Subtitle placeholder (~40% width)
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(12.dp)
                            .clip(skeletonShape)
                    )

                    Spacer(Modifier.height(10.dp))

                    // Progress bar placeholder
                    ShimmerEffect(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun CoursesErrorState(
    errorDetail: String?,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "❌",
                fontSize = 48.sp
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Gagal memuat kursus",
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = CunnyColors.textDark,
                textAlign = TextAlign.Center
            )

            if (!errorDetail.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = errorDetail,
                    fontFamily = DmSansFontFamily,
                    fontSize = 14.sp,
                    color = CunnyColors.textSubtle,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            CunnyPrimaryButton(
                text = "Coba Lagi",
                onClick = onRetry,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
private fun CategoryScrollSection(
    categories: List<com.eleonorez.cunny.data.response.CategoryItem>,
    selectedSlug: String,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        categories.forEach { category ->
            val isActive = category.slug == selectedSlug
            val icon = getCategoryIcon(category.slug)

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            
            // Tactile 3D press vertical offset animation
            val offsetY by animateDpAsState(
                targetValue = if (isPressed) 0.dp else (-3).dp,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "catOffset"
            )

            val shape = RoundedCornerShape(CunnyDimens.radiusMd)

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onCategorySelected(category.slug) }
                    )
                    .width(72.dp)
            ) {
                // Category Icon Wrap Box matching .cat-icon in HTML with 3D tactile elevation
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .padding(bottom = 3.dp)
                        .background(
                            color = CunnyColors.tactileShadow, // Theme-aware 3D shadow base
                            shape = shape
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .offset(y = offsetY)
                            .clip(shape)
                            .background(
                                if (isActive) CunnyColors.glassBg 
                                else CunnyColors.glassBg.copy(alpha = 0.3f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isActive) CunnyColors.glassBorder 
                                        else CunnyColors.glassBorder.copy(alpha = 0.3f),
                                shape = shape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = category.name,
                            tint = if (isActive) CunnyColors.primary else CunnyColors.textSubtle,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = category.name,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (isActive) CunnyColors.textDark else CunnyColors.textSubtle
                )

                // Category Indicator Underline
                if (isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Brush.linearGradient(CunnyColors.gradPlum))
                    )
                }
            }
        }
    }
}

@Composable
private fun CoursesSectionHero(
    title: String,
    description: String
) {
    val shape = RoundedCornerShape(CunnyDimens.radiusSurface)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 4.dp)
            .background(
                color = CunnyColors.primaryShadow, // Theme-aware 3D shadow base
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-4).dp)
                .clip(shape)
                .background(Brush.linearGradient(CunnyColors.gradHeroDark))
                .border(
                    width = 1.dp,
                    color = Color(0x289284A6),
                    shape = shape
                )
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
        Column {
            Text(
                text = if (title == "AI") "AI & Machine Learning" else title,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White,
                lineHeight = 28.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontFamily = DmSansFontFamily,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.72f),
                lineHeight = 21.sp
            )
        }
    }
}
}

@Composable
private fun CoursesEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No courses in this category yet.",
            fontFamily = DmSansFontFamily,
            fontSize = 14.sp,
            color = CunnyColors.textSubtle,
            textAlign = TextAlign.Center
        )
    }
}

private fun getCategoryIcon(slug: String): ImageVector {
    return when (slug) {
        "ai" -> PhosphorIcons.Regular.Brain
        "coding" -> PhosphorIcons.Regular.Code
        "data" -> PhosphorIcons.Regular.ChartBar
        "cnn" -> PhosphorIcons.Regular.Camera
        else -> PhosphorIcons.Regular.Brain
    }
}

private fun getCategoryEmoji(slug: String): String {
    return when (slug) {
        "ai" -> "🤖"
        "coding" -> "💻"
        "data" -> "📊"
        "cnn" -> "📷"
        else -> "🤖"
    }
}

private fun getCategoryColor(slug: String): Color {
    return when (slug) {
        "ai" -> Color(0xFFF0EBF4)
        "coding" -> Color(0xFFEDE6F0)
        "data" -> Color(0xFFF4EFF4)
        "cnn" -> Color(0xFFEDE4F0)
        else -> Color(0xFFF0EBF4)
    }
}

@Preview(widthDp = 393, heightDp = 852)
@Composable
private fun CoursesPreview() {
    CunnyTheme { CoursesScreen(onCourseClick = {}) }
}
