package com.nextersolutions.soulmetric.feature.results.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextersolutions.soulmetric.core.domain.usecase.DeleteSurveyResultUseCase
import com.nextersolutions.soulmetric.core.domain.usecase.GetSurveyResultsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultsViewModel @Inject constructor(
    private val getResultsUseCase: GetSurveyResultsUseCase,
    private val deleteResultUseCase: DeleteSurveyResultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ResultsState(isLoading = true))
    val state: StateFlow<ResultsState> = _state.asStateFlow()

    private val _effect = Channel<ResultsEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(ResultsIntent.Load)
    }

    fun onIntent(intent: ResultsIntent) {
        when (intent) {
            ResultsIntent.Load -> loadResults()
            is ResultsIntent.Delete -> deleteResult(intent.resultId)
        }
    }

    private fun loadResults() {
        viewModelScope.launch {
            getResultsUseCase()
                .collect { results ->
                    _state.update { it.copy(results = results, isLoading = false) }
                }
        }
    }

    private fun deleteResult(resultId: Long) {
        viewModelScope.launch { deleteResultUseCase(resultId) }
    }
}
