package com.nextersolutions.soulmetric.feature.survey.presentation.viewmodel

import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.Question
import com.nextersolutions.soulmetric.core.domain.model.Survey

// A step is either a full-screen section splash or a question.
sealed class SurveyStep {
    data class SectionSplash(val sectionId: String, val title: String) : SurveyStep()
    data class QuestionStep(val question: Question, val questionNumber: Int) : SurveyStep()
}

data class SurveyState(
    val survey: Survey? = null,
    val steps: List<SurveyStep> = emptyList(),
    val currentStepIndex: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val isCompleted: Boolean = false,
    val resultId: Long? = null
) {
    val currentStep: SurveyStep? get() = steps.getOrNull(currentStepIndex)

    // Question-scoped helpers (only valid when currentStep is QuestionStep)
    val currentQuestion: Question? get() = (currentStep as? SurveyStep.QuestionStep)?.question
    val currentQuestionNumber: Int get() = (currentStep as? SurveyStep.QuestionStep)?.questionNumber ?: 0
    val totalQuestions: Int get() = steps.filterIsInstance<SurveyStep.QuestionStep>().size
    val progress: Float get() = if (totalQuestions == 0) 0f else currentQuestionNumber.toFloat() / totalQuestions

    val isLastStep: Boolean get() = currentStepIndex == steps.lastIndex

    // "Next" is enabled when on a section splash (always) or when the question is answered / optional
    val canGoNext: Boolean get() = when (val step = currentStep) {
        is SurveyStep.SectionSplash -> true
        is SurveyStep.QuestionStep -> if (!step.question.required) true
            else answers.containsKey(step.question.id)
        null -> false
    }

    // Back skips over section splashes — returns true if there's a QuestionStep before us
    val canGoBack: Boolean get() = steps.take(currentStepIndex).any { it is SurveyStep.QuestionStep }
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
