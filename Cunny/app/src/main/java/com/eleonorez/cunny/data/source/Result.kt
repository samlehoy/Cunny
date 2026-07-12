package com.eleonorez.cunny.data.source

import com.google.firebase.auth.FirebaseUser

/**
 * Kelas sealed untuk merepresentasikan hasil dari suatu operasi,
 * yang umumnya digunakan untuk pemanggilan jaringan (network) atau basis data.
 */
sealed class Result {
    // Merepresentasikan operasi yang berhasil, berisi data pengguna.
    data class Success(val user: FirebaseUser?) : Result()
    // Merepresentasikan kegagalan, berisi pesan kesalahan.
    data class Error(val message: String) : Result()
    // Merepresentasikan operasi yang sedang berlangsung.
    object Loading : Result()
}


