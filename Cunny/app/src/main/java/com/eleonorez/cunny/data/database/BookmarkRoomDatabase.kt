package com.eleonorez.cunny.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [UserProgressEntity::class, LessonCompletionEntity::class, BadgeEntity::class],
    version = 5
)
abstract class BookmarkRoomDatabase : RoomDatabase() {

    abstract fun gamificationDao(): GamificationDao

    companion object {
        @Volatile
        private var INSTANCE: BookmarkRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): BookmarkRoomDatabase {
            if (INSTANCE == null) {
                synchronized(BookmarkRoomDatabase::class.java) {
                    val MIGRATION_1_2 = Migration(1, 2) { db ->
                        db.execSQL("CREATE TABLE IF NOT EXISTS UserProgressEntity (id INTEGER PRIMARY KEY NOT NULL, xp INTEGER NOT NULL DEFAULT 0, level INTEGER NOT NULL DEFAULT 1, streak INTEGER NOT NULL DEFAULT 0, lastActiveDate TEXT NOT NULL DEFAULT '')")
                        db.execSQL("CREATE TABLE IF NOT EXISTS LessonCompletionEntity (lessonSlug TEXT PRIMARY KEY NOT NULL, completedAt INTEGER NOT NULL DEFAULT 0, bestScore INTEGER NOT NULL DEFAULT 0)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS BadgeEntity (badgeId TEXT PRIMARY KEY NOT NULL, unlockedAt INTEGER NOT NULL DEFAULT 0)")
                    }
                    val MIGRATION_2_3 = Migration(2, 3) { db ->
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN energy INTEGER NOT NULL DEFAULT 5")
                    }
                    val MIGRATION_3_4 = Migration(3, 4) { db ->
                        db.execSQL("ALTER TABLE UserProgressEntity ADD COLUMN lastRefillTime INTEGER NOT NULL DEFAULT 0")
                    }
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        BookmarkRoomDatabase::class.java, "bookmark_database"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .fallbackToDestructiveMigration()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(db: SupportSQLiteDatabase) {
                                super.onCreate(db)
                            }
                        })
                        .build()
                }
            }
            return INSTANCE as BookmarkRoomDatabase
        }
    }
}

