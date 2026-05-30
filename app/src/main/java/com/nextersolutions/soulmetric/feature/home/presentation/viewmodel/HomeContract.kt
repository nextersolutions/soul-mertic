package com.nextersolutions.soulmetric.feature.home.presentation.viewmodel

import com.nextersolutions.soulmetric.core.domain.model.Survey

data class HomeState(
    val surveys: List<Survey> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null
)

sealed class HomeIntent {
    data object LoadSurveys : HomeIntent()
    data object RefreshSurveys : HomeIntent()
    data class OpenSurvey(val surveyId: String) : HomeIntent()
    data object NavigateToResults : HomeIntent()
}

sealed class HomeEffect {
    data class NavigateToSurvey(val surveyId: String) : HomeEffect()
    data object NavigateToResults : HomeEffect()
    data class ShowSnackbar(val message: String) : HomeEffect()
}
