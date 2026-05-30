package com.nextersolutions.soulmetric.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nextersolutions.soulmetric.core.domain.repository.SurveyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Downloads the surveys JSON from the network and persists any surveys that are
 * new or have a higher version than what is currently cached in Room.
 *
 * Scheduled on every app start via [SoulMetricApp] with [androidx.work.ExistingWorkPolicy.REPLACE],
 * so a fresh sync is triggered each launch and any stale pending job is cancelled first.
 */
@HiltWorker
class SurveySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val surveyRepository: SurveyRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return surveyRepository.refreshSurveysFromNetwork().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }

    companion object {
        const val WORK_NAME = "survey_sync"
    }
}
