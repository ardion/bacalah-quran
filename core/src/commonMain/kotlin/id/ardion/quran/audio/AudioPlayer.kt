@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.ardion.quran.audio

expect class AudioPlayer() {
    fun play(url: String, onCompletion: (() -> Unit)? = null)
    fun pause()
    fun stop()
}
