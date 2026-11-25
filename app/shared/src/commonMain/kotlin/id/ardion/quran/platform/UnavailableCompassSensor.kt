package id.ardion.quran.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ACCURACY_HIGH = 3

class UnavailableCompassSensor : CompassSensor {
    override val isAvailable: Boolean = false
    override val azimuth: StateFlow<Float> = MutableStateFlow(0f).asStateFlow()
    override val accuracy: StateFlow<Int> = MutableStateFlow(ACCURACY_HIGH).asStateFlow()
    override val isFlat: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()

    override fun start() = Unit
    override fun stop() = Unit
    override fun setLocation(latitude: Double, longitude: Double) = Unit
}
