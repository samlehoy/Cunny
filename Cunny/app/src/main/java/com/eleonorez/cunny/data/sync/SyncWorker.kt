package com.eleonorez.cunny.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.eleonorez.cunny.data.retrofit.ApiConfig

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"

        const val KEY_SYNC_TYPE = "sync_type"
        const val KEY_LESSON_SLUG = "lesson_slug"
        const val KEY_SCORE = "score"
        const val KEY_XP = "xp"
        const val KEY_LEVEL = "level"
        const val KEY_STREAK = "streak"
        const val KEY_ENERGY = "energy"
        const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        const val KEY_BADGES = "badges"

        const val SYNC_TYPE_PROGRESS = "progress"
        const val SYNC_TYPE_GAMIFICATION = "gamification"
        const val SYNC_TYPE_BADGES = "badges"

        const val MAX_RETRIES = 3
    }

    override suspend fun doWork(): Result {
        val syncType = inputData.getString(KEY_SYNC_TYPE) ?: return Result.failure()

        if (runAttemptCount >= MAX_RETRIES) {
            Log.e(TAG, "❌ Max retries ($MAX_RETRIES) reached for syncType=$syncType, giving up")
            return Result.failure()
        }

        Log.d(TAG, "▶ SyncWorker attempt ${runAttemptCount + 1}/$MAX_RETRIES for type=$syncType")

        return try {
            val apiService = ApiConfig.getApiService()

            when (syncType) {
                SYNC_TYPE_PROGRESS -> {
                    val slug = inputData.getString(KEY_LESSON_SLUG) ?: return Result.failure()
                    val score = inputData.getInt(KEY_SCORE, 0)
                    apiService.syncProgress(mapOf("lesson_slug" to slug, "score" to score))
                    Log.d(TAG, "✅ syncProgress retry OK: slug=$slug")
                }

                SYNC_TYPE_GAMIFICATION -> {
                    val payload = mapOf<String, Any>(
                        "xp" to inputData.getInt(KEY_XP, 0),
                        "level" to inputData.getInt(KEY_LEVEL, 1),
                        "streak" to inputData.getInt(KEY_STREAK, 0),
                        "energy" to inputData.getInt(KEY_ENERGY, 5),
                        "last_active_date" to (inputData.getString(KEY_LAST_ACTIVE_DATE) ?: "")
                    )
                    apiService.syncGamification(payload)
                    Log.d(TAG, "✅ syncGamification retry OK")
                }

                SYNC_TYPE_BADGES -> {
                    val badges = inputData.getStringArray(KEY_BADGES)?.toList()
                        ?: return Result.failure()
                    apiService.syncBadges(mapOf("badges" to badges))
                    Log.d(TAG, "✅ syncBadges retry OK: badges=$badges")
                }

                else -> {
                    Log.e(TAG, "❌ Unknown syncType: $syncType")
                    return Result.failure()
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ SyncWorker attempt ${runAttemptCount + 1} failed: ${e.message}", e)
            Result.retry()
        }
    }
}
