package id.ardion.quran.domain.repository

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimes
import kotlinx.datetime.LocalDate

interface PrayerRepository {
    suspend fun getPrayerTimes(location: LocationCoordinates, isLiveLocation: Boolean): Result<PrayerTimes>

    suspend fun getSchedule(
        location: LocationCoordinates,
        isLiveLocation: Boolean,
        from: LocalDate,
        days: Int
    ): Result<List<PrayerTimes>>
}
