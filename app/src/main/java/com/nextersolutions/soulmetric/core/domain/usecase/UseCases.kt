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
        val scoring = survey.scoring ?: return null
        if (scoring.method != "sum") return null

        // Build a lookup: questionId → numeric option values, for choice questions
        val choiceValueMap: Map<String, Map<String, Int>> = survey.questions
            .filterIsInstance<com.nextersolutions.soulmetric.core.domain.model.Question.Choice>()
            .associate { q -> q.id to q.options.associate { it.id to it.value } }

        val targetIds = scoring.scoredQuestions.toSet()

        return answers.values
            .filter { targetIds.isEmpty() || it.questionId in targetIds }
            .sumOf { answer ->
                when (answer) {
                    is Answer.ScaleAnswer -> answer.value
                    is Answer.ChoiceAnswer ->
                        choiceValueMap[answer.questionId]?.get(answer.selectedOptionId) ?: 0
                    is Answer.TextAnswer -> 0
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
