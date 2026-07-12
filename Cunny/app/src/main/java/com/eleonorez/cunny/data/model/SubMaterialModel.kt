package com.eleonorez.cunny.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubMaterialModel (
    val id: Int? = null,
    val subMaterial: String? = null,
    val subBodyMaterial : List<String> = emptyList(),
    val learningImagePath: String,

    // recent material
    val parentMaterialId: Int? = null,
    val positionInParent: Int? = null,
    val totalInParent: Int? = null,
    val parentMaterial: LearningMaterialModel? = null,

    // lesson-based (block) fields
    val slug: String? = null,
    val language: String = "id"
):Parcelable

