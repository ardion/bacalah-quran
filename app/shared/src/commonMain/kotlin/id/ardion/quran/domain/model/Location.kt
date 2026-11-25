package id.ardion.quran.domain.model

data class LocationCoordinates(
    val latitude: Double,
    val longitude: Double
)

sealed interface LocationResult {
    data class Available(val coordinates: LocationCoordinates) : LocationResult
    data object PermissionDenied : LocationResult
    data object Unavailable : LocationResult
}
