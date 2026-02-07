package com.shivams.mockmate.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shivams.mockmate.ApiConfig
import com.shivams.mockmate.api.GeminiApiService
import com.shivams.mockmate.data.database.AppDatabase
import com.shivams.mockmate.data.database.ChatDao
import com.shivams.mockmate.data.database.TrueFalseDao
import com.shivams.mockmate.data.database.UserProfileDao
import com.shivams.mockmate.data.repositories.MentorChatRepository
import com.shivams.mockmate.data.repositories.SettingsRepository
import com.shivams.mockmate.data.repositories.TestRepository
import com.shivams.mockmate.data.repositories.TestRepositoryImpl
import com.shivams.mockmate.data.repositories.UserProfileRepository
import com.shivams.mockmate.domain.usecases.TestAttemptOperationsUseCase
import com.shivams.mockmate.service.AIQuestionGenerator
import com.shivams.mockmate.service.AiInsightsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Note: Gson is provided by AnalysisNetworkModule

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
    fun provideUserProfileDao(appDatabase: AppDatabase): UserProfileDao {
        return appDatabase.userProfileDao()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideTestAttemptOperationsUseCase(repository: TestRepository): TestAttemptOperationsUseCase {
        return TestAttemptOperationsUseCase(repository)
    }

    // New providers for ViewModels
    
    @Provides
    @Singleton
    fun provideAiInsightsService(
        apiConfig: ApiConfig,
        testRepository: TestRepository
    ): AiInsightsService {
        return AiInsightsService(apiConfig, testRepository)
    }

    @Provides
    @Singleton
    fun provideChatDao(appDatabase: AppDatabase): ChatDao {
        return appDatabase.chatDao()
    }

    @Provides
    @Singleton
    fun provideTrueFalseDao(appDatabase: AppDatabase): TrueFalseDao {
        return appDatabase.trueFalseDao()
    }

    @Provides
    @Singleton
    fun provideMentorChatRepository(chatDao: ChatDao): MentorChatRepository {
        return MentorChatRepository(chatDao)
    }
    
    // Note: PdfAnalysisRepository is now provided by AnalysisNetworkModule
}

