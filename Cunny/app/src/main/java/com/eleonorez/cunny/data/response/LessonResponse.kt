package com.eleonorez.cunny.data.response

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

data class LessonsListResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("lessons")
    val lessons: List<LessonSummary>? = null
)

data class LessonDetailResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("lesson")
    val lesson: LessonDetail? = null
)

@Immutable
data class LessonSummary(
    @field:SerializedName("id")
    val id: Int = 0,
    @field:SerializedName("slug")
    val slug: String = "",
    @field:SerializedName("language")
    val language: String = "",
    @field:SerializedName("title")
    val title: String = "",
    @field:SerializedName("summary")
    val summary: String? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("updated_at")
    val updatedAt: String? = null
)

data class LessonDetail(
    @field:SerializedName("id")
    val id: Int = 0,
    @field:SerializedName("slug")
    val slug: String = "",
    @field:SerializedName("course_slug")
    val courseSlug: String = "",
    @field:SerializedName("chapter_slug")
    val chapterSlug: String = "",
    @field:SerializedName("lesson_order")
    val lessonOrder: Int = 0,
    @field:SerializedName("language")
    val language: String = "",
    @field:SerializedName("title")
    val title: String = "",
    @field:SerializedName("summary")
    val summary: String? = null,
    @field:SerializedName("blocks")
    val blocks: List<BlockResponse>? = null,
    @field:SerializedName("created_at")
    val createdAt: String? = null,
    @field:SerializedName("updated_at")
    val updatedAt: String? = null
)

data class BlockResponse(
    @field:SerializedName("type")
    val type: String = "",
    @field:SerializedName("markdown")
    val markdown: String? = null,
    @field:SerializedName("image_url")
    val imageUrl: String? = null,
    @field:SerializedName("video_url")
    val videoUrl: String? = null,
    @field:SerializedName("caption")
    val caption: String? = null,
    @field:SerializedName("style")
    val style: String? = null,
    @field:SerializedName("widget_type")
    val widgetType: String? = null,
    @field:SerializedName("config")
    val config: Map<String, Any>? = null,
    @field:SerializedName("question")
    val question: String? = null,
    @field:SerializedName("choices")
    val choices: List<String>? = null,
    @field:SerializedName("answer_index")
    val answerIndex: Int? = null,
    @field:SerializedName("hint")
    val hint: String? = null,
    @field:SerializedName("explanation")
    val explanation: String? = null
)


