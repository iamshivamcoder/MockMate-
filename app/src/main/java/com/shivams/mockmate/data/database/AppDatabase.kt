package com.shivams.mockmate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.shivams.mockmate.model.UserProfile

@Database(
    entities = [
        QuestionEntity::class,
        TestEntity::class,
        TestAttemptEntity::class,
        UserAnswerEntity::class,
        UserStatsEntity::class,
        TestQuestionCrossRef::class,
        UserProfile::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun questionDao(): QuestionDao
    abstract fun testDao(): TestDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mockmate_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}