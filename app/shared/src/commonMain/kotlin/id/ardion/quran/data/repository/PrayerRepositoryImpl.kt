package id.ardion.quran.data.repository

import id.ardion.quran.data.mapper.formatLocationName
import id.ardion.quran.data.mapper.toDomain
import id.ardion.quran.data.remote.PrayerApiService
import id.ardion.quran.domain.irepository.PrayerRepository
import id.ardion.quran.domain.model.PrayerTimes
import id.ardion.quran.exception.NetworkException
import id.ardion.quran.utils.QiblaCalculator

class PrayerRepositoryImpl(
    private val apiService: PrayerApiService
) : PrayerRepository {

    override suspend fun getPrayerTimes(
        latitude: Double,
        longitude: Double
    ): Result<PrayerTimes> {
        if (latitude == 0.0 && longitude == 0.0) {
            return Result.failure(NetworkException.LocationPermissionDenied)
        }

        return try {
            val response = apiService.fetchPrayerTimings(latitude, longitude)
            val data = response.data ?: return Result.failure(NetworkException.PrayerDataUnavailable)
            val qibla = QiblaCalculator.calculateQiblaBearing(latitude, longitude)
            val locationName = resolveLocationName(latitude, longitude)

            Result.success(
                data.toDomain(
                    latitude = latitude,
                    longitude = longitude,
                    qiblaBearing = qibla,
                    locationName = locationName
                )
            )
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    private suspend fun resolveLocationName(latitude: Double, longitude: Double): String {
        val fallbackCoordName = "Koordinat (${formatCoord(latitude)}, ${formatCoord(longitude)})"
        return try {
            val nom = apiService.fetchReverseGeocodeNominatim(latitude, longitude)
            val formatted = nom.address?.formatLocationName() ?: ""
            if (formatted.isNotBlank()) formatted else fallbackCoordName
        } catch (_: Throwable) {
            try {
                val geo = apiService.fetchReverseGeocodeBigData(latitude, longitude)
                val formatted = geo.formatLocationName()
                if (formatted.isNotBlank()) formatted else fallbackCoordName
            } catch (_: Throwable) {
                fallbackCoordName
            }
        }
    }

    private fun formatCoord(value: Double): String {
        val rounded = (value * 1000).toInt() / 1000.0
        return rounded.toString()
    }
}
