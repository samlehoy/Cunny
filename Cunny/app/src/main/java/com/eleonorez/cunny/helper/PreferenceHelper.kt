package com.eleonorez.cunny.helper

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.eleonorez.cunny.data.model.SubMaterialModel
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {

    companion object {
        private const val PREF_NAME = "RecentMaterialsPrefs"
        private const val KEY_RECENT_MATERIALS = "recent_materials"
        private const val MAX_RECENT = 10
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    fun saveRecentMaterial(material: SubMaterialModel) {
        val current = getRecentMaterials().toMutableList()
        current.removeAll { it.subMaterial == material.subMaterial }
        current.add(0, material)

        if (current.size > MAX_RECENT) {
            current.subList(MAX_RECENT, current.size).clear()
        }

        val type = object : TypeToken<List<SubMaterialModel>>() {}.type
        val json = gson.toJson(current, type)

        sharedPreferences.edit {
            putString(KEY_RECENT_MATERIALS, json)
        }
    }

    fun getRecentMaterials(): List<SubMaterialModel> {
        val materialsJson = sharedPreferences.getString(KEY_RECENT_MATERIALS, null)
            ?: return emptyList()

        return try {
            val type = object : TypeToken<List<SubMaterialModel>>() {}.type
            gson.fromJson<List<SubMaterialModel>>(materialsJson, type) ?: emptyList()
        } catch (e: JsonSyntaxException) {

            e.printStackTrace()

            sharedPreferences.edit {
                remove(KEY_RECENT_MATERIALS)
            }

            emptyList()
        }
    }

    // Progress per materi (pakai materialId)
    fun saveProgress(materialId: Int, progress: Int) {
        sharedPreferences.edit {
            putInt("progress_$materialId", progress)
        }
    }

    fun getProgress(materialId: Int): Int {
        return sharedPreferences.getInt("progress_$materialId", 0)
    }
}

