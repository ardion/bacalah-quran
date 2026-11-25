package id.ardion.quran.audio

actual class AudioPlayer actual constructor() : AudioPlayback {
    actual override val isAvailable: Boolean = false

    actual override fun play(url: String, onCompletion: (() -> Unit)?) = Unit

    actual override fun pause() = Unit

    actual override fun stop() = Unit
}
