package com.eleonorez.cunny.ui.course

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.repository.ProgressRepository

class CourseDetailViewModelFactory(
    private val progressRepository: ProgressRepository,
    private val courseRepository: CourseRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseDetailViewModel::class.java)) {
            return CourseDetailViewModel(progressRepository, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ")
    }
}
