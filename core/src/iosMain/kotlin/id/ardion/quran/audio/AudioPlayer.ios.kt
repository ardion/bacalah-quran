@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.ardion.quran.audio

import platform.AVFoundation.AVPlayer
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.Foundation.NSURL

actual class AudioPlayer actual constructor() {
    private var player: AVPlayer? = null
    private var currentUrl: String? = null

    actual fun play(url: String) {
        if (currentUrl == url && player != null) {
            player?.play()
        } else {
            currentUrl = url
            val nsUrl = NSURL.URLWithString(url) ?: return
            player = AVPlayer(uRL = nsUrl)
            player?.play()
        }
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        player?.pause()
        player = null
        currentUrl = null
    }
}
