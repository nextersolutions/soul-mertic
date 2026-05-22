package com.nextersolutions.soulmetric.core.data.repository

import com.nextersolutions.soulmetric.core.data.local.dao.SurveyAnswerDao
import com.nextersolutions.soulmetric.core.data.local.dao.SurveyResultDao
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyAnswerEntity
import com.nextersolutions.soulmetric.core.data.local.entity.SurveyResultEntity
import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.SurveyResult
import com.nextersolutions.soulmetric.core.domain.repository.SurveyResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class SurveyResultRepositoryImpl @Inject constructor(
    private val resultDao: SurveyResultDao,
    private val answerDao: SurveyAnswerDao
) : SurveyResultRepository {

    override fun getAllResults(): Flow<List<SurveyResult>> =
        resultDao.observeAll().map { it.map { entity -> entity.toDomain() } }

    override fun getResultsForSurvey(surveyId: String): Flow<List<SurveyResult>> =
        resultDao.observeForSurvey(surveyId).map { it.map { entity -> entity.toDomain() } }

    override suspend fun saveResult(result: SurveyResult): Long {
        val entity = SurveyResultEntity(
            surveyId = result.surveyId,
            surveyTitle = result.surveyTitle,
            completedAt = result.completedAt.toEpochMilli(),
            score = result.score,
            scoreDescription = result.scoreDescription
        )
        val insertedId = resultDao.insert(entity)
        val answers = result.answers.map { it.toEntity(insertedId) }
        answerDao.insertAll(answers)
        return insertedId
    }

    override suspend fun deleteResult(resultId: Long) {
        resultDao.delete(resultId)
    }

    private fun SurveyResultEntity.toDomain() = SurveyResult(
        id = id,
        surveyId = surveyId,
        surveyTitle = surveyTitle,
        completedAt = Instant.ofEpochMilli(completedAt),
        answers = emptyList(), // answers loaded on demand
        score = score,
        scoreDescription = scoreDescription
    )

    private fun Answer.toEntity(resultId: Long) = when (this) {
        is Answer.ScaleAnswer -> SurveyAnswerEntity(resultId = resultId, questionId = questionId, type = "scale", intValue = value, stringValue = null)
        is Answer.ChoiceAnswer -> SurveyAnswerEntity(resultId = resultId, questionId = questionId, type = "choice", intValue = null, stringValue = "$selectedOptionId|$selectedOptionText")
        is Answer.TextAnswer -> SurveyAnswerEntity(resultId = resultId, questionId = questionId, type = "text", intValue = null, stringValue = value)
    }
}
