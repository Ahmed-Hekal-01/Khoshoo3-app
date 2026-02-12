package com.khoshoo3.app.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.khoshoo3.app.data.PrayerRepository
import com.khoshoo3.app.data.SilenceManager
import java.util.concurrent.TimeUnit

class PrayerCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val prayerRepository = PrayerRepository()

    override suspend fun doWork(): Result {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(KEY_AUTO_SILENT, false)
        val latitude = prefs.getFloat(KEY_LATITUDE, 0f).toDouble()
        val longitude = prefs.getFloat(KEY_LONGITUDE, 0f).toDouble()

        if (!isEnabled || (latitude == 0.0 && longitude == 0.0)) {
            Log.d(TAG, "Worker skipped: enabled=$isEnabled, lat=$latitude, lng=$longitude")
            return Result.success()
        }

        if (!SilenceManager.isNotificationPolicyGranted(applicationContext)) {
            Log.w(TAG, "Notification policy access not granted — cannot toggle DND")
            return Result.failure()
        }

        val isWithinWindow = prayerRepository.isWithinPrayerWindow(latitude, longitude)
        Log.d(TAG, "Within prayer window: $isWithinWindow")

        if (isWithinWindow) {
            SilenceManager.enableDnd(applicationContext)
            prefs.edit().putBoolean(KEY_WE_ENABLED_DND, true).apply()
        } else {
            val weEnabledDnd = prefs.getBoolean(KEY_WE_ENABLED_DND, false)
            if (weEnabledDnd) {
                SilenceManager.disableDnd(applicationContext)
                prefs.edit().putBoolean(KEY_WE_ENABLED_DND, false).apply()
            }
        }

        return Result.success()
    }

    companion object {
        private const val TAG = "PrayerCheckWorker"
        const val WORK_NAME = "prayer_check_work"
        const val PREFS_NAME = "khoshoo3_prefs"
        const val KEY_AUTO_SILENT = "auto_silent_enabled"
        const val KEY_LATITUDE = "latitude"
        const val KEY_LONGITUDE = "longitude"
        const val KEY_WE_ENABLED_DND = "we_enabled_dnd"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<PrayerCheckWorker>(
                15, TimeUnit.MINUTES
            ).setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            ).build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
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
