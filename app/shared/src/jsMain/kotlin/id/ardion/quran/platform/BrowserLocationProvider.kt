package id.ardion.quran.platform

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.LocationResult
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.logging.logWarning
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val TAG = "LocationProvider"
private const val PERMISSION_DENIED_CODE = 1

class BrowserLocationProvider : LocationProvider {

    override suspend fun getCurrentLocation(): LocationResult = suspendCancellableCoroutine { continuation ->
        fun finish(result: LocationResult) {
            if (continuation.isActive) continuation.resume(result)
        }

        val geolocation = window.navigator.asDynamic().geolocation
        if (geolocation == null || geolocation == undefined) {
            finish(LocationResult.Unavailable)
            return@suspendCancellableCoroutine
        }

        try {
            geolocation.getCurrentPosition(
                { position: dynamic ->
                    val latitude = (position.coords.latitude as? Number)?.toDouble()
                    val longitude = (position.coords.longitude as? Number)?.toDouble()
                    finish(
                        if (latitude != null && longitude != null) {
                            LocationResult.Available(LocationCoordinates(latitude, longitude))
                        } else {
                            LocationResult.Unavailable
                        }
                    )
                },
                { error: dynamic ->
                    val code = (error?.code as? Number)?.toInt()
                    finish(if (code == PERMISSION_DENIED_CODE) LocationResult.PermissionDenied else LocationResult.Unavailable)
                }
            )
        } catch (e: Throwable) {
            logWarning(TAG, "Geolocation call failed", e)
            finish(LocationResult.Unavailable)
        }
    }
}
