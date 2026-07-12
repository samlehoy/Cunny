package com.eleonorez.cunny.data.repository

import com.eleonorez.cunny.data.model.*
import com.eleonorez.cunny.data.response.*
import com.eleonorez.cunny.data.retrofit.ApiService

import com.eleonorez.cunny.helper.toUserFriendlyException

class LessonRepository(private val apiService: ApiService) {

    suspend fun getLessons(language: String = "id"): Result<List<LessonSummary>> {
        return try {
            val response = apiService.getLessons(language)
            if (response.error == true) {
                Result.failure(Exception(response.message ?: "Unknown error"))
            } else {
                Result.success(response.lessons ?: emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e.toUserFriendlyException())
        }
    }

    suspend fun getLessonBySlug(slug: String, language: String = "id"): Result<Lesson> {
        return try {
            val response = apiService.getLessonBySlug(slug, language)
            if (response.error == true || response.lesson == null) {
                Result.failure(Exception(response.message ?: "Lesson not found"))
            } else {
                val lesson = response.lesson
                val blocks = lesson.blocks?.mapNotNull { blockResp ->
                    parseBlock(blockResp)
                } ?: emptyList()
                Result.success(
                    Lesson(
                        id = lesson.id,
                        slug = lesson.slug,
                        courseSlug = lesson.courseSlug,
                        chapterSlug = lesson.chapterSlug,
                        lessonOrder = lesson.lessonOrder,
                        title = lesson.title,
                        summary = lesson.summary,
                        blocks = blocks,
                        language = lesson.language
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e.toUserFriendlyException())
        }
    }

    private fun parseBlock(block: BlockResponse): Block? {
        return when (block.type) {
            "text" -> TextBlock(markdown = block.markdown ?: "")
            "image" -> ImageBlock(imageUrl = block.imageUrl ?: "", caption = block.caption)
            "video" -> VideoBlock(videoUrl = block.videoUrl ?: "", caption = block.caption)
            "callout" -> CalloutBlock(style = block.style ?: "info", markdown = block.markdown ?: "")
            "widget" -> WidgetBlock(widgetType = block.widgetType ?: "", config = block.config)
            "quiz" -> QuizBlock(
                question = block.question ?: "",
                choices = block.choices ?: emptyList(),
                answerIndex = block.answerIndex ?: 0,
                hint = block.hint,
                explanation = block.explanation
            )
            else -> null
        }
    }

    companion object {
        @Volatile
        private var instance: LessonRepository? = null
        fun getInstance(apiService: ApiService): LessonRepository =
            instance ?: synchronized(this) {
                instance ?: LessonRepository(apiService)
            }.also { instance = it }
    }
}
