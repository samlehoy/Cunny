package com.eleonorez.cunny.di

import android.content.Context
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import com.eleonorez.cunny.data.repository.CourseRepository
import com.eleonorez.cunny.data.repository.LessonRepository
import com.eleonorez.cunny.data.repository.MaterialsRepository
import com.eleonorez.cunny.data.repository.ProgressRepository
import com.eleonorez.cunny.data.retrofit.ApiConfig

object Injection {
    fun provideCourseRepository(): CourseRepository {
        val apiService = ApiConfig.getApiService()
        return CourseRepository.getInstance(apiService)
    }

    fun provideRepository(context: Context): MaterialsRepository {
        val apiService = ApiConfig.getApiService()
        return MaterialsRepository.getInstance(apiService)
    }

    fun provideLessonRepository(): LessonRepository {
        val apiService = ApiConfig.getApiService()
        return LessonRepository.getInstance(apiService)
    }

    fun provideProgressRepository(context: Context): ProgressRepository {
        val db = BookmarkRoomDatabase.getDatabase(context)
        return ProgressRepository.getInstance(db.gamificationDao(), context)
    }
}
