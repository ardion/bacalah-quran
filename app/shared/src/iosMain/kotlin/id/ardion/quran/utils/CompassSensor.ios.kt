package id.ardion.quran.utils

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.CLHeading
import platform.Foundation.NSError
import platform.darwin.NSObject

actual class CompassSensor {
    private val _azimuth = MutableStateFlow(0f)
    actual val azimuth: StateFlow<Float> = _azimuth

    private val _accuracy = MutableStateFlow(3)
    actual val accuracy: StateFlow<Int> = _accuracy

    private val _isFlat = MutableStateFlow(true)
    actual val isFlat: StateFlow<Boolean> = _isFlat

    private var locationManager: CLLocationManager? = null
    private var delegate: CompassHeadingDelegate? = null

    actual fun start() {
        try {
            if (locationManager == null) {
                locationManager = CLLocationManager()
                delegate = CompassHeadingDelegate(
                    onHeadingUpdated = { heading, accLevel ->
                        _azimuth.value = heading
                        _accuracy.value = accLevel
                    }
                )
                locationManager?.delegate = delegate
            }
            locationManager?.startUpdatingHeading()
        } catch (_: Throwable) {}
    }

    actual fun stop() {
        try {
            locationManager?.stopUpdatingHeading()
        } catch (_: Throwable) {}
    }

    actual fun setLocation(latitude: Double, longitude: Double, altitude: Double) {
        // iOS automatically computes trueHeading via CoreLocation when location services are active
    }

    private class CompassHeadingDelegate(
        private val onHeadingUpdated: (Float, Int) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {
        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            val heading = if (didUpdateHeading.trueHeading >= 0) {
                didUpdateHeading.trueHeading
            } else {
                didUpdateHeading.magneticHeading
            }

            val acc = didUpdateHeading.headingAccuracy
            val accLevel = when {
                acc < 0.0 -> 0 // Unreliable
                acc > 25.0 -> 1 // Low
                acc > 15.0 -> 2 // Medium
                else -> 3 // High
            }

            if (heading >= 0) {
                onHeadingUpdated(heading.toFloat(), accLevel)
            }
        }

        override fun locationManagerShouldDisplayHeadingCalibration(manager: CLLocationManager): Boolean {
            // Enable native iOS calibration overlay when magnetic interference is detected
            return true
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            // Handle iOS Simulator missing compass hardware gracefully
        }
    }
}
