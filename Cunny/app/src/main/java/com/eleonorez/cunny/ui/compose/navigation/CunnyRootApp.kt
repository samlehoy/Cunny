package com.eleonorez.cunny.ui.compose.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eleonorez.cunny.ui.compose.components.CunnyBottomBar
import com.eleonorez.cunny.ui.compose.screens.bookmarks.BookmarksScreen
import com.eleonorez.cunny.ui.compose.screens.course.CourseDetailScreen
import com.eleonorez.cunny.ui.compose.screens.courses.CoursesScreen
import com.eleonorez.cunny.ui.compose.screens.home.HomeScreen
import com.eleonorez.cunny.ui.compose.screens.lesson.LessonIntroScreen
import com.eleonorez.cunny.ui.compose.screens.lesson.LessonScreen
import com.eleonorez.cunny.ui.compose.screens.playground.PlaygroundScreen
import com.eleonorez.cunny.ui.compose.screens.practice.PracticeScreen
import com.eleonorez.cunny.ui.compose.screens.prediction.PredictionScreen
import com.eleonorez.cunny.ui.compose.screens.settings.SettingsScreen
import com.eleonorez.cunny.ui.compose.screens.auth.LoginScreen
import com.eleonorez.cunny.ui.compose.screens.auth.RegisterScreen
import com.eleonorez.cunny.ui.compose.screens.onboarding.OnboardingStep1Screen
import com.eleonorez.cunny.ui.compose.screens.onboarding.OnboardingRoleScreen
import com.eleonorez.cunny.ui.compose.screens.onboarding.OnboardingStep2Screen
import com.eleonorez.cunny.ui.compose.screens.onboarding.AgeGateScreen
import com.eleonorez.cunny.ui.compose.screens.onboarding.ParentRestrictionScreen
import com.eleonorez.cunny.ui.compose.screens.onboarding.ParentEmailSentScreen
import com.eleonorez.cunny.ui.compose.screens.profile.ProfileScreen
import com.eleonorez.cunny.ui.compose.screens.celebration.CourseCelebrationScreen
import com.eleonorez.cunny.ui.compose.screens.paywall.PaywallScreen
import com.eleonorez.cunny.ml.PredictionResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.rememberCoroutineScope
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import com.eleonorez.cunny.ui.compose.components.animations.MovingFluidBackground

@Composable
fun CunnyRootApp(
    startDestination: String,
    onOnboardingComplete: () -> Unit,
    onRoleSave: (String) -> Unit = {}
) {
    // Theme is provided by MainActivity's CunnyTheme(darkTheme = useDarkTheme)
    val navController = rememberNavController()
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val db = remember { BookmarkRoomDatabase.getDatabase(context) }
        val gamificationDao = remember { db.gamificationDao() }
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route?.substringBefore("/")
        val showBottomBar = currentRoute in CunnyRoutes.TAB_ROUTES

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize()) {
                // Persistent root fluid background for smooth page transitions
                MovingFluidBackground()

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.fillMaxSize(),
                enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn() },
                exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut() },
                popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn() },
                popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut() }
            ) {
                composable(CunnyRoutes.ONBOARDING_STEP_1) {
                    OnboardingStep1Screen(
                        onContinue = { navController.navigate(CunnyRoutes.ONBOARDING_STEP_2) },
                        onSkip = {
                            onOnboardingComplete()
                            navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                popUpTo(CunnyRoutes.ONBOARDING_STEP_1) { inclusive = true }
                            }
                        }
                    )
                }
                composable(CunnyRoutes.ONBOARDING_STEP_2) {
                    OnboardingStep2Screen(
                        onContinue = {
                            navController.navigate(CunnyRoutes.ONBOARDING_ROLE)
                        }
                    )
                }
                composable(CunnyRoutes.ONBOARDING_ROLE) {
                    OnboardingRoleScreen(
                        onRoleSelected = { role ->
                            onOnboardingComplete()
                            onRoleSave(role)
                            if (role == "guru") {
                                // Teacher → navigate to Login (clearing onboarding) then to Register
                                navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                    popUpTo(CunnyRoutes.ONBOARDING_STEP_1) { inclusive = true }
                                }
                                navController.navigate(CunnyRoutes.AUTH_REGISTER)
                            } else {
                                // Student → age gate
                                navController.navigate(CunnyRoutes.AGE_GATE)
                            }
                        },
                        onLoginClicked = {
                            onOnboardingComplete()
                            navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                popUpTo(CunnyRoutes.ONBOARDING_STEP_1) { inclusive = true }
                            }
                        }
                    )
                }
                // Age Gate Screen
                composable(CunnyRoutes.AGE_GATE) {
                    AgeGateScreen(
                        onBack = { navController.popBackStack() },
                        onAgeConfirmed = { birthYear, isMinor ->
                            if (isMinor) {
                                navController.currentBackStackEntry
                                    ?.savedStateHandle?.set("birthYear", birthYear)
                                navController.navigate(CunnyRoutes.PARENT_RESTRICTION)
                            } else {
                                // Non-minor → navigate to Login (clearing onboarding) then to Register
                                navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                    popUpTo(CunnyRoutes.ONBOARDING_STEP_1) { inclusive = true }
                                }
                                navController.navigate(CunnyRoutes.AUTH_REGISTER)
                            }
                        }
                    )
                }

                // Parent Restriction Screen
                composable(CunnyRoutes.PARENT_RESTRICTION) {
                    val birthYear = navController.previousBackStackEntry
                        ?.savedStateHandle?.get<Int>("birthYear") ?: 2015
                    ParentRestrictionScreen(
                        birthYear = birthYear,
                        onBack = { navController.popBackStack() },
                        onEmailSent = { parentEmail ->
                            navController.navigate(
                                CunnyRoutes.parentEmailSent(parentEmail)
                            )
                        }
                    )
                }

                // Parent Email Sent Screen
                composable(
                    route = CunnyRoutes.PARENT_EMAIL_SENT,
                    arguments = listOf(
                        navArgument("parentEmail") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val parentEmail = backStackEntry.arguments?.getString("parentEmail") ?: ""
                    ParentEmailSentScreen(
                        parentEmail = parentEmail,
                        onGoToLogin = {
                            navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                popUpTo(CunnyRoutes.ONBOARDING_STEP_1) { inclusive = true }
                            }
                        }
                    )
                }

                composable(CunnyRoutes.AUTH_LOGIN) {
                    LoginScreen(
                        onLoginSuccess = {
                            navController.navigate(CunnyRoutes.HOME) {
                                popUpTo(CunnyRoutes.AUTH_LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateRegister = { navController.navigate(CunnyRoutes.AGE_GATE) }
                    )
                }
                composable(CunnyRoutes.AUTH_REGISTER) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            navController.navigate(CunnyRoutes.HOME) {
                                popUpTo(CunnyRoutes.AUTH_LOGIN) { inclusive = true }
                            }
                        },
                        onNavigateLogin = {
                            navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                popUpTo(CunnyRoutes.AUTH_REGISTER) { inclusive = true }
                            }
                        }
                    )
                }
                composable(CunnyRoutes.HOME) {
                    HomeScreen(
                        onStartLesson = { lessonSlug ->
                            navController.navigate(CunnyRoutes.lessonIntro(lessonSlug))
                        },
                        onOpenCourse = { courseSlug ->
                            navController.navigate(CunnyRoutes.courseDetail(courseSlug))
                        }
                    )
                }
                composable(CunnyRoutes.COURSES) {
                    CoursesScreen(
                        onCourseClick = { courseSlug ->
                            navController.navigate(CunnyRoutes.courseDetail(courseSlug))
                        }
                    )
                }
                composable(CunnyRoutes.BOOKMARKS) {
                    BookmarksScreen(
                        onBack = { navController.popBackStack() },
                        onBookmarkClick = { title ->
                            val route = when {
                                title.contains("Apa itu AI", ignoreCase = true) || title.contains("What is AI", ignoreCase = true) -> {
                                    CunnyRoutes.lessonIntro("what-is-ai")
                                }
                                title.contains("Belajar", ignoreCase = true) || title.contains("Learn", ignoreCase = true) -> {
                                    CunnyRoutes.lessonIntro("how-ai-learns")
                                }
                                else -> {
                                    CunnyRoutes.courseDetail("intro-to-ai")
                                }
                            }
                            navController.navigate(route)
                        }
                    )
                }
                composable(CunnyRoutes.SETTINGS) {
                    SettingsScreen(
                        onBookmarksClick = { navController.navigate(CunnyRoutes.BOOKMARKS) },
                        onProfile = { navController.navigate(CunnyRoutes.PROFILE) },
                        onLogout = {
                            Firebase.auth.signOut()
                            scope.launch {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                    try {
                                        gamificationDao.clearAllGamificationData()
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                                navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable(
                    route = CunnyRoutes.COURSE_DETAIL,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { entry ->
                    val slug = entry.arguments?.getString("slug") ?: "intro-to-ai"
                    CourseDetailScreen(
                        slug = slug,
                        onBack = { navController.popBackStack() },
                        onStartLesson = { lessonSlug ->
                            navController.navigate(CunnyRoutes.lessonIntro(lessonSlug))
                        },
                        onStartPractice = { practiceSlug ->
                            navController.navigate(CunnyRoutes.practice(practiceSlug))
                        }
                    )
                }
                composable(
                    route = CunnyRoutes.LESSON_INTRO,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { entry ->
                    val slug = entry.arguments?.getString("slug") ?: "what-is-ai"
                    LessonIntroScreen(
                        slug = slug,
                        onBack = { navController.popBackStack() },
                        onStart = {
                            navController.navigate(CunnyRoutes.lesson(slug)) {
                                popUpTo(CunnyRoutes.LESSON_INTRO) { inclusive = true }
                            }
                        }
                    )
                }
                composable(
                    route = CunnyRoutes.LESSON,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { entry ->
                    val slug = entry.arguments?.getString("slug") ?: "what-is-ai"
                    LessonScreen(
                        slug = slug,
                        onBack = { navController.popBackStack() },
                        onNext = { nextSlug, courseSlug ->
                            if (nextSlug != null) {
                                navController.navigate(CunnyRoutes.lessonIntro(nextSlug)) {
                                    popUpTo(CunnyRoutes.COURSE_DETAIL) { inclusive = false }
                                }
                            } else {
                                navController.navigate(CunnyRoutes.courseCelebration(courseSlug)) {
                                    popUpTo(CunnyRoutes.HOME) { inclusive = false }
                                }
                            }
                        },
                        onOpenPractice = { widgetType ->
                            navController.navigate(CunnyRoutes.practice(slug))
                        }
                    )
                }
                composable(
                    route = CunnyRoutes.COURSE_CELEBRATION,
                    arguments = listOf(navArgument("courseSlug") { type = NavType.StringType })
                ) { entry ->
                    val slug = entry.arguments?.getString("courseSlug") ?: "intro-to-ai"
                    CourseCelebrationScreen(
                        courseSlug = slug,
                        onViewJourney = {
                            navController.navigate(CunnyRoutes.courseDetail(slug)) {
                                popUpTo(CunnyRoutes.HOME) { inclusive = false }
                            }
                        },
                        onExploreCourses = {
                            navController.navigate(CunnyRoutes.COURSES) {
                                popUpTo(CunnyRoutes.HOME) { inclusive = false }
                            }
                        }
                    )
                }
                composable(
                    route = CunnyRoutes.PRACTICE,
                    arguments = listOf(navArgument("lessonSlug") { type = NavType.StringType })
                ) { entry ->
                    val lessonSlug = entry.arguments?.getString("lessonSlug") ?: "what-is-ai"
                    PracticeScreen(
                        lessonSlug = lessonSlug,
                        onBack = { navController.popBackStack() },
                        onPredictionReady = { result, imageUri ->
                            navController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("predicted_label", result.predictedLabel)
                                set("confidence", result.confidence.toDouble())
                                set("image_uri", imageUri.toString())
                                set("rationale", result.rationale)
                                set("top_k_json", com.google.gson.Gson().toJson(result.topK))
                            }
                            navController.navigate(CunnyRoutes.PREDICTION)
                        }
                    )
                }
                composable(CunnyRoutes.PREDICTION) {
                    val handle = navController.previousBackStackEntry?.savedStateHandle

                    val imageUri = remember { mutableStateOf<String?>(null) }
                    val predictedLabel = remember { mutableStateOf<String?>(null) }
                    val confidence = remember { mutableStateOf(0.0) }
                    val rationale = remember { mutableStateOf<String?>(null) }
                    val topKJson = remember { mutableStateOf<String?>(null) }

                    handle?.get<String>("image_uri")?.let { imageUri.value = it }
                    handle?.get<String>("predicted_label")?.let { predictedLabel.value = it }
                    handle?.get<Double>("confidence")?.let { confidence.value = it }
                    handle?.get<String>("rationale")?.let { rationale.value = it }
                    handle?.get<String>("top_k_json")?.let { topKJson.value = it }

                    PredictionScreen(
                        imageUri = imageUri.value,
                        predictedLabel = predictedLabel.value,
                        confidence = confidence.value,
                        rationale = rationale.value,
                        topKJson = topKJson.value,
                        onBack = { navController.popBackStack() },
                        onDone = { navController.popBackStack(); navController.popBackStack() }
                    )
                }
                composable(CunnyRoutes.PROFILE) {
                    ProfileScreen(
                        onBack = { navController.popBackStack() },
                        onAccountDeleted = {
                            navController.navigate(CunnyRoutes.AUTH_LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
                composable(CunnyRoutes.PLAYGROUND) {
                    PlaygroundScreen(
                        onBack = null,
                        onOpenPractice = { navController.navigate(CunnyRoutes.practice("playground")) }
                    )
                }
                composable(CunnyRoutes.PAYWALL) {
                    PaywallScreen(
                        onDismiss = { navController.popBackStack() }
                    )
                }
            }

            if (showBottomBar) {
                CunnyBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                )
            }
        }
    }
}
