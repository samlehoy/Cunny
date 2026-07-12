package com.eleonorez.cunny.ui.bookmark

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.*
import com.eleonorez.cunny.data.database.BookmarkModel
import com.eleonorez.cunny.data.repository.BookmarkRepository



class BookmarkViewModel(private val repository: BookmarkRepository) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    // sumber data Room
    private val source: LiveData<List<BookmarkModel>> = repository.bookmarks

    // cache data asli
    private val _all = MutableLiveData<List<BookmarkModel>>(emptyList())

    // hasil yang dipakai UI
    private val _filtered = MediatorLiveData<List<BookmarkModel>>()
    val filteredBookmarks: LiveData<List<BookmarkModel>> get() = _filtered

    // state query
    private val _query = MutableLiveData("")
    val query: LiveData<String> get() = _query

    init {
        _filtered.addSource(source) { list ->
            _all.value = list ?: emptyList()
            applyFilter(_query.value.orEmpty())
        }
        _filtered.addSource(_query) { q ->
            applyFilter(q.orEmpty())
        }
    }

    fun setQuery(q: String) {
        _query.value = q
    }

    fun clearSearch() {
        _query.value = ""             // trigger applyFilter("")
        // tidak perlu set _filtered langsung; applyFilter akan pakai _all
    }

    private fun applyFilter(q: String) {
        val base = _all.value ?: emptyList()
        _filtered.value = if (q.isBlank()) {
            base
        } else {
            base.filter { it.title.contains(q, ignoreCase = true) }
            // bisa tambahkan OR description / subMaterial kalau mau
        }
    }

    fun hasBaseData(): Boolean = !(_all.value.isNullOrEmpty())
    fun currentQuery(): String = _query.value.orEmpty()
}


