package com.eleonorez.cunny.ui.authen

import android.app.Activity
import android.util.Log
import android.util.Patterns
import android.app.Application
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.eleonorez.cunny.BuildConfig
import com.eleonorez.cunny.data.retrofit.ApiConfig
import com.eleonorez.cunny.data.source.Result
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val context = application.applicationContext

    // LiveData for Login Process
    private val _loginResult = MutableLiveData<Result>()
    val loginResult: LiveData<Result> = _loginResult

    // LiveData for Registration Process
    private val _registerResult = MutableLiveData<Result>()
    val registerResult: LiveData<Result> = _registerResult

    private fun registerUserInPostgres(displayName: String) {
        viewModelScope.launch {
            try {
                val sharedPref = context.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
                val rawRole = sharedPref.getString("user_role", "student") ?: "student"
                // Normalize "guru" to "teacher" for backend uniformity if needed, otherwise pass it directly
                val role = if (rawRole == "guru") "teacher" else "student"

                val apiService = ApiConfig.getApiService()
                val response = apiService.registerUser(mapOf("display_name" to displayName, "role" to role))
                if (response.error == false) {
                    _loginResult.postValue(Result.Success(auth.currentUser))
                } else {
                    _loginResult.postValue(Result.Error(response.message ?: "Registrasi backend gagal"))
                }
            } catch (e: Exception) {
                _loginResult.postValue(Result.Error(e.message ?: "Koneksi ke backend gagal"))
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        // [PERUBAHAN]: Tambahkan pengecekan form kosong di awal
        if (email.isBlank() || password.isBlank()) {
            _loginResult.value = Result.Error("Harap isi email dan password dengan benar.")
            return
        }

        // Validasi spesifik lainnya
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _loginResult.value = Result.Error("Format email tidak valid.")
            return
        }
        if (password.length < 8) {
            _loginResult.value = Result.Error("Password minimal harus 8 karakter.")
            return
        }

        // Start Loading and call Firebase
        _loginResult.value = Result.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    registerUserInPostgres(user?.displayName ?: "Student")
                } else {
                    val exception = task.exception
                    val errorMessage = if (exception is com.google.firebase.auth.FirebaseAuthInvalidUserException) {
                        "No account found for this email."
                    } else {
                        exception?.message ?: "Login gagal"
                    }
                    _loginResult.value = Result.Error(errorMessage)
                }
            }
    }

    fun registerWithEmail(name: String, email: String, password: String) {
        // [PERUBAHAN]: Tambahkan pengecekan form kosong di awal
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _registerResult.value = Result.Error("Harap isi semua kolom dengan benar.")
            return
        }

        // Validasi spesifik lainnya
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _registerResult.value = Result.Error("Format email tidak valid.")
            return
        }
        if (password.length < 8) {
            _registerResult.value = Result.Error("Password minimal harus 8 karakter.")
            return
        }

        // Start Loading and create user
        _registerResult.value = Result.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User created, now update the profile with the name
                    val user = auth.currentUser
                    val profileUpdates = userProfileChangeRequest {
                        displayName = name
                    }
                    user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                        if (profileTask.isSuccessful) {
                            // Register in PostgreSQL directly via ApiService
                            viewModelScope.launch {
                                try {
                                    val sharedPref = context.getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
                                    val rawRole = sharedPref.getString("user_role", "student") ?: "student"
                                    val role = if (rawRole == "guru") "teacher" else "student"

                                    val apiService = ApiConfig.getApiService()
                                    val response = apiService.registerUser(mapOf("display_name" to name, "role" to role))
                                    if (response.error == false) {
                                        _registerResult.value = Result.Success(user)
                                    } else {
                                        _registerResult.value = Result.Error(response.message ?: "Registrasi backend gagal")
                                    }
                                } catch (e: Exception) {
                                    _registerResult.value = Result.Error(e.message ?: "Koneksi ke backend gagal")
                                }
                            }
                        } else {
                            // Profile update failed, but registration succeeded.
                            _registerResult.value =
                                Result.Error("Gagal menyimpan nama, silakan coba lagi.")
                        }
                    }
                } else {
                    _registerResult.value =
                        Result.Error(task.exception?.message ?: "Registrasi gagal")
                }
            }
    }

    /**
     * Initiates Google Sign-In using Credential Manager.
     * Uses a two-step approach to work around Samsung S25 / Android 16
     * CredentialSelectorActivity crash (TransactionTooLargeException when
     * many Google accounts are present on the device).
     *
     * Step 1: Try authorized accounts only (smaller payload)
     * Step 2: If no authorized accounts, fall back to all accounts
     */
    fun signInWithGoogle(activity: Activity, isRegister: Boolean) {
        _loginResult.value = Result.Loading
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)

                Log.d("GoogleSignIn", "Requesting credential with WEB_CLIENT_ID: ${BuildConfig.WEB_CLIENT_ID.take(20)}...")

                // Step 1: Try authorized accounts only (avoids TransactionTooLargeException)
                val response = try {
                    val authorizedOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(true)
                        .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                        .build()
                    val authorizedRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(authorizedOption)
                        .build()
                    Log.d("GoogleSignIn", "Step 1: Trying authorized accounts only...")
                    credentialManager.getCredential(activity, authorizedRequest)
                } catch (e: GetCredentialException) {
                    // Step 2: No authorized accounts, fall back to all accounts
                    Log.d("GoogleSignIn", "Step 1 failed (${e.type}), falling back to all accounts...")
                    val allAccountsOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                        .build()
                    val allAccountsRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(allAccountsOption)
                        .build()
                    credentialManager.getCredential(activity, allAccountsRequest)
                }

                // Handle the credential response
                handleCredentialResponse(response, isRegister)

            } catch (e: GetCredentialException) {
                Log.e("GoogleSignIn", "GetCredentialException: ${e.type} - ${e.message}", e)
                _loginResult.postValue(Result.Error("Google sign-in gagal: ${e.message ?: "No credentials available"}"))
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Sign-in failed", e)
                _loginResult.postValue(Result.Error("Google sign-in gagal: ${e.message}"))
            }
        }
    }

    private fun handleCredentialResponse(response: androidx.credentials.GetCredentialResponse, isRegister: Boolean) {
        when (val credential = response.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val firebaseCredential =
                            GoogleAuthProvider.getCredential(
                                googleIdTokenCredential.idToken,
                                null
                            )
                        loginWithGoogle(firebaseCredential, isRegister)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("GoogleSignIn", "Token parsing failed", e)
                        _loginResult.postValue(Result.Error("Invalid Google token"))
                    }
                } else {
                    Log.e("GoogleSignIn", "Unexpected credential type: ${credential.type}")
                    _loginResult.postValue(Result.Error("Unexpected credential type"))
                }
            }
            else -> {
                Log.e("GoogleSignIn", "Unexpected credential class: ${response.credential.javaClass.simpleName}")
                _loginResult.postValue(Result.Error("Unexpected credential response"))
            }
        }
    }

    private fun loginWithGoogle(credential: AuthCredential, isRegister: Boolean) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser == true
                    if (!isRegister && isNewUser) {
                        // User is new but trying to login -> Delete the new account and reject
                        val user = auth.currentUser
                        user?.delete()?.addOnCompleteListener {
                            auth.signOut()
                            _loginResult.postValue(Result.Error("No account found for this email."))
                        } ?: run {
                            auth.signOut()
                            _loginResult.postValue(Result.Error("No account found for this email."))
                        }
                    } else {
                        val user = auth.currentUser
                        registerUserInPostgres(user?.displayName ?: "Student")
                    }
                } else {
                    _loginResult.postValue(
                        Result.Error(task.exception?.message ?: "Login Google gagal")
                    )
                }
            }
    }

    fun checkIfUserIsLoggedIn() {
        if (auth.currentUser != null) {
            _loginResult.value = Result.Success(auth.currentUser)
        }
    }
}
