package com.khoshoo3.app.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khoshoo3.app.data.PrayerRepository
import com.khoshoo3.app.data.PrayerTimeInfo
import com.khoshoo3.app.data.SilenceManager
import com.khoshoo3.app.worker.PrayerCheckWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class MainUiState(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val prayerTimes: List<PrayerTimeInfo> = emptyList(),
    val nextPrayer: PrayerTimeInfo? = null,
    val countdownText: String = "--:--:--",
    val isAutoSilentEnabled: Boolean = false,
    val isDndActive: Boolean = false,
    val isDndPermissionGranted: Boolean = false,
    val locationAvailable: Boolean = false,
    val isTestingDnd: Boolean = false,
    val testCountdown: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prayerRepository = PrayerRepository()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences(
        PrayerCheckWorker.PREFS_NAME, Context.MODE_PRIVATE
    )

    init {
        // Restore toggle state from prefs
        val saved = prefs.getBoolean(PrayerCheckWorker.KEY_AUTO_SILENT, false)
        _uiState.value = _uiState.value.copy(isAutoSilentEnabled = saved)
        startCountdownTicker()
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationAvailable = true
        )

        // Persist for the Worker
        prefs.edit()
            .putFloat(PrayerCheckWorker.KEY_LATITUDE, latitude.toFloat())
            .putFloat(PrayerCheckWorker.KEY_LONGITUDE, longitude.toFloat())
            .apply()

        refreshPrayerTimes()
    }

    fun refreshPrayerTimes() {
        val state = _uiState.value
        if (!state.locationAvailable) return

        val times = prayerRepository.getPrayerTimes(state.latitude, state.longitude)
        val next = prayerRepository.getNextPrayer(state.latitude, state.longitude)

        _uiState.value = state.copy(
            prayerTimes = times,
            nextPrayer = next
        )
    }

    fun refreshDndStatus() {
        val ctx = getApplication<Application>()
        _uiState.value = _uiState.value.copy(
            isDndActive = SilenceManager.isDndActive(ctx),
            isDndPermissionGranted = SilenceManager.isNotificationPolicyGranted(ctx)
        )
    }

    fun toggleAutoSilent(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isAutoSilentEnabled = enabled)
        prefs.edit().putBoolean(PrayerCheckWorker.KEY_AUTO_SILENT, enabled).apply()

        val ctx = getApplication<Application>()
        if (enabled) {
            PrayerCheckWorker.schedule(ctx)
        } else {
            PrayerCheckWorker.cancel(ctx)
            // Also restore DND if we had enabled it
            val weEnabledDnd = prefs.getBoolean(PrayerCheckWorker.KEY_WE_ENABLED_DND, false)
            if (weEnabledDnd) {
                SilenceManager.disableDnd(ctx)
                prefs.edit().putBoolean(PrayerCheckWorker.KEY_WE_ENABLED_DND, false).apply()
            }
        }
    }

    /**
     * Test DND toggle: enables DND for 30 seconds, then auto-disables.
     * This lets you verify the feature works without waiting for a prayer.
     */
    fun testDndNow() {
        val ctx = getApplication<Application>()
        if (!SilenceManager.isNotificationPolicyGranted(ctx)) return

        viewModelScope.launch {
            SilenceManager.enableDnd(ctx)
            _uiState.value = _uiState.value.copy(isTestingDnd = true, isDndActive = true)

            for (i in 30 downTo 1) {
                _uiState.value = _uiState.value.copy(testCountdown = i)
                delay(1000)
            }

            SilenceManager.disableDnd(ctx)
            _uiState.value = _uiState.value.copy(
                isTestingDnd = false,
                isDndActive = false,
                testCountdown = 0
            )
        }
    }

    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                val next = _uiState.value.nextPrayer
                if (next != null) {
                    val diff = next.time.time - Date().time
                    if (diff > 0) {
                        val hours = (diff / (1000 * 60 * 60)) % 24
                        val minutes = (diff / (1000 * 60)) % 60
                        val seconds = (diff / 1000) % 60
                        _uiState.value = _uiState.value.copy(
                            countdownText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        )
                    } else {
                        // Next prayer time passed — refresh
                        refreshPrayerTimes()
                    }
                }
                delay(1000)
            }
        }
    }
}
