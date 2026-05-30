package com.nextersolutions.soulmetric

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nextersolutions.soulmetric.core.data.worker.SurveySyncWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import androidx.work.Configuration as WorkConfiguration

@HiltAndroidApp
class SoulMetricApp : Application() {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        WorkManager.initialize(
            this,
            WorkConfiguration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        )

        enqueueSurveysSync()
    }

    private fun enqueueSurveysSync() {
        WorkManager.getInstance(this).enqueueUniqueWork(
            SurveySyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<SurveySyncWorker>().build()
        )
    }
}
