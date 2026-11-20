package id.ardion.quran.utils

import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class LocationService {
    actual suspend fun getCurrentLocation(): LocationCoordinates? {
        return try {
            suspendCancellableCoroutine { continuation ->
                var isResumed = false
                try {
                    val geo = window.navigator.asDynamic().geolocation
                    geo.getCurrentPosition(
                        { pos: dynamic ->
                            if (!isResumed) {
                                isResumed = true
                                val lat = (pos.coords.latitude as Number).toDouble()
                                val lon = (pos.coords.longitude as Number).toDouble()
                                continuation.resume(LocationCoordinates(lat, lon, isLiveGps = true))
                            }
                        },
                        { _: dynamic ->
                            if (!isResumed) {
                                isResumed = true
                                continuation.resume(LocationCoordinates(-6.2297, 106.8295, isLiveGps = false))
                            }
                        }
                    )
                } catch (_: Throwable) {
                    if (!isResumed) {
                        isResumed = true
                        continuation.resume(LocationCoordinates(-6.2297, 106.8295, isLiveGps = false))
                    }
                }
            }
        } catch (_: Throwable) {
            LocationCoordinates(-6.2297, 106.8295, isLiveGps = false)
        }
    }
}
