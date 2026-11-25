package id.ardion.quran.data.repository

import id.ardion.quran.data.mapper.formatLocationName
import id.ardion.quran.data.mapper.toDomain
import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.domain.repository.PrayerRepository
import id.ardion.quran.exception.NetworkException
import id.ardion.quran.logging.logWarning
import id.ardion.quran.result.runCatchingCancellable
import id.ardion.quran.domain.qibla.QiblaCalculator

private const val TAG = "PrayerRepository"

class PrayerRepositoryImpl(
    private val apiService: PrayerApiService
) : PrayerRepository {

    override suspend fun getPrayerTimes(
        location: LocationCoordinates,
        isLiveLocation: Boolean
    ): Result<PrayerTimes> {
        val response = runCatchingCancellable { apiService.fetchPrayerTimings(location) }
            .getOrElse { return Result.failure(it) }

        val prayerTimes = response.data?.toDomain(
            location = location,
            qiblaBearing = QiblaCalculator.calculateQiblaBearing(location.latitude, location.longitude),
            locationName = resolveLocationName(location),
            isLiveLocation = isLiveLocation
        )
        return prayerTimes?.let { Result.success(it) } ?: Result.failure(NetworkException.EmptyResponse)
    }

    private suspend fun resolveLocationName(location: LocationCoordinates): String? {
        val nominatim = runCatchingCancellable {
            apiService.fetchReverseGeocodeNominatim(location).address?.formatLocationName()
        }.onFailure { logWarning(TAG, "Nominatim reverse geocoding failed", it) }.getOrNull()
        if (nominatim != null) return nominatim

        val bigData = runCatchingCancellable {
            apiService.fetchReverseGeocodeBigData(location).formatLocationName()
        }.onFailure { logWarning(TAG, "BigDataCloud reverse geocoding failed", it) }.getOrNull()

        return bigData
    }
}
