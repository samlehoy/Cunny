package com.eleonorez.cunny.data.response

import com.google.gson.annotations.SerializedName

data class CategoryCoursesResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("category")
    val category: String = "",
    @field:SerializedName("description")
    val description: String = "",
    @field:SerializedName("courses")
    val courses: List<CourseItem>? = null
)
