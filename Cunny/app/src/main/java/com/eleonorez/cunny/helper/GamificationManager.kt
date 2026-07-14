package com.eleonorez.cunny.helper

import android.content.Context
import com.eleonorez.cunny.data.database.BadgeEntity
import com.eleonorez.cunny.data.database.BookmarkRoomDatabase
import com.eleonorez.cunny.data.database.LessonCompletionEntity
import com.eleonorez.cunny.data.database.UserProgressEntity
import com.eleonorez.cunny.data.retrofit.ApiConfig
import com.eleonorez.cunny.data.sync.SyncManager
import com.eleonorez.cunny.data.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GamificationManager(context: Context) {
    private val db = BookmarkRoomDatabase.getDatabase(context)
    private val dao = db.gamificationDao()
    private val apiService = ApiConfig.getApiService()
    private val syncManager = SyncManager(context)

    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val xpMutex = Mutex()

    suspend fun addXp(amount: Int, syncToServer: Boolean = true) {
        xpMutex.withLock {
            val current = dao.getProgress()
            val updated = if (current == null) {
                val level = 1 + (amount / 50)
                UserProgressEntity(id = 1, xp = amount, level = level)
            } else {
                val newXp = current.xp + amount
                val newLevel = 1 + (newXp / 50)
                current.copy(xp = newXp, level = newLevel)
            }
            dao.upsertProgress(updated)
            if (syncToServer) {
                syncGamificationToServer(updated)
            }
        }
    }

    suspend fun completeLesson(slug: String, score: Int) {
        // 1. Local lesson completion write
        val existing = dao.getCompletion(slug)
        val bestScore = maxOf(existing?.bestScore ?: 0, score)
        dao.upsertLessonCompletion(
            LessonCompletionEntity(lessonSlug = slug, bestScore = bestScore)
        )

        // 2. Local gamification writes (skip individual server syncs)
        addXp(10, syncToServer = false)
        updateStreak(syncToServer = false)

        // 3. Single batched sync to server (each call isolated)
        syncScope.launch {
            val api = ApiConfig.getApiService()
            android.util.Log.d("GamificationSync", "▶ completeLesson sync START for slug=$slug score=$bestScore")

            // 3a. Sync lesson progress
            try {
                val syncResult = api.syncProgress(mapOf("lesson_slug" to slug, "score" to bestScore))
                android.util.Log.d("GamificationSync", "✅ syncProgress OK: error=${syncResult.error}, slug=${syncResult.progress?.lessonSlug}")
            } catch (e: Exception) {
                android.util.Log.e("GamificationSync", "❌ syncProgress FAILED, enqueueing retry: ${e.javaClass.simpleName}: ${e.message}", e)
                syncManager.enqueueSyncWork(
                    SyncWorker.SYNC_TYPE_PROGRESS,
                    mapOf(
                        SyncWorker.KEY_LESSON_SLUG to slug,
                        SyncWorker.KEY_SCORE to bestScore
                    )
                )
            }

            // 3b. Sync gamification metrics (runs even if 3a failed)
            try {
                val progress = dao.getProgress()
                android.util.Log.d("GamificationSync", "📊 Local progress: xp=${progress?.xp} level=${progress?.level} streak=${progress?.streak} energy=${progress?.energy}")
                if (progress != null) {
                    val gamResult = api.syncGamification(
                        mapOf(
                            "xp" to progress.xp,
                            "level" to progress.level,
                            "streak" to progress.streak,
                            "energy" to progress.energy,
                            "last_active_date" to progress.lastActiveDate
                        )
                    )
                    android.util.Log.d("GamificationSync", "✅ syncGamification OK: error=${gamResult.error}, xp=${gamResult.user?.xp}")
                }
            } catch (e: Exception) {
                android.util.Log.e("GamificationSync", "❌ syncGamification FAILED, enqueueing retry: ${e.javaClass.simpleName}: ${e.message}", e)
                val progress = dao.getProgress()
                if (progress != null) {
                    syncManager.enqueueSyncWork(
                        SyncWorker.SYNC_TYPE_GAMIFICATION,
                        mapOf(
                            SyncWorker.KEY_XP to progress.xp,
                            SyncWorker.KEY_LEVEL to progress.level,
                            SyncWorker.KEY_STREAK to progress.streak,
                            SyncWorker.KEY_ENERGY to progress.energy,
                            SyncWorker.KEY_LAST_ACTIVE_DATE to progress.lastActiveDate
                        )
                    )
                }
            }

            android.util.Log.d("GamificationSync", "▶ completeLesson sync DONE for slug=$slug")
        }
    }

    suspend fun updateStreak(syncToServer: Boolean = true) {
        val current = dao.getProgress()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = fmt.format(Date())
        val updated = if (current == null) {
            UserProgressEntity(id = 1, streak = 1, lastActiveDate = today)
        } else {
            val last = current.lastActiveDate
            if (last.isEmpty()) {
                current.copy(streak = 1, lastActiveDate = today)
            } else {
                val yesterday = fmt.format(Date(System.currentTimeMillis() - 86400000L))
                val newStreak = when (last) {
                    today -> current.streak
                    yesterday -> current.streak + 1
                    else -> 1
                }
                current.copy(streak = newStreak, lastActiveDate = today)
            }
        }
        dao.upsertProgress(updated)
        if (syncToServer) {
            syncGamificationToServer(updated)
        }
    }

    suspend fun checkAndRefillEnergy(currentTime: Long = System.currentTimeMillis()) {
        val current = dao.getProgress() ?: return
        if (current.energy >= 5) {
            if (current.lastRefillTime != 0L) {
                val updated = current.copy(lastRefillTime = 0L)
                dao.upsertProgress(updated)
                syncGamificationToServer(updated)
            }
            return
        }

        val lastRefill = current.lastRefillTime
        if (lastRefill == 0L) {
            val updated = current.copy(lastRefillTime = currentTime)
            dao.upsertProgress(updated)
            syncGamificationToServer(updated)
            return
        }

        val elapsedMs = currentTime - lastRefill
        val intervalMs = 30 * 60 * 1000L // 30 minutes
        val intervalsPassed = (elapsedMs / intervalMs).toInt()

        if (intervalsPassed > 0) {
            val newEnergy = minOf(5, current.energy + intervalsPassed)
            val newRefillTime = if (newEnergy >= 5) 0L else lastRefill + (intervalsPassed * intervalMs)
            val updated = current.copy(energy = newEnergy, lastRefillTime = newRefillTime)
            dao.upsertProgress(updated)
            syncGamificationToServer(updated)
            android.util.Log.d("GamificationSync", "⚡ Refilled energy: ${current.energy} -> $newEnergy, next refill time: $newRefillTime")
        }
    }

    suspend fun checkDailyEnergyRefill() {
        val current = dao.getProgress() ?: return
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = fmt.format(Date())
        if (current.lastActiveDate != today) {
            val updated = if (current.energy < 5) {
                current.copy(energy = 5, lastRefillTime = 0L, lastActiveDate = today)
            } else {
                current.copy(lastActiveDate = today)
            }
            dao.upsertProgress(updated)
            syncGamificationToServer(updated)
            android.util.Log.d("GamificationSync", "☀️ Daily reset: energy update. energy=${updated.energy}")
        }
    }

    suspend fun consumeEnergy(amount: Int = 1): Boolean {
        val current = dao.getProgress()
        val currentTime = System.currentTimeMillis()
        if (current == null) {
            val updated = UserProgressEntity(
                id = 1,
                energy = maxOf(0, 5 - amount),
                lastRefillTime = if (5 - amount < 5) currentTime else 0L
            )
            dao.upsertProgress(updated)
            syncGamificationToServer(updated)
            return true
        }

        if (current.energy < amount) {
            android.util.Log.w("GamificationSync", "❌ consumeEnergy failed: insufficient energy (${current.energy} < $amount)")
            return false
        }

        val newEnergy = current.energy - amount
        val newRefillTime = if (newEnergy < 5 && current.lastRefillTime == 0L) currentTime else current.lastRefillTime
        val updated = current.copy(energy = newEnergy, lastRefillTime = newRefillTime)
        dao.upsertProgress(updated)
        syncGamificationToServer(updated)
        android.util.Log.d("GamificationSync", "⚡ consumeEnergy success: energy ${current.energy} -> $newEnergy")
        return true
    }

    suspend fun checkBadgeUnlock(badgeId: String) {
        val existing = dao.getBadge(badgeId)
        if (existing == null) {
            val badge = BadgeEntity(badgeId = badgeId)
            dao.insertBadge(badge)
            if (badgeId == "first-scan") {
                addXp(15)
            }
            // SYNC-04: Push badge to server
            syncScope.launch {
                try {
                    apiService.syncBadges(mapOf("badges" to listOf(badgeId)))
                    android.util.Log.d("GamificationSync", "✅ syncBadges OK: badgeId=$badgeId")
                } catch (e: Exception) {
                    android.util.Log.e("GamificationSync", "❌ syncBadges FAILED, enqueueing retry: ${e.message}", e)
                    syncManager.enqueueSyncWork(
                        SyncWorker.SYNC_TYPE_BADGES,
                        mapOf(SyncWorker.KEY_BADGES to listOf(badgeId))
                    )
                }
            }
        }
    }

    fun syncGamificationToServer(progress: UserProgressEntity) {
        syncScope.launch {
            try {
                val payload = mapOf<String, Any>(
                    "xp" to progress.xp,
                    "level" to progress.level,
                    "streak" to progress.streak,
                    "energy" to progress.energy,
                    "last_active_date" to progress.lastActiveDate
                )
                apiService.syncGamification(payload)
            } catch (e: Exception) {
                android.util.Log.e("GamificationSync", "❌ syncGamification failed, enqueueing retry: ${e.message}", e)
                syncManager.enqueueSyncWork(
                    SyncWorker.SYNC_TYPE_GAMIFICATION,
                    mapOf(
                        SyncWorker.KEY_XP to progress.xp,
                        SyncWorker.KEY_LEVEL to progress.level,
                        SyncWorker.KEY_STREAK to progress.streak,
                        SyncWorker.KEY_ENERGY to progress.energy,
                        SyncWorker.KEY_LAST_ACTIVE_DATE to progress.lastActiveDate
                    )
                )
            }
        }
    }
}
