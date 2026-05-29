package com.nextersolutions.soulmetric.feature.survey.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextersolutions.soulmetric.core.domain.model.Answer
import com.nextersolutions.soulmetric.core.domain.model.Survey
import com.nextersolutions.soulmetric.core.domain.usecase.GetSurveyByIdUseCase
import com.nextersolutions.soulmetric.core.domain.usecase.SubmitSurveyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SurveyViewModel @Inject constructor(
    private val getSurveyByIdUseCase: GetSurveyByIdUseCase,
    private val submitSurveyUseCase: SubmitSurveyUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val surveyId: String = checkNotNull(savedStateHandle["surveyId"])

    private val _state = MutableStateFlow(SurveyState(isLoading = true))
    val state: StateFlow<SurveyState> = _state.asStateFlow()

    private val _effect = Channel<SurveyEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(SurveyIntent.LoadSurvey)
    }

    fun onIntent(intent: SurveyIntent) {
        when (intent) {
            SurveyIntent.LoadSurvey -> loadSurvey()
            is SurveyIntent.AnswerScale -> answer(Answer.ScaleAnswer(intent.questionId, intent.value))
            is SurveyIntent.AnswerChoice -> answer(Answer.ChoiceAnswer(intent.questionId, intent.optionId, intent.optionText))
            is SurveyIntent.AnswerText -> answer(Answer.TextAnswer(intent.questionId, intent.text))
            SurveyIntent.NextQuestion -> nextStep()
            SurveyIntent.PreviousQuestion -> previousStep()
            SurveyIntent.Submit -> submitSurvey()
        }
    }

    private fun loadSurvey() {
        viewModelScope.launch {
            val survey = getSurveyByIdUseCase(surveyId)
            if (survey != null) {
                _state.update { it.copy(survey = survey, steps = buildSteps(survey), isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Survey not found") }
                _effect.send(SurveyEffect.NavigateBack)
            }
        }
    }

    /**
     * Build an ordered step list interleaving SectionSplash entries before the first
     * question of each section (including the very first section).
     */
    private fun buildSteps(survey: Survey): List<SurveyStep> {
        val sectionTitles = survey.sections.associate { it.id to it.title }
        val steps = mutableListOf<SurveyStep>()
        var lastSectionId: String? = null  // sentinel — no section seen yet
        var questionNumber = 0

        for (question in survey.questions) {
            val sectionId = question.sectionId
            if (sectionId != null && sectionId != lastSectionId) {
                // First question of a new section → insert splash first
                val title = sectionTitles[sectionId] ?: sectionId
                steps += SurveyStep.SectionSplash(sectionId = sectionId, title = title)
                lastSectionId = sectionId
            } else if (sectionId == null && lastSectionId == null && steps.isEmpty()) {
                // Survey has no sections at all — questions flow straight through (no splash)
            }
            questionNumber++
            steps += SurveyStep.QuestionStep(question = question, questionNumber = questionNumber)
        }
        return steps
    }

    private fun answer(answer: Answer) {
        _state.update { it.copy(answers = it.answers + (answer.questionId to answer)) }
    }

    private fun nextStep() {
        val s = _state.value
        if (s.isLastStep) {
            onIntent(SurveyIntent.Submit)
        } else {
            _state.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
        }
    }

    /** Go back to the nearest previous QuestionStep, skipping any SectionSplash in between. */
    private fun previousStep() {
        val s = _state.value
        val prevQuestionIdx = (s.currentStepIndex - 1 downTo 0)
            .firstOrNull { s.steps[it] is SurveyStep.QuestionStep }
        if (prevQuestionIdx != null) {
            _state.update { it.copy(currentStepIndex = prevQuestionIdx) }
        } else {
            viewModelScope.launch { _effect.send(SurveyEffect.NavigateBack) }
        }
    }

    private fun submitSurvey() {
        val survey = _state.value.survey ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            runCatching { submitSurveyUseCase(survey, _state.value.answers) }
                .onSuccess { resultId ->
                    _state.update { it.copy(isSubmitting = false, isCompleted = true, resultId = resultId) }
                    _effect.send(SurveyEffect.NavigateToResult(resultId))
                }
                .onFailure { e ->
                    _state.update { it.copy(isSubmitting = false) }
                    _effect.send(SurveyEffect.ShowError(e.message ?: "Submission failed"))
                }
        }
    }
}
