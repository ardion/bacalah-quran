package id.ardion.quran.domain.usecase

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.LocationResult
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.domain.repository.PrayerRepository

class GetPrayerTimesUseCase(
    private val locationProvider: LocationProvider,
    private val repository: PrayerRepository
) {
    suspend operator fun invoke(): Result<PrayerTimes> =
        when (val location = locationProvider.getCurrentLocation()) {
            is LocationResult.Available -> repository.getPrayerTimes(location.coordinates, isLiveLocation = true)
            LocationResult.PermissionDenied,
            LocationResult.Unavailable -> repository.getPrayerTimes(FALLBACK_LOCATION, isLiveLocation = false)
        }

    companion object {
        val FALLBACK_LOCATION = LocationCoordinates(latitude = -6.2088, longitude = 106.8456)
    }
}
