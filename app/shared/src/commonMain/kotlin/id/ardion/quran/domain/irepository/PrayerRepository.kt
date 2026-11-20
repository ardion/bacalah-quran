package id.ardion.quran.domain.irepository

import id.ardion.quran.domain.model.PrayerTimes

interface PrayerRepository {
    suspend fun getPrayerTimes(latitude: Double, longitude: Double): Result<PrayerTimes>
}
