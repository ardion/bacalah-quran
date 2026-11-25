package id.ardion.quran.audio

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement

actual class AudioPlayer actual constructor() : AudioPlayback {
    actual override val isAvailable: Boolean = true

    private var audioElement: HTMLAudioElement? = null
    private var currentUrl: String? = null

    actual override fun play(url: String, onCompletion: (() -> Unit)?) {
        if (currentUrl == url && audioElement != null) {
            audioElement?.play()
        } else {
            stop()
            currentUrl = url
            audioElement = (document.createElement("audio") as? HTMLAudioElement)?.apply {
                src = url
                onended = {
                    currentUrl = null
                    onCompletion?.invoke()
                }
                play()
            }
        }
    }

    actual override fun pause() {
        audioElement?.pause()
    }

    actual override fun stop() {
        audioElement?.pause()
        audioElement = null
        currentUrl = null
    }
}
