package com.eleonorez.cunny.data.response

import com.google.gson.annotations.SerializedName
import com.eleonorez.cunny.data.database.BookmarkModel
import com.eleonorez.cunny.data.model.SubMaterialModel

data class MaterialsResponse(

	@field:SerializedName("learningMaterials")
	val learningMaterials: List<LearningMaterial>? = null,

	@field:SerializedName("error")
	val error: Boolean? = null,

	@field:SerializedName("message")
	val message: String? = null
)

data class LearningMaterial(

	@field:SerializedName("sub_body_materials")
	val subBodyMaterials: List<List<String?>?>? = null,

	@field:SerializedName("description")
	val description: String = "",

	@field:SerializedName("id")
	val id: Int = 0,

	@field:SerializedName("title")
	val title: String = "",

	@field:SerializedName("sub_materials")
	val subMaterials: List<List<String?>?>? = null,

	@field:SerializedName("learning_image_path")
	val learningImagePath: String = ""
) {
    fun toBookmarkModel(): BookmarkModel {
        val subMaterialList = mutableListOf<SubMaterialModel>()
        val size = this.subMaterials?.size ?: 0
        for (i in 0 until size) {
            val slug = this.subMaterials?.getOrNull(i)?.getOrNull(0) ?: ""
            val bodyList = this.subBodyMaterials?.getOrNull(i)?.filterNotNull() ?: emptyList()
            val title = bodyList.firstOrNull() ?: ""
            subMaterialList.add(
                SubMaterialModel(
                    id = i,
                    subMaterial = title,
                    subBodyMaterial = bodyList,
                    learningImagePath = this.learningImagePath,
                    slug = slug,
                    parentMaterialId = this.id
                )
            )
        }
        return BookmarkModel(
            id = this.id,
            title = this.title,
            description = this.description,
            learningImagePath = this.learningImagePath,
            subMaterials = subMaterialList
        )
    }
}
