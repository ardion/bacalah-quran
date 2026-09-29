@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
@file:OptIn(ExperimentalWasmJsInterop::class)

package id.ardion.quran.audio

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement

actual class AudioPlayer actual constructor() {
    private var audioElement: HTMLAudioElement? = null
    private var currentUrl: String? = null

    actual fun play(url: String, onCompletion: (() -> Unit)?) {
        if (currentUrl == url && audioElement != null) {
            audioElement?.play()
        } else {
            stop()
            currentUrl = url
            audioElement = (document.createElement("audio") as HTMLAudioElement).apply {
                src = url
                onended = {
                    currentUrl = null
                    onCompletion?.invoke()
                }
                play()
            }
        }
    }

    actual fun pause() {
        audioElement?.pause()
    }

    actual fun stop() {
        audioElement?.pause()
        audioElement = null
        currentUrl = null
    }
}
