package id.ardion.quran.utils

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaCalculator {
    private const val KAABA_LAT = 21.422487
    private const val KAABA_LON = 39.826206

    private fun toRadians(degrees: Double): Double = degrees * (PI / 180.0)
    private fun toDegrees(radians: Double): Double = radians * (180.0 / PI)

    fun calculateQiblaBearing(userLat: Double, userLon: Double): Double {
        val userLatRad = toRadians(userLat)
        val userLonRad = toRadians(userLon)
        val kaabaLatRad = toRadians(KAABA_LAT)
        val kaabaLonRad = toRadians(KAABA_LON)

        val deltaLon = kaabaLonRad - userLonRad

        val y = sin(deltaLon) * cos(kaabaLatRad)
        val x = cos(userLatRad) * sin(kaabaLatRad) - sin(userLatRad) * cos(kaabaLatRad) * cos(deltaLon)

        val bearingRad = atan2(y, x)
        val bearingDeg = toDegrees(bearingRad)

        return (bearingDeg + 360.0) % 360.0
    }
}
