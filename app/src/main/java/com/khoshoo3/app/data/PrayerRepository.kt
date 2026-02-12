package com.khoshoo3.app.data

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.Prayer
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Date

data class PrayerTimeInfo(
    val name: String,
    val time: Date
)

class PrayerRepository {

    fun getPrayerTimes(latitude: Double, longitude: Double, date: Date = Date()): List<PrayerTimeInfo> {
        val coordinates = Coordinates(latitude, longitude)
        val params = CalculationMethod.EGYPTIAN.getParameters()
        val dateComponents = DateComponents.from(date)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        return listOf(
            PrayerTimeInfo("Fajr", prayerTimes.fajr),
            PrayerTimeInfo("Sunrise", prayerTimes.sunrise),
            PrayerTimeInfo("Dhuhr", prayerTimes.dhuhr),
            PrayerTimeInfo("Asr", prayerTimes.asr),
            PrayerTimeInfo("Maghrib", prayerTimes.maghrib),
            PrayerTimeInfo("Isha", prayerTimes.isha)
        )
    }

    fun isWithinPrayerWindow(
        latitude: Double,
        longitude: Double,
        windowMinutes: Int = 15
    ): Boolean {
        val now = Date()
        val prayerTimes = getPrayerTimes(latitude, longitude, now)

        // Exclude sunrise — it's not an actual prayer
        val actualPrayers = prayerTimes.filter { it.name != "Sunrise" }

        return actualPrayers.any { prayer ->
            val diffMs = kotlin.math.abs(now.time - prayer.time.time)
            val diffMinutes = diffMs / (1000 * 60)
            diffMinutes <= windowMinutes
        }
    }

    fun getNextPrayer(latitude: Double, longitude: Double): PrayerTimeInfo? {
        val now = Date()
        val coordinates = Coordinates(latitude, longitude)
        val params = CalculationMethod.EGYPTIAN.getParameters()
        val dateComponents = DateComponents.from(now)
        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        val nextPrayer: Prayer = prayerTimes.nextPrayer()
        if (nextPrayer == Prayer.NONE) return null

        val time = prayerTimes.timeForPrayer(nextPrayer) ?: return null
        val name = when (nextPrayer) {
            Prayer.FAJR -> "Fajr"
            Prayer.SUNRISE -> "Sunrise"
            Prayer.DHUHR -> "Dhuhr"
            Prayer.ASR -> "Asr"
            Prayer.MAGHRIB -> "Maghrib"
            Prayer.ISHA -> "Isha"
            else -> "Unknown"
        }
        return PrayerTimeInfo(name, time)
    }
}
