package com.nextersolutions.soulmetric.feature.survey.presentation.viewmodel

import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.Survey

data class SurveyState(
    val survey: Survey? = null,
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val resultId: Long? = null
) {
    val currentQuestion get() = survey?.questions?.getOrNull(currentQuestionIndex)
    val totalQuestions get() = survey?.questions?.size ?: 0
    val progress get() = if (totalQuestions == 0) 0f else (currentQuestionIndex + 1f) / totalQuestions
    val canGoBack get() = currentQuestionIndex > 0
    val canGoNext get() = currentQuestion?.let { q ->
        if (!q.required) true
        else answers.containsKey(q.id)
    } ?: false
    val isLastQuestion get() = currentQuestionIndex == totalQuestions - 1
}

sealed class SurveyIntent {
    data object LoadSurvey : SurveyIntent()
    data class AnswerScale(val questionId: String, val value: Int) : SurveyIntent()
    data class AnswerChoice(val questionId: String, val optionId: String, val optionText: String) : SurveyIntent()
    data class AnswerText(val questionId: String, val text: String) : SurveyIntent()
    data object NextQuestion : SurveyIntent()
    data object PreviousQuestion : SurveyIntent()
    data object Submit : SurveyIntent()
}

sealed class SurveyEffect {
    data object NavigateBack : SurveyEffect()
    data class NavigateToResult(val resultId: Long) : SurveyEffect()
    data class ShowError(val message: String) : SurveyEffect()
}
