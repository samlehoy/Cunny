package com.eleonorez.cunny.data.response

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

data class CourseJourneyResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("course_slug")
    val courseSlug: String = "",
    @field:SerializedName("course_title")
    val courseTitle: String = "",
    @field:SerializedName("course_description")
    val courseDescription: String = "",
    @field:SerializedName("chapters")
    val chapters: List<ChapterItem>? = null
)

@Immutable
data class ChapterItem(
    @field:SerializedName("id")
    val id: Int = 0,
    @field:SerializedName("slug")
    val slug: String = "",
    @field:SerializedName("level")
    val level: Int = 0,
    @field:SerializedName("title")
    val title: String = "",
    @field:SerializedName("lessons")
    val lessons: List<JourneyLessonItem>? = null
)

@Immutable
data class JourneyLessonItem(
    @field:SerializedName("slug")
    val slug: String = "",
    @field:SerializedName("title")
    val title: String = "",
    @field:SerializedName("summary")
    val summary: String? = null,
    @field:SerializedName("lesson_order")
    val lessonOrder: Int = 0,
    @field:SerializedName("done")
    val done: Boolean = false
)
