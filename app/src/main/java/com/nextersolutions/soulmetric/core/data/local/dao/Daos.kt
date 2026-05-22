package com.nextersolutions.soulmetric.core.data.local.dao

import androidx.room.*
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyAnswerEntity
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyCacheEntity
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyCacheDao {
    @Query("SELECT * FROM survey_cache")
    fun observeAll(): Flow<List<SurveyCacheEntity>>

    @Query("SELECT * FROM survey_cache WHERE id = :id")
    suspend fun getById(id: String): SurveyCacheEntity?

    @Query("SELECT COUNT(*) FROM survey_cache")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(surveys: List<SurveyCacheEntity>)

    @Query("DELETE FROM survey_cache")
    suspend fun clearAll()
}

@Dao
interface SurveyResultDao {
    @Query("SELECT * FROM survey_results ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<SurveyResultEntity>>

    @Query("SELECT * FROM survey_results WHERE surveyId = :surveyId ORDER BY completedAt DESC")
    fun observeForSurvey(surveyId: String): Flow<List<SurveyResultEntity>>

    @Insert
    suspend fun insert(result: SurveyResultEntity): Long

    @Query("DELETE FROM survey_results WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface SurveyAnswerDao {
    @Query("SELECT * FROM survey_answers WHERE resultId = :resultId")
    suspend fun getAnswersForResult(resultId: Long): List<SurveyAnswerEntity>

    @Insert
    suspend fun insertAll(answers: List<SurveyAnswerEntity>)
}
