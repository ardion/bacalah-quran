package id.ardion.quran.audio

expect class AudioPlayer() : AudioPlayback {
    override val isAvailable: Boolean
    override fun play(url: String, onCompletion: (() -> Unit)?)
    override fun pause()
    override fun stop()
}
