package id.ardion.quran.utils

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

actual class LocationService {
    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(): LocationCoordinates? = withContext(Dispatchers.Main) {
        val context = ContextHolder.appContext
            ?: return@withContext LocationCoordinates(-6.2088, 106.8456)

        val hasFine = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return@withContext LocationCoordinates(-6.2088, 106.8456)
        }

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        val priority = if (hasFine) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val freshLocation = withTimeoutOrNull(10_000L) {
            suspendCancellableCoroutine<LocationCoordinates?> { continuation ->
                val cts = CancellationTokenSource()
                fusedClient.getCurrentLocation(priority, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(
                                LocationCoordinates(location.latitude, location.longitude)
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }

                continuation.invokeOnCancellation {
                    cts.cancel()
                }
            }
        }

        if (freshLocation != null) {
            return@withContext freshLocation
        }

        val lastLocation = withTimeoutOrNull(3_000L) {
            suspendCancellableCoroutine<LocationCoordinates?> { continuation ->
                fusedClient.lastLocation
                    .addOnSuccessListener { location ->
                        if (location != null) {
                            continuation.resume(
                                LocationCoordinates(location.latitude, location.longitude)
                            )
                        } else {
                            continuation.resume(null)
                        }
                    }
                    .addOnFailureListener {
                        continuation.resume(null)
                    }
            }
        }

        lastLocation ?: LocationCoordinates(-6.2088, 106.8456)
    }
}
