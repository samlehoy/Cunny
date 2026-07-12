package com.eleonorez.cunny.ui.materials

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.data.repository.MaterialsRepository
import com.eleonorez.cunny.data.response.LearningMaterial
import com.eleonorez.cunny.data.response.MaterialsResponse
import kotlinx.coroutines.launch

// MaterialViewModel.kt
class MaterialViewModel(private val repository: MaterialsRepository) : ViewModel() {

    private val _allMaterials = MutableLiveData<List<LearningMaterial>>(emptyList())
    private val _materials = MutableLiveData<MaterialsResponse>()
    val materials: LiveData<MaterialsResponse> get() = _materials

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    // sumber kebenaran state pencarian
    private val _query = MutableLiveData("")
    val query: LiveData<String> get() = _query

    fun hasBaseData(): Boolean = !_allMaterials.value.isNullOrEmpty()
    fun currentQuery(): String = _query.value.orEmpty()
    fun fetchMaterials(force: Boolean = false) {
        // kalau sudah pernah punya data dan tidak diminta force, jangan fetch ulang
        if (!force && !_allMaterials.value.isNullOrEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getMaterials()
                val list = response?.learningMaterials ?: emptyList()
                _allMaterials.value = list
                // tampilkan full list saat awal/refresh
                _materials.value = MaterialsResponse(list)
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setQuery(q: String) {
        _query.value = q
        applyFilter(q)
    }

    fun clearSearch() {
        _query.value = ""
        val base = _allMaterials.value
        if (!base.isNullOrEmpty()) {
            _materials.value = MaterialsResponse(base)
        }
        // kalau base masih null/empty, diam saja: biar fetchMaterials() yang publish nanti
    }

    private fun applyFilter(q: String) {
        val base = _allMaterials.value ?: emptyList()
        if (q.isBlank()) {
            _materials.value = MaterialsResponse(base)
        } else {
            val filtered = base.filter { it.title.contains(q, ignoreCase = true) }
            _materials.value = MaterialsResponse(filtered)
        }
    }
}

