package id.ardion.quran.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

actual class LocationService {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getCurrentLocation(): LocationCoordinates? {
        return try {
            val locationManager = CLLocationManager()
            val status = locationManager.authorizationStatus
            if (status != kCLAuthorizationStatusAuthorizedWhenInUse && status != kCLAuthorizationStatusAuthorizedAlways) {
                locationManager.requestWhenInUseAuthorization()
            }

            val loc = locationManager.location
            if (loc != null) {
                val coords = loc.coordinate
                coords.useContents {
                    LocationCoordinates(latitude, longitude, isLiveGps = true)
                }
            } else {
                LocationCoordinates(-6.2297, 106.8295, isLiveGps = false)
            }
        } catch (_: Throwable) {
            LocationCoordinates(-6.2297, 106.8295, isLiveGps = false)
        }
    }
}
