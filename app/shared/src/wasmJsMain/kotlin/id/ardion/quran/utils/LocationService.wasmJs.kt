package id.ardion.quran.utils

actual class LocationService {
    actual suspend fun getCurrentLocation(): LocationCoordinates? {
        return LocationCoordinates(-6.2297, 106.8295, isLiveGps = false)
    }
}
