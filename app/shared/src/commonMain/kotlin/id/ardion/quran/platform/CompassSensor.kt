package id.ardion.quran.platform

import kotlinx.coroutines.flow.StateFlow

interface CompassSensor {
    val isAvailable: Boolean

    val azimuth: StateFlow<Float>

    val accuracy: StateFlow<Int>

    val isFlat: StateFlow<Boolean>

    fun start()
    fun stop()

    fun setLocation(latitude: Double, longitude: Double)
}
