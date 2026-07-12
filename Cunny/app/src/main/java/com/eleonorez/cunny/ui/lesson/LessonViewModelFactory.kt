package com.eleonorez.cunny.ui.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eleonorez.cunny.data.repository.LessonRepository
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.repository.ProgressRepository
import com.eleonorez.cunny.di.Injection

class LessonViewModelFactory(
    private val repository: LessonRepository,
    private val courseRepository: CourseRepository = Injection.provideCourseRepository(),
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LessonViewModel::class.java)) {
            return LessonViewModel(repository, courseRepository, progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}



