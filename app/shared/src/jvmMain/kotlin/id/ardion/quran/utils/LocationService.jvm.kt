package id.ardion.quran.utils

actual class LocationService {
    actual suspend fun getCurrentLocation(): LocationCoordinates? {
        return LocationCoordinates(-6.2088, 106.8456)
    }
}
