package id.ardion.quran.utils

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double,
    val isLiveGps: Boolean = false
)

expect class LocationService() {
    suspend fun getCurrentLocation(): LocationCoordinates?
}
