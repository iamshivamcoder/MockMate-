package com.shivams.mockmate.di

import android.content.Context
import com.shivams.mockmate.ApiConfig
import com.shivams.mockmate.api.GeminiApiService
import com.shivams.mockmate.data.database.UserProfileDao
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.data.repositories.TestRepositoryImpl
import com.shivams.mockmate.data.repositories.UserProfileRepository
import com.shivams.mockmate.domain.usecases.TestAttemptOperationsUseCase
import com.shivams.mockmate.service.AIQuestionGenerator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTestRepository(@ApplicationContext context: Context): TestRepository {
        return TestRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideApiConfig(@ApplicationContext context: Context): ApiConfig {
        return ApiConfig(context)
    }

    @Provides
    @Singleton
    fun provideAiQuestionGenerator(geminiApiService: GeminiApiService): AIQuestionGenerator {
        return AIQuestionGenerator(geminiApiService)
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(apiConfig: ApiConfig): GeminiApiService {
        return apiConfig.geminiApiService
    }

    @Provides
    @Singleton
    fun provideUserProfileRepository(userProfileDao: UserProfileDao): UserProfileRepository {
        return UserProfileRepository(userProfileDao)
    }

    @Provides
    @Singleton
    fun provideUserProfileDao(appDatabase: com.shivams.mockmate.data.database.AppDatabase): UserProfileDao {
        return appDatabase.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): com.shivams.mockmate.data.database.AppDatabase {
        return com.shivams.mockmate.data.database.AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTestAttemptOperationsUseCase(repository: TestRepository): TestAttemptOperationsUseCase {
        return TestAttemptOperationsUseCase(repository)
    }
}
