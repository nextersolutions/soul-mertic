package com.nextersolutions.soulmetric.core.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.nextersolutions.soulmetric.BuildConfig
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyAnswerDao
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyCacheDao
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyResultDao
import com.nextersolutions.soulmetric.core.data.local.database.SoulMetricDatabase
import com.nextersolutions.soulmetric.core.data.remote.api.SurveyApiService
import com.nextersolutions.soulmetric.core.data.repository.SurveyRepositoryImpl
import com.nextersolutions.soulmetric.core.data.repository.SurveyResultRepositoryImpl
import com.nextersolutions.soulmetric.core.domain.repository.SurveyRepository
import com.nextersolutions.soulmetric.core.domain.repository.SurveyResultRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideHttpClient(json: Json): HttpClient = HttpClient(Android) {
        install(ContentNegotiation) { json(json) }
        install(Logging) {
            level = if (BuildConfig.DEBUG) LogLevel.BODY else LogLevel.NONE
            logger = object : Logger {
                override fun log(message: String) {
                    android.util.Log.d("Ktor", message)
                }
            }
        }
        defaultRequest { url(BuildConfig.SURVEYS_BASE_URL) }
    }

    @Provides
    @Singleton
    fun provideSurveyApiService(client: HttpClient) = SurveyApiService(client)
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SoulMetricDatabase =
        Room.databaseBuilder(context, SoulMetricDatabase::class.java, "soulmetric.db").build()

    @Provides
    fun provideSurveyCacheDao(db: SoulMetricDatabase): SurveyCacheDao = db.surveyCacheDao()
    @Provides
    fun provideSurveyResultDao(db: SoulMetricDatabase): SurveyResultDao = db.surveyResultDao()
    @Provides
    fun provideSurveyAnswerDao(db: SoulMetricDatabase): SurveyAnswerDao = db.surveyAnswerDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSurveyRepository(impl: SurveyRepositoryImpl): SurveyRepository

    @Binds
    @Singleton
    abstract fun bindSurveyResultRepository(impl: SurveyResultRepositoryImpl): SurveyResultRepository
}

@Module
@InstallIn(SingletonComponent::class)
object WorkModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
