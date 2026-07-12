package com.eleonorez.cunny.ui.authen

sealed class Result {
    data class Success(val data: Any? = null) : Result()
    data class Error(val message: String) : Result()
    object Loading : Result()
}
