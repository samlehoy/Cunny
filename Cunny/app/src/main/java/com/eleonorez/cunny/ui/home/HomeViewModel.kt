package com.eleonorez.cunny.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.data.database.UserProgressEntity
import com.eleonorez.cunny.data.database.LessonCompletionEntity
import com.eleonorez.cunny.data.database.BadgeEntity
import com.eleonorez.cunny.data.retrofit.ApiConfig
import com.google.firebase.auth.FirebaseAuth
import com.eleonorez.cunny.data.repository.ProgressRepository
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.response.CourseItem
import com.eleonorez.cunny.data.response.CourseJourneyResponse
import com.eleonorez.cunny.data.response.JourneyLessonItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Chapter with progress info for the home carousel. */
data class ChapterWithProgress(
    val slug: String,
    val level: Int,
    val title: String,
    val totalLessons: Int,
    val completedLessons: Int,
    val lessonSlugs: List<String>
)

/** Lesson with its completion status for the home carousel. */
data class LessonWithStatus(
    val slug: String,
    val title: String,
    val summary: String?,
    val lessonOrder: Int,
    val status: LessonStatus
)

enum class LessonStatus { DONE, ACTIVE, LOCKED }

class HomeViewModel(
    private val progressRepository: ProgressRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {

    // LiveData untuk loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    val userProgress: Flow<UserProgressEntity?> = progressRepository.userProgress
    val lessonCompletions = progressRepository.lessonCompletions

    private val _courses = MutableStateFlow<List<CourseItem>>(emptyList())
    val courses: StateFlow<List<CourseItem>> = _courses.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isCoursesLoading = MutableStateFlow(true)
    val isCoursesLoading: StateFlow<Boolean> = _isCoursesLoading.asStateFlow()

    private val _journeys = MutableStateFlow<Map<String, CourseJourneyResponse>>(emptyMap())

    val activeLessons: StateFlow<Map<String, JourneyLessonItem?>> = combine(
        _journeys,
        progressRepository.lessonCompletions
    ) { journeys, completions ->
        val completedSlugs = completions.map { it.lessonSlug }.toSet()
        journeys.mapValues { (_, journey) ->
            journey.chapters?.flatMap { it.lessons ?: emptyList() }
                ?.firstOrNull { it.slug !in completedSlugs }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    /** The first course that still has uncompleted lessons. */
    val recommendedCourse: StateFlow<CourseItem?> = combine(
        _courses, activeLessons
    ) { courseList, actives ->
        courseList.firstOrNull { course ->
            actives[course.slug] != null
        } ?: courseList.firstOrNull() // fallback to first course
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    /** Flat lesson lists per course slug, with done/active/locked status. */
    val allCourseLessons: StateFlow<Map<String, List<LessonWithStatus>>> = combine(
        _courses, _journeys, progressRepository.lessonCompletions
    ) { courseList, journeys, completions ->
        val completedSlugs = completions.map { it.lessonSlug }.toSet()
        courseList.associate { course ->
            val journey = journeys[course.slug]
            val lessons = if (journey != null) {
                var foundActive = false
                journey.chapters?.flatMap { it.lessons ?: emptyList() }?.map { lesson ->
                    val status = when {
                        lesson.slug in completedSlugs -> LessonStatus.DONE
                        !foundActive -> { foundActive = true; LessonStatus.ACTIVE }
                        else -> LessonStatus.LOCKED
                    }
                    LessonWithStatus(
                        slug = lesson.slug,
                        title = lesson.title,
                        summary = lesson.summary,
                        lessonOrder = lesson.lessonOrder,
                        status = status
                    )
                } ?: emptyList()
            } else emptyList()
            course.slug to lessons
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    /** Course progress: total lessons vs completed per course. */
    val courseProgress: StateFlow<Map<String, Pair<Int, Int>>> = combine(
        _courses, _journeys, progressRepository.lessonCompletions
    ) { courseList, journeys, completions ->
        val completedSlugs = completions.map { it.lessonSlug }.toSet()
        courseList.associate { course ->
            val journey = journeys[course.slug]
            val allLessonSlugs = journey?.chapters?.flatMap { ch ->
                ch.lessons?.map { it.slug } ?: emptyList()
            } ?: emptyList()
            val total = allLessonSlugs.size
            val done = allLessonSlugs.count { it in completedSlugs }
            course.slug to (done to total)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    init {
        loadCoursesAndJourneys()
        syncRemoteData()
    }

    fun retry() {
        _error.value = null
        loadCoursesAndJourneys()
    }

    private fun syncRemoteData() {
        viewModelScope.launch {
            // Skip sync if no user is logged in
            if (FirebaseAuth.getInstance().currentUser == null) {
                android.util.Log.d("StartupSync", "⏭ No user logged in, skipping sync")
                return@launch
            }

            try {
                val apiService = ApiConfig.getApiService()
                android.util.Log.d("StartupSync", "▶ Startup sync START")
                
                // 1. Remote Profile Sync — merge with max(local, remote)
                val profileResult = apiService.getMe()
                android.util.Log.d("StartupSync", "📡 getMe: error=${profileResult.error}, xp=${profileResult.user?.xp}, level=${profileResult.user?.level}, streak=${profileResult.user?.streak}")
                profileResult.user?.let { remoteUser ->
                    val localProgress = progressRepository.getProgress()
                    android.util.Log.d("StartupSync", "📱 Local: xp=${localProgress?.xp}, level=${localProgress?.level}, streak=${localProgress?.streak}")
                    val mergedProgress = UserProgressEntity(
                        id = 1,
                        xp = maxOf(localProgress?.xp ?: 0, remoteUser.xp),
                        level = maxOf(localProgress?.level ?: 0, remoteUser.level),
                        streak = maxOf(localProgress?.streak ?: 0, remoteUser.streak),
                        energy = maxOf(localProgress?.energy ?: 0, remoteUser.energy),
                        lastActiveDate = remoteUser.lastActiveDate ?: localProgress?.lastActiveDate ?: ""
                    )
                    progressRepository.upsertProgress(mergedProgress)
                    progressRepository.checkEnergyRefill()
                    android.util.Log.d("StartupSync", "✅ Merged: xp=${mergedProgress.xp}, level=${mergedProgress.level}, streak=${mergedProgress.streak}")
                }

                // 2. Remote Progress Sync (Lessons and Badges)
                val progressResult = apiService.getProgress()
                android.util.Log.d("StartupSync", "📡 getProgress: error=${progressResult.error}, lessons=${progressResult.progress?.size ?: 0}, badges=${progressResult.badges?.size ?: 0}")
                progressResult.progress?.forEach { progressDto ->
                    val slug = progressDto.lessonSlug
                    if (!slug.isNullOrEmpty()) {
                        val existing = progressRepository.getCompletion(slug)
                        val bestScore = maxOf(existing?.bestScore ?: 0, progressDto.score)
                        progressRepository.upsertLessonCompletion(
                            LessonCompletionEntity(
                                lessonSlug = slug,
                                bestScore = bestScore
                            )
                        )
                        android.util.Log.d("StartupSync", "  📝 Restored lesson: $slug score=$bestScore")
                    }
                }
                progressResult.badges?.forEach { badgeDto ->
                    val badgeId = badgeDto.badgeId
                    if (!badgeId.isNullOrEmpty()) {
                        progressRepository.insertBadge(
                            BadgeEntity(
                                badgeId = badgeId
                            )
                        )
                        android.util.Log.d("StartupSync", "  🏅 Restored badge: $badgeId")
                    }
                }
                android.util.Log.d("StartupSync", "▶ Startup sync DONE")
            } catch (e: Exception) {
                android.util.Log.e("StartupSync", "❌ Startup sync FAILED: ${e.javaClass.simpleName}: ${e.message}", e)
            }
        }
    }

    private fun loadCoursesAndJourneys() {
        _isLoading.value = true
        _isCoursesLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val categoriesResult = courseRepository.getCategories()
                if (categoriesResult.isSuccess) {
                    val categories = categoriesResult.getOrNull() ?: emptyList()
                    val allCourses = mutableListOf<CourseItem>()
                    val journeysMap = mutableMapOf<String, CourseJourneyResponse>()

                    for (category in categories) {
                        val coursesResult = courseRepository.getCategoryCourses(category.slug)
                        if (coursesResult.isSuccess) {
                            val coursesList = coursesResult.getOrNull() ?: emptyList()
                            allCourses.addAll(coursesList)

                            for (course in coursesList) {
                                val journeyResult = courseRepository.getCourseJourney(course.slug)
                                if (journeyResult.isSuccess) {
                                    journeyResult.getOrNull()?.let { journey ->
                                        journeysMap[course.slug] = journey
                                    }
                                }
                            }
                        }
                    }
                    _courses.value = allCourses
                    _journeys.value = journeysMap
                } else {
                    _error.value = categoriesResult.exceptionOrNull()?.message ?: "Gagal memuat data"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Terjadi kesalahan"
            } finally {
                _isLoading.postValue(false)
                _isCoursesLoading.value = false
            }
        }
    }
}

