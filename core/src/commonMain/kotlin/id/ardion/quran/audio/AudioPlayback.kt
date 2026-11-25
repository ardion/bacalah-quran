package id.ardion.quran.audio

interface AudioPlayback {
    val isAvailable: Boolean

    fun play(url: String, onCompletion: (() -> Unit)? = null)
    fun pause()
    fun stop()
}
