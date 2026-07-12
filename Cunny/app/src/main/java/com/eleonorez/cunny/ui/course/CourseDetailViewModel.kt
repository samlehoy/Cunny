package com.eleonorez.cunny.ui.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.data.database.LessonCompletionEntity
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.repository.ProgressRepository
import com.eleonorez.cunny.data.response.ChapterItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val progressRepository: ProgressRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {
    val lessonCompletions: Flow<List<LessonCompletionEntity>> = progressRepository.lessonCompletions

    private val _courseTitle = MutableLiveData<String>("")
    val courseTitle: LiveData<String> get() = _courseTitle

    private val _courseDescription = MutableLiveData<String>("")
    val courseDescription: LiveData<String> get() = _courseDescription

    private val _chapters = MutableLiveData<List<ChapterItem>>(emptyList())
    val chapters: LiveData<List<ChapterItem>> get() = _chapters

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun fetchCourseJourney(courseSlug: String, language: String = "id") {
        viewModelScope.launch {
            _isLoading.value = true
            val result = courseRepository.getCourseJourney(courseSlug, language)
            if (result.isSuccess) {
                val journey = result.getOrNull()
                _courseTitle.value = journey?.courseTitle ?: ""
                _courseDescription.value = journey?.courseDescription ?: ""
                _chapters.value = journey?.chapters ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }
}
