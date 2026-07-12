package com.eleonorez.cunny.data.repository

import android.content.Context
import com.eleonorez.cunny.data.database.BadgeEntity
import com.eleonorez.cunny.data.database.GamificationDao
import com.eleonorez.cunny.data.database.LessonCompletionEntity
import com.eleonorez.cunny.data.database.UserProgressEntity
import com.eleonorez.cunny.helper.GamificationManager
import kotlinx.coroutines.flow.Flow

class ProgressRepository(private val dao: GamificationDao, private val context: Context) {

    val userProgress: Flow<UserProgressEntity?> = dao.getProgressFlow()
    val lessonCompletions: Flow<List<LessonCompletionEntity>> = dao.getAllCompletionsFlow()
    val unlockedBadges: Flow<List<BadgeEntity>> = dao.getAllBadgesFlow()

    suspend fun getProgress(): UserProgressEntity? = dao.getProgress()
    suspend fun getCompletion(slug: String): LessonCompletionEntity? = dao.getCompletion(slug)

    suspend fun upsertProgress(progress: UserProgressEntity) {
        dao.upsertProgress(progress)
    }

    suspend fun upsertLessonCompletion(completion: LessonCompletionEntity) {
        dao.upsertLessonCompletion(completion)
    }

    suspend fun insertBadge(badge: BadgeEntity) {
        dao.insertBadge(badge)
    }

    suspend fun clearAllLocalData() {
        dao.clearAllGamificationData()
    }

    suspend fun checkEnergyRefill() {
        val manager = GamificationManager(context)
        manager.checkDailyEnergyRefill()
        manager.checkAndRefillEnergy()
    }

    companion object {
        @Volatile
        private var instance: ProgressRepository? = null
        fun getInstance(dao: GamificationDao, context: Context): ProgressRepository =
            instance ?: synchronized(this) {
                instance ?: ProgressRepository(dao, context.applicationContext)
            }.also { instance = it }
    }
}


