package com.nextersolutions.soulmetric.feature.home.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.nextersolutions.soulmetric.core.data.worker.SurveySyncWorker
import com.nextersolutions.soulmetric.core.domain.usecase.GetSurveysUseCase
import com.nextersolutions.soulmetric.core.domain.usecase.RefreshSurveysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getSurveysUseCase: GetSurveysUseCase,
    private val refreshSurveysUseCase: RefreshSurveysUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _effect = Channel<HomeEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        onIntent(HomeIntent.LoadSurveys)
        observeSyncWorker()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.LoadSurveys -> loadSurveys()
            HomeIntent.RefreshSurveys -> refreshSurveys()
            is HomeIntent.OpenSurvey -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToSurvey(intent.surveyId))
            }
            HomeIntent.NavigateToResults -> viewModelScope.launch {
                _effect.send(HomeEffect.NavigateToResults)
            }
        }
    }

    private fun loadSurveys() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getSurveysUseCase()
                .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
                .collect { surveys ->
                    _state.update { it.copy(surveys = surveys, isLoading = false, error = null) }
                }
        }
    }

    private fun refreshSurveys() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            refreshSurveysUseCase()
                .onSuccess { _state.update { it.copy(isRefreshing = false) } }
                .onFailure { e ->
                    _state.update { it.copy(isRefreshing = false) }
                    _effect.send(HomeEffect.ShowSnackbar(e.message ?: "Refresh failed"))
                }
        }
    }

    /** Mirrors the WorkManager job state into [HomeState.isSyncing]. */
    private fun observeSyncWorker() {
        viewModelScope.launch {
            workManager
                .getWorkInfosForUniqueWorkFlow(SurveySyncWorker.WORK_NAME)
                .collect { infos ->
                    val running = infos.any { info ->
                        info.state == WorkInfo.State.RUNNING ||
                        info.state == WorkInfo.State.ENQUEUED
                    }
                    _state.update { it.copy(isSyncing = running) }
                }
        }
    }
}
