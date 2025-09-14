package com.shivams.mockmate.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shivams.mockmate.data.database.converters.DateConverter
import com.shivams.mockmate.data.database.daos.QuestionDao
import com.shivams.mockmate.data.database.daos.TestAttemptDao
import com.shivams.mockmate.data.database.daos.TestDao
import com.shivams.mockmate.data.database.daos.UserStatsDao
import com.shivams.mockmate.data.database.entities.QuestionEntity
import com.shivams.mockmate.data.database.entities.TestAttemptEntity
import com.shivams.mockmate.data.database.entities.TestEntity
import com.shivams.mockmate.data.database.entities.TestQuestionCrossRef
import com.shivams.mockmate.data.database.entities.UserAnswerEntity
import com.shivams.mockmate.data.database.entities.UserStatsEntity

@Database(
    entities = [
        QuestionEntity::class,
        TestEntity::class,
        TestAttemptEntity::class,
        UserAnswerEntity::class,
        UserStatsEntity::class,
        TestQuestionCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun questionDao(): QuestionDao
    abstract fun testDao(): TestDao
    abstract fun testAttemptDao(): TestAttemptDao
    abstract fun userStatsDao(): UserStatsDao
    
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