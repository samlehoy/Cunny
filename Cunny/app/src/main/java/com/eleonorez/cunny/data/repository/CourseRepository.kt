package com.eleonorez.cunny.data.repository

import com.eleonorez.cunny.data.response.*
import com.eleonorez.cunny.data.retrofit.ApiService

import com.eleonorez.cunny.helper.toUserFriendlyException

class CourseRepository(private val apiService: ApiService) {

    suspend fun getCategories(language: String = "id"): Result<List<CategoryItem>> {
        return try {
            val response = apiService.getCategories(language)
            if (response.error == true) {
                Result.failure(Exception(response.message ?: "Unknown error"))
            } else {
                Result.success(response.categories ?: emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e.toUserFriendlyException())
        }
    }

    suspend fun getCategoryCourses(catSlug: String, language: String = "id"): Result<List<CourseItem>> {
        return try {
            val response = apiService.getCategoryCourses(catSlug, language)
            if (response.error == true) {
                Result.failure(Exception(response.message ?: "Unknown error"))
            } else {
                Result.success(response.courses ?: emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e.toUserFriendlyException())
        }
    }

    suspend fun getCourseJourney(courseSlug: String, language: String = "id"): Result<CourseJourneyResponse> {
        return try {
            val response = apiService.getCourseJourney(courseSlug, language)
            if (response.error == true) {
                Result.failure(Exception(response.message ?: "Unknown error"))
            } else {
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(e.toUserFriendlyException())
        }
    }

    companion object {
        @Volatile
        private var instance: CourseRepository? = null
        fun getInstance(apiService: ApiService): CourseRepository =
            instance ?: synchronized(this) {
                instance ?: CourseRepository(apiService)
            }.also { instance = it }
    }
}
