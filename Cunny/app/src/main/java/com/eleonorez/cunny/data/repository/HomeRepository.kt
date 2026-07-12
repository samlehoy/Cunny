package com.eleonorez.cunny.data.repository

import androidx.lifecycle.LiveData
import com.eleonorez.cunny.data.database.BookmarkDao
import com.eleonorez.cunny.data.database.BookmarkModel

class HomeRepository(private val bookmarkDao: BookmarkDao) {
    val bookmarks: LiveData<List<BookmarkModel>> = bookmarkDao.getAllBookmarks()
}

