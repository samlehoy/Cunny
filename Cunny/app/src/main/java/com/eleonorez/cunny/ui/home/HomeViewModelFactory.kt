package com.eleonorez.cunny.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.eleonorez.cunny.data.repository.HomeRepository
import com.eleonorez.cunny.data.repository.ProgressRepository
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.di.Injection

class HomeViewModelFactory @JvmOverloads constructor(
    private val repository: HomeRepository,
    private val progressRepository: ProgressRepository,
    private val courseRepository: CourseRepository = Injection.provideCourseRepository()
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository, progressRepository, courseRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


