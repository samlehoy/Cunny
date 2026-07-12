package com.eleonorez.cunny.data.repository

import com.eleonorez.cunny.data.response.MaterialsResponse
import com.eleonorez.cunny.data.retrofit.ApiService

class MaterialsRepository (private val apiService: ApiService) {

    // Fungsi untuk mengambil data materi dari API
    suspend fun getMaterials(): MaterialsResponse {
        return apiService.getLearningMaterials()
    }

    companion object {
        @Volatile
        private var instance: MaterialsRepository? = null
        fun getInstance(
            apiService: ApiService,
        ): MaterialsRepository =
            instance ?: synchronized(this) {
                instance ?: MaterialsRepository(apiService)
            }.also { instance = it }
    }
}

