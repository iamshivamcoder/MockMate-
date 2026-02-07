package com.shivams.mockmate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shivams.mockmate.BuildConfig
import com.shivams.mockmate.model.UserProfile

@Database(
    entities = [
        QuestionEntity::class,
        TestEntity::class,
        TestAttemptEntity::class,
        UserAnswerEntity::class,
        UserStatsEntity::class,
        TestQuestionCrossRef::class,
        UserProfile::class,
        ChatMessageEntity::class,
        ChatSessionEntity::class,
        TrueFalseSessionEntity::class,
        TrueFalseStatementEntity::class,
        TrueFalseAnswerEntity::class,
        AnalysisEntity::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun questionDao(): QuestionDao
    abstract fun testDao(): TestDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun chatDao(): ChatDao
    abstract fun trueFalseDao(): TrueFalseDao
    abstract fun analysisDao(): AnalysisDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mockmate_database"
                )
                
                // Only use destructive migration in debug builds
                // In production, proper migrations should be added
                if (BuildConfig.DEBUG) {
                    builder.fallbackToDestructiveMigration(true)
                }
                // TODO: Add proper migrations for production releases
                // .addMigrations(MIGRATION_7_8, MIGRATION_8_9, ...)
                
                val instance = builder.build()
                INSTANCE = instance
                instance
            }
        }
    }
}
