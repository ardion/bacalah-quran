package id.ardion.quran.presentation.prayer

data class CompassReading(
    val azimuth: Float,
    val accuracy: Int,
    val isFlat: Boolean,
    val isAvailable: Boolean
)
