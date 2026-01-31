package com.shivams.mockmate.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.shivams.mockmate.data.remote.AnalysisApi
import com.shivams.mockmate.data.remote.ApiConstants
import com.shivams.mockmate.data.repositories.PdfAnalysisRepository
import com.shivams.mockmate.data.repositories.RealPdfAnalysisRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies for PDF Analysis.
 */
@Module
@InstallIn(SingletonComponent::class)
object AnalysisNetworkModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAnalysisApi(retrofit: Retrofit): AnalysisApi {
        return retrofit.create(AnalysisApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAnalysisDao(database: com.shivams.mockmate.data.database.AppDatabase): com.shivams.mockmate.data.database.AnalysisDao {
        return database.analysisDao()
    }
}

/**
 * Binds the repository implementation to the interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AnalysisRepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindPdfAnalysisRepository(
        realPdfAnalysisRepository: RealPdfAnalysisRepository
    ): PdfAnalysisRepository
}
