package com.steamtimeline.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.steamtimeline.app.domain.SessionTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SteamPollingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val sessionTracker: SessionTracker
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        sessionTracker.poll()
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "steam_polling_work"

        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<SteamPollingWorker>(
                repeatInterval = 1,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
