package com.eleonorez.cunny.data.retrofit

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import android.content.Context
import android.net.ConnectivityManager

class ApiConfig {
    companion object {
        private const val CONTENT_API_BASE = "https://cunny-content-api.muttaqien0111.workers.dev/api/"

        @Volatile
        private var apiServiceInstance: ApiService? = null

        fun getApiService(): ApiService {
            return apiServiceInstance ?: synchronized(this) {
                apiServiceInstance ?: createService(CONTENT_API_BASE).also { apiServiceInstance = it }
            }
        }

        private fun isNetworkAvailable(context: Context?): Boolean {
            if (context == null) return true
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager != null) {
                val activeNetwork = connectivityManager.activeNetworkInfo
                return activeNetwork != null && activeNetwork.isConnected
            }
            return true
        }

        private fun createService(baseUrl: String): ApiService {
            val loggingInterceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
            
            val authInterceptor = Interceptor { chain ->
                val isFirebaseInitialized = try {
                    com.google.firebase.FirebaseApp.getInstance()
                    true
                } catch (e: IllegalStateException) {
                    false
                }

                val currentUser = if (isFirebaseInitialized) {
                    FirebaseAuth.getInstance().currentUser
                } else null

                val requestBuilder = chain.request().newBuilder()
                
                if (currentUser != null) {
                    try {
                        val tokenResult = Tasks.await(currentUser.getIdToken(true))
                        val token = tokenResult.token
                        if (!token.isNullOrEmpty()) {
                            requestBuilder.addHeader("Authorization", "Bearer $token")
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                chain.proceed(requestBuilder.build())
            }

            val context = try {
                com.google.firebase.FirebaseApp.getInstance().applicationContext
            } catch (e: Exception) {
                null
            }

            val okHttpBuilder = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(loggingInterceptor)

            if (context != null) {
                val cacheSize = 10 * 1024 * 1024L // 10 MB
                val cacheDir = File(context.cacheDir, "http-cache")
                val cache = okhttp3.Cache(cacheDir, cacheSize)
                okHttpBuilder.cache(cache)

                // Force reading from cache if offline (non-authenticated resources only)
                okHttpBuilder.addInterceptor(Interceptor { chain ->
                    var request = chain.request()
                    val path = request.url.encodedPath
                    val isCacheable = !path.contains("auth/") && !path.contains("me/")
                    if (request.method == "GET" && isCacheable && !isNetworkAvailable(context)) {
                        request = request.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=604800") // 7 days stale cache
                            .build()
                    }
                    chain.proceed(request)
                })

                // Rewrite response headers to force caching (non-authenticated resources only)
                okHttpBuilder.addNetworkInterceptor(Interceptor { chain ->
                    val request = chain.request()
                    val response = chain.proceed(request)
                    val path = request.url.encodedPath
                    val isCacheable = !path.contains("auth/") && !path.contains("me/")
                    if (request.method == "GET" && isCacheable) {
                        response.newBuilder()
                            .header("Cache-Control", "public, max-age=86400") // 24 hours online cache
                            .build()
                    } else {
                        response.newBuilder()
                            .header("Cache-Control", "no-store, no-cache, must-revalidate")
                            .build()
                    }
                })
            }

            val client = okHttpBuilder.build()

            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
                .create(ApiService::class.java)
        }

        fun clearCache(context: Context) {
            try {
                val cacheDir = File(context.cacheDir, "http-cache")
                if (cacheDir.exists()) {
                    cacheDir.deleteRecursively()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}


