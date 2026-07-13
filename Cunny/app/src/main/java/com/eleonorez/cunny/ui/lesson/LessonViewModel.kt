package com.eleonorez.cunny.ui.lesson

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.data.model.WidgetBlock
import com.eleonorez.cunny.data.model.QuizBlock
import com.eleonorez.cunny.data.model.Lesson
import com.eleonorez.cunny.data.repository.LessonRepository
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.repository.ProgressRepository
import com.eleonorez.cunny.data.database.UserProgressEntity
import com.eleonorez.cunny.data.database.LessonCompletionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

sealed class LessonUiState {
    data object Loading : LessonUiState()
    data class Success(val lesson: Lesson) : LessonUiState()
    data class Error(val message: String) : LessonUiState()
}

class LessonViewModel(
    private val repository: LessonRepository,
    private val courseRepository: CourseRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {

    val userProgress: Flow<UserProgressEntity?> = progressRepository.userProgress
    val lessonCompletions: Flow<List<LessonCompletionEntity>> = progressRepository.lessonCompletions

    private val _uiState = MutableLiveData<LessonUiState>(LessonUiState.Loading)
    val uiState: LiveData<LessonUiState> = _uiState

    val completedBlocks = mutableStateMapOf<Int, Boolean>()

    private val _courseLessons = MutableLiveData<List<String>>()
    val courseLessons: LiveData<List<String>> get() = _courseLessons

    fun loadLesson(slug: String) {
        viewModelScope.launch {
            progressRepository.checkEnergyRefill()
        }
        if (_uiState.value is LessonUiState.Success && (_uiState.value as LessonUiState.Success).lesson.slug == slug) {
            return
        }
        _uiState.value = LessonUiState.Loading
        viewModelScope.launch {
            repository.getLessonBySlug(slug)
                .onSuccess { lesson ->
                    _uiState.value = LessonUiState.Success(lesson)
                    lesson.blocks.forEachIndexed { index, block ->
                        val isAutoComplete = when (block) {
                            is WidgetBlock -> {
                                when (block.widgetType) {
                                    "fruit_scanner", "taxonomy_concentric_circles" -> false
                                    else -> true
                                }
                            }
                            is QuizBlock -> false
                            else -> true
                        }
                        completedBlocks[index] = isAutoComplete
                    }

                    // Dynamically load the course journey when loadLesson is called
                    if (lesson.courseSlug.isNotEmpty()) {
                        courseRepository.getCourseJourney(lesson.courseSlug)
                            .onSuccess { journeyResponse ->
                                val lessonSlugs = journeyResponse.chapters?.flatMap { chapter ->
                                    chapter.lessons?.map { it.slug } ?: emptyList()
                                } ?: emptyList()
                                _courseLessons.value = lessonSlugs
                            }
                            .onFailure {
                                _courseLessons.value = emptyList()
                            }
                    } else {
                        _courseLessons.value = emptyList()
                    }
                }
                .onFailure { exception ->
                    _uiState.value = LessonUiState.Error(exception.message ?: "Failed to load lesson")
                }
        }
    }

    fun setBlockCompleted(index: Int, completed: Boolean) {
        completedBlocks[index] = completed
    }

    fun isLessonCompleted(): Boolean {
        return completedBlocks.values.all { it }
    }
}


