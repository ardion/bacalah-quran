package id.ardion.quran.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

actual class CompassSensor {
    private val _azimuth = MutableStateFlow(0f)
    actual val azimuth: StateFlow<Float> = _azimuth

    private val _accuracy = MutableStateFlow(3)
    actual val accuracy: StateFlow<Int> = _accuracy

    private val _isFlat = MutableStateFlow(true)
    actual val isFlat: StateFlow<Boolean> = _isFlat

    actual fun start() {}
    actual fun stop() {}
    actual fun setLocation(latitude: Double, longitude: Double, altitude: Double) {}
}
