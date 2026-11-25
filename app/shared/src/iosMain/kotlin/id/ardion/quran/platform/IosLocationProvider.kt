package id.ardion.quran.platform

import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.LocationResult
import id.ardion.quran.domain.repository.LocationProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

private const val LOCATION_TIMEOUT_MS = 15_000L

class IosLocationProvider : LocationProvider {

    override suspend fun getCurrentLocation(): LocationResult = withContext(Dispatchers.Main) {
        withTimeoutOrNull(LOCATION_TIMEOUT_MS) { requestSingleLocation() } ?: LocationResult.Unavailable
    }

    private suspend fun requestSingleLocation(): LocationResult = suspendCancellableCoroutine { continuation ->
        val manager = CLLocationManager()
        val delegate = SingleLocationDelegate { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        manager.delegate = delegate
        continuation.invokeOnCancellation {
            manager.stopUpdatingLocation()
            manager.delegate = null
            delegate.release()
        }

        when (manager.authorizationStatus) {
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted ->
                delegate.finish(LocationResult.PermissionDenied)
            kCLAuthorizationStatusNotDetermined -> manager.requestWhenInUseAuthorization()
            else -> manager.requestLocation()
        }
    }
}

private class SingleLocationDelegate(
    private var onResult: ((LocationResult) -> Unit)?
) : NSObject(), CLLocationManagerDelegateProtocol {

    fun finish(result: LocationResult) {
        onResult?.invoke(result)
        release()
    }

    fun release() {
        onResult = null
    }

    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        when (manager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> manager.requestLocation()
            kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> finish(LocationResult.PermissionDenied)
            else -> Unit
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        val coordinates = location?.coordinate?.useContents { LocationCoordinates(latitude, longitude) }
        finish(coordinates?.let { LocationResult.Available(it) } ?: LocationResult.Unavailable)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        finish(LocationResult.Unavailable)
    }
}
