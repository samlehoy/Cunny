package com.eleonorez.cunny.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GamificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: UserProgressEntity)

    @Query("SELECT * FROM UserProgressEntity WHERE id = 1")
    suspend fun getProgress(): UserProgressEntity?

    @Query("SELECT * FROM UserProgressEntity WHERE id = 1")
    fun getProgressFlow(): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLessonCompletion(completion: LessonCompletionEntity)

    @Query("SELECT * FROM LessonCompletionEntity ORDER BY completedAt DESC")
    suspend fun getAllCompletions(): List<LessonCompletionEntity>

    @Query("SELECT * FROM LessonCompletionEntity ORDER BY completedAt DESC")
    fun getAllCompletionsFlow(): Flow<List<LessonCompletionEntity>>

    @Query("SELECT * FROM LessonCompletionEntity WHERE lessonSlug = :slug")
    suspend fun getCompletion(slug: String): LessonCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Query("SELECT * FROM BadgeEntity ORDER BY unlockedAt DESC")
    suspend fun getAllBadges(): List<BadgeEntity>

    @Query("SELECT * FROM BadgeEntity ORDER BY unlockedAt DESC")
    fun getAllBadgesFlow(): Flow<List<BadgeEntity>>

    @Query("SELECT * FROM BadgeEntity WHERE badgeId = :badgeId")
    suspend fun getBadge(badgeId: String): BadgeEntity?

    @Query("DELETE FROM UserProgressEntity")
    suspend fun clearProgress()

    @Query("DELETE FROM LessonCompletionEntity")
    suspend fun clearCompletions()

    @Query("DELETE FROM BadgeEntity")
    suspend fun clearBadges()

    @androidx.room.Transaction
    suspend fun clearAllGamificationData() {
        clearProgress()
        clearCompletions()
        clearBadges()
    }
}


