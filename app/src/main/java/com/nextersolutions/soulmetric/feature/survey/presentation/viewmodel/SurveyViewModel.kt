package com.nextersolutions.soulmetric.feature.survey.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextersolutions.soulmetric.core.domain.model.Answer
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
            SurveyIntent.NextQuestion -> nextQuestion()
            SurveyIntent.PreviousQuestion -> _state.update { it.copy(currentQuestionIndex = (it.currentQuestionIndex - 1).coerceAtLeast(0)) }
            SurveyIntent.Submit -> submitSurvey()
        }
    }

    private fun loadSurvey() {
        viewModelScope.launch {
            val survey = getSurveyByIdUseCase(surveyId)
            if (survey != null) {
                _state.update { it.copy(survey = survey, isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false, error = "Survey not found") }
                _effect.send(SurveyEffect.NavigateBack)
            }
        }
    }

    private fun answer(answer: Answer) {
        _state.update { it.copy(answers = it.answers + (answer.questionId to answer)) }
    }

    private fun nextQuestion() {
        val s = _state.value
        if (s.isLastQuestion) {
            onIntent(SurveyIntent.Submit)
        } else {
            _state.update { it.copy(currentQuestionIndex = it.currentQuestionIndex + 1) }
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
