package id.ardion.quran.utils

import kotlinx.coroutines.flow.StateFlow

expect class CompassSensor() {
    val azimuth: StateFlow<Float>
    val accuracy: StateFlow<Int>
    val isFlat: StateFlow<Boolean>
    fun start()
    fun stop()
    fun setLocation(latitude: Double, longitude: Double, altitude: Double = 0.0)
}
