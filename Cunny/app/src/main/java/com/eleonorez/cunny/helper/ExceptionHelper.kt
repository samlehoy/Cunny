package com.eleonorez.cunny.helper

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.toUserFriendlyMessage(): String {
    return when (this) {
        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException,
        is IOException -> "Koneksi internet bermasalah. Periksa jaringan Anda dan coba lagi."
        else -> this.localizedMessage ?: "Terjadi kesalahan. Silakan coba lagi."
    }
}

fun Throwable.toUserFriendlyException(): Exception {
    return Exception(this.toUserFriendlyMessage(), this)
}
