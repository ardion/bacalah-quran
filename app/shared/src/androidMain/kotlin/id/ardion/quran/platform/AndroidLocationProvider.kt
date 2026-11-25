package id.ardion.quran.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import id.ardion.quran.domain.model.LocationCoordinates
import id.ardion.quran.domain.model.LocationResult
import id.ardion.quran.domain.repository.LocationProvider
import id.ardion.quran.logging.logWarning
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

private const val TAG = "LocationProvider"
private const val FRESH_LOCATION_TIMEOUT_MS = 10_000L
private const val LAST_LOCATION_TIMEOUT_MS = 3_000L

class AndroidLocationProvider(private val context: Context) : LocationProvider {

    override suspend fun getCurrentLocation(): LocationResult {
        val hasFine = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
        val hasCoarse = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (!hasFine && !hasCoarse) return LocationResult.PermissionDenied

        val client = LocationServices.getFusedLocationProviderClient(context)
        val priority = if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        val coordinates = withTimeoutOrNull(FRESH_LOCATION_TIMEOUT_MS) { client.currentLocation(priority) }
            ?: withTimeoutOrNull(LAST_LOCATION_TIMEOUT_MS) { client.lastKnownLocation() }

        return coordinates?.let { LocationResult.Available(it) } ?: LocationResult.Unavailable
    }

    private fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    // Permission is checked in getCurrentLocation() before either call below.
    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.currentLocation(priority: Int): LocationCoordinates? =
        suspendCancellableCoroutine { continuation ->
            val cancellation = CancellationTokenSource()
            getCurrentLocation(priority, cancellation.token)
                .addOnSuccessListener { continuation.resume(it?.let { l -> LocationCoordinates(l.latitude, l.longitude) }) }
                .addOnFailureListener {
                    logWarning(TAG, "getCurrentLocation failed", it)
                    continuation.resume(null)
                }
            continuation.invokeOnCancellation { cancellation.cancel() }
        }

    // Same permission check as above.
    @SuppressLint("MissingPermission")
    private suspend fun FusedLocationProviderClient.lastKnownLocation(): LocationCoordinates? =
        suspendCancellableCoroutine { continuation ->
            lastLocation
                .addOnSuccessListener { continuation.resume(it?.let { l -> LocationCoordinates(l.latitude, l.longitude) }) }
                .addOnFailureListener {
                    logWarning(TAG, "lastLocation failed", it)
                    continuation.resume(null)
                }
        }
}
