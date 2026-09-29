@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package id.ardion.quran.audio

import android.media.AudioAttributes
import android.media.MediaPlayer

actual class AudioPlayer actual constructor() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isPaused = false

    actual fun play(url: String) {
        if (currentUrl == url && mediaPlayer != null) {
            if (isPaused) {
                mediaPlayer?.start()
                isPaused = false
            }
        } else {
            stop()
            currentUrl = url
            isPaused = false
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
                setOnPreparedListener { 
                    start() 
                }
            }
        }
    }

    actual fun pause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                isPaused = true
            }
        }
    }

    actual fun stop() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.stop()
                }
            } catch (_: Exception) {}
            it.release()
        }
        mediaPlayer = null
        currentUrl = null
        isPaused = false
    }
}
