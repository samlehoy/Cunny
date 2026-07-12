package com.eleonorez.cunny.data.response

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

data class CoursesResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("courses")
    val courses: List<CourseItem>? = null
)

@Immutable
data class CourseItem(
    @field:SerializedName("id")
    val id: Int = 0,
    @field:SerializedName("slug")
    val slug: String = "",
    @field:SerializedName("category_slug")
    val categorySlug: String = "",
    @field:SerializedName("title")
    val title: String = "",
    @field:SerializedName("description")
    val description: String = "",
    @field:SerializedName("image_url")
    val imageUrl: String = ""
)
