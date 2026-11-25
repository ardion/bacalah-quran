package id.ardion.quran.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.Foundation.NSError
import platform.darwin.NSObject

private const val ACCURACY_UNRELIABLE = 0
private const val ACCURACY_LOW = 1
private const val ACCURACY_MEDIUM = 2
private const val ACCURACY_HIGH = 3
private const val LOW_ACCURACY_DEGREES = 25.0
private const val MEDIUM_ACCURACY_DEGREES = 15.0

class IosCompassSensor : CompassSensor {
    override val isAvailable: Boolean = CLLocationManager.headingAvailable()

    private val _azimuth = MutableStateFlow(0f)
    override val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _accuracy = MutableStateFlow(ACCURACY_HIGH)
    override val accuracy: StateFlow<Int> = _accuracy.asStateFlow()

    override val isFlat: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    private val delegate = HeadingDelegate { heading, accuracyLevel ->
        _azimuth.value = heading
        _accuracy.value = accuracyLevel
    }
    private val locationManager = CLLocationManager().also { it.delegate = delegate }

    override fun start() {
        if (isAvailable) locationManager.startUpdatingHeading()
    }

    override fun stop() {
        locationManager.stopUpdatingHeading()
    }

    override fun setLocation(latitude: Double, longitude: Double) = Unit

    private class HeadingDelegate(
        private val onHeading: (Float, Int) -> Unit
    ) : NSObject(), CLLocationManagerDelegateProtocol {

        @OptIn(ExperimentalForeignApi::class)
        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            val heading = didUpdateHeading.trueHeading.takeIf { it >= 0 } ?: didUpdateHeading.magneticHeading
            if (heading < 0) return

            val error = didUpdateHeading.headingAccuracy
            val level = when {
                error < 0.0 -> ACCURACY_UNRELIABLE
                error > LOW_ACCURACY_DEGREES -> ACCURACY_LOW
                error > MEDIUM_ACCURACY_DEGREES -> ACCURACY_MEDIUM
                else -> ACCURACY_HIGH
            }
            onHeading(heading.toFloat(), level)
        }

        override fun locationManagerShouldDisplayHeadingCalibration(manager: CLLocationManager): Boolean = true

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) = Unit
    }
}
