package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.irepository.PrayerRepository
import id.ardion.quran.domain.model.PrayerTimes

class GetPrayerTimesUseCase(
    private val repository: PrayerRepository
) {
    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<PrayerTimes> {
        return repository.getPrayerTimes(latitude, longitude)
    }
}
