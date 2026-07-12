package com.eleonorez.cunny.ui.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.response.CategoryItem
import com.eleonorez.cunny.data.response.CourseItem
import kotlinx.coroutines.launch

class CourseViewModel(private val repository: CourseRepository) : ViewModel() {

    private val _categories = MutableLiveData<List<CategoryItem>>(emptyList())
    val categories: LiveData<List<CategoryItem>> get() = _categories

    private val _courses = MutableLiveData<List<CourseItem>>(emptyList())
    val courses: LiveData<List<CourseItem>> get() = _courses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun clearError() {
        _error.value = null
    }

    fun retry() {
        clearError()
        fetchCategoriesAndCourses()
    }

    fun fetchCategoriesAndCourses(language: String = "id") {
        viewModelScope.launch {
            _isLoading.value = true
            val catResult = repository.getCategories(language)
            if (catResult.isSuccess) {
                val cats = catResult.getOrNull() ?: emptyList()
                _categories.value = cats
                if (cats.isNotEmpty()) {
                    fetchCoursesForCategory(cats.first().slug, language)
                } else {
                    _isLoading.value = false
                }
            } else {
                _error.value = catResult.exceptionOrNull()?.message
                _isLoading.value = false
            }
        }
    }

    fun fetchCoursesForCategory(categorySlug: String, language: String = "id") {
        viewModelScope.launch {
            _isLoading.value = true
            android.util.Log.d("CourseViewModel", "Fetching courses for category: $categorySlug")
            val courseResult = repository.getCategoryCourses(categorySlug, language)
            if (courseResult.isSuccess) {
                val list = courseResult.getOrNull() ?: emptyList()
                android.util.Log.d("CourseViewModel", "Fetched ${list.size} courses")
                _courses.value = list
            } else {
                val errorMsg = courseResult.exceptionOrNull()?.message ?: "Unknown error"
                android.util.Log.e("CourseViewModel", "Error fetching courses: $errorMsg")
                _error.value = errorMsg
            }
            _isLoading.value = false
        }
    }
}

class CourseViewModelFactory(private val repository: CourseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CourseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CourseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
