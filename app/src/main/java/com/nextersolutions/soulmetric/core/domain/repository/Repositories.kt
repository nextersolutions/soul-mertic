package com.nextersolutions.soulmetric.core.domain.repository

import com.nextersolutions.soulmetric.core.domain.model.Survey
import com.nextersolutions.soulmetric.core.domain.model.SurveyResult
import kotlinx.coroutines.flow.Flow

interface SurveyRepository {
    fun getSurveys(): Flow<List<Survey>>
    suspend fun getSurveyById(id: String): Survey?
    suspend fun refreshSurveysFromNetwork(): Result<Unit>
}

interface SurveyResultRepository {
    fun getAllResults(): Flow<List<SurveyResult>>
    fun getResultsForSurvey(surveyId: String): Flow<List<SurveyResult>>
    suspend fun saveResult(result: SurveyResult): Long
    suspend fun deleteResult(resultId: Long)
}
