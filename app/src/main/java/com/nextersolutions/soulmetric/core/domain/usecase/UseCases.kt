package com.nextersolutions.soulmetric.core.domain.usecase

import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.Survey
import com.nextersolutions.soulmetric.core.domain.model.SurveyResult
import com.nextersolutions.soulmetric.core.domain.repository.SurveyRepository
import com.nextersolutions.soulmetric.core.domain.repository.SurveyResultRepository
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class GetSurveysUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    operator fun invoke(): Flow<List<Survey>> = repository.getSurveys()
}

class GetSurveyByIdUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(id: String): Survey? = repository.getSurveyById(id)
}

class RefreshSurveysUseCase @Inject constructor(
    private val repository: SurveyRepository
) {
    suspend operator fun invoke(): Result<Unit> = repository.refreshSurveysFromNetwork()
}

class SubmitSurveyUseCase @Inject constructor(
    private val resultRepository: SurveyResultRepository
) {
    suspend operator fun invoke(
        survey: Survey,
        answers: Map<String, Answer>
    ): Long {
        val score = calculateScore(survey, answers)
        val scoreDescription = survey.scoring?.ranges
            ?.firstOrNull { score != null && score in it.min..it.max }
            ?.description

        val result = SurveyResult(
            surveyId = survey.id,
            surveyTitle = survey.title,
            completedAt = Instant.now(),
            answers = answers.values.toList(),
            score = score,
            scoreDescription = scoreDescription
        )
        return resultRepository.saveResult(result)
    }

    private fun calculateScore(survey: Survey, answers: Map<String, Answer>): Int? {
        if (survey.scoring?.method != "sum") return null
        return answers.values.sumOf { answer ->
            when (answer) {
                is Answer.ScaleAnswer -> answer.value
                else -> 0
            }
        }
    }
}

class GetSurveyResultsUseCase @Inject constructor(
    private val repository: SurveyResultRepository
) {
    operator fun invoke(): Flow<List<SurveyResult>> = repository.getAllResults()
}

class DeleteSurveyResultUseCase @Inject constructor(
    private val repository: SurveyResultRepository
) {
    suspend operator fun invoke(resultId: Long) = repository.deleteResult(resultId)
}
