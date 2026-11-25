package id.ardion.quran.domain.qibla

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class QiblaCalculatorTest {

    private fun assertBearing(expected: Double, actual: Double, tolerance: Double = 0.5) =
        assertTrue(abs(expected - actual) <= tolerance, "expected ~$expected° but was $actual°")

    @Test
    fun jakartaFacesWestNorthWest() {
        assertBearing(295.1, QiblaCalculator.calculateQiblaBearing(-6.2088, 106.8456))
    }

    @Test
    fun londonFacesSouthEast() {
        assertBearing(119.0, QiblaCalculator.calculateQiblaBearing(51.5074, -0.1278))
    }

    @Test
    fun bearingIsAlwaysWithinZeroAnd360() {
        listOf(-89.0 to -179.0, 0.0 to 0.0, 89.0 to 179.0).forEach { (lat, lon) ->
            val bearing = QiblaCalculator.calculateQiblaBearing(lat, lon)
            assertTrue(bearing >= 0.0 && bearing < 360.0)
        }
    }
}
