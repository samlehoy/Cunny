package com.eleonorez.cunny.data.response

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

data class CategoriesResponse(
    @field:SerializedName("error")
    val error: Boolean? = null,
    @field:SerializedName("message")
    val message: String? = null,
    @field:SerializedName("categories")
    val categories: List<CategoryItem>? = null
)

@Immutable
data class CategoryItem(
    @field:SerializedName("id")
    val id: Int = 0,
    @field:SerializedName("slug")
    val slug: String = "",
    @field:SerializedName("name")
    val name: String = "",
    @field:SerializedName("description")
    val description: String = ""
)
