@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.ardion.quran.audio

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement

actual class AudioPlayer actual constructor() {
    private var audioElement: HTMLAudioElement? = null

    actual fun play(url: String) {
        stop()
        audioElement = (document.createElement("audio") as HTMLAudioElement).apply {
            src = url
            play()
        }
    }

    actual fun pause() {
        audioElement?.pause()
    }

    actual fun stop() {
        audioElement?.pause()
        audioElement = null
    }
}
