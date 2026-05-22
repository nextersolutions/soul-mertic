package com.nextersolutions.soulmetric.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyAnswerDao
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyCacheDao
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyResultDao
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyAnswerEntity
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyCacheEntity
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyResultEntity

@Database(
    entities = [SurveyCacheEntity::class, SurveyResultEntity::class, SurveyAnswerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SoulMetricDatabase : RoomDatabase() {
    abstract fun surveyCacheDao(): SurveyCacheDao
    abstract fun surveyResultDao(): SurveyResultDao
    abstract fun surveyAnswerDao(): SurveyAnswerDao
}
