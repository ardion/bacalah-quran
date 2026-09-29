@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.ardion.quran.audio

import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.play
import platform.AVFoundation.pause
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSURL
import platform.darwin.NSObjectProtocol

actual class AudioPlayer actual constructor() {
    private var player: AVPlayer? = null
    private var currentUrl: String? = null
    private var observer: NSObjectProtocol? = null

    actual fun play(url: String, onCompletion: (() -> Unit)?) {
        if (currentUrl == url && player != null) {
            player?.play()
        } else {
            removeObserver()
            currentUrl = url
            val nsUrl = NSURL.URLWithString(url) ?: return
            val item = AVPlayerItem(uRL = nsUrl)
            player = AVPlayer(playerItem = item)

            if (onCompletion != null) {
                observer = NSNotificationCenter.defaultCenter.addObserverForName(
                    name = AVPlayerItemDidPlayToEndTimeNotification,
                    `object` = item,
                    queue = null
                ) { _ ->
                    currentUrl = null
                    onCompletion()
                }
            }

            player?.play()
        }
    }

    actual fun pause() {
        player?.pause()
    }

    actual fun stop() {
        removeObserver()
        player?.pause()
        player = null
        currentUrl = null
    }

    private fun removeObserver() {
        observer?.let {
            NSNotificationCenter.defaultCenter.removeObserver(it)
            observer = null
        }
    }
}
