package com.eleonorez.cunny.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val streak: Int = 0,
    val lastActiveDate: String = "",
    val energy: Int = 5,
    val lastRefillTime: Long = 0L
)

@Entity
data class LessonCompletionEntity(
    @PrimaryKey
    val lessonSlug: String,
    val completedAt: Long = System.currentTimeMillis(),
    val bestScore: Int = 0
)

@Entity
data class BadgeEntity(
    @PrimaryKey
    val badgeId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)


