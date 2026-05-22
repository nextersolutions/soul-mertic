package com.nextersolutions.soulmetric.feature.results.presentation.viewmodel

import com.nextersolutions.soulmetric.core.domain.model.SurveyResult

data class ResultsState(
    val results: List<SurveyResult> = emptyList(),
    val isLoading: Boolean = false
)

sealed class ResultsIntent {
    data object Load : ResultsIntent()
    data class Delete(val resultId: Long) : ResultsIntent()
}

sealed class ResultsEffect {
    data object NavigateBack : ResultsEffect()
}
