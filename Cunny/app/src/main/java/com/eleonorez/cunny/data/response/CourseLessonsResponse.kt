package com.eleonorez.cunny.data.response

import com.google.gson.annotations.SerializedName

data class CourseLessonsResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("lessons")
    val lessons: List<LessonSummary>? = null
)
