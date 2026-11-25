package id.ardion.quran.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import id.ardion.quran.logging.logWarning

private const val TAG = "AudioPlayer"

actual class AudioPlayer actual constructor() : AudioPlayback {
    actual override val isAvailable: Boolean = true

    private var mediaPlayer: MediaPlayer? = null
    private var currentUrl: String? = null
    private var isPaused = false

    actual override fun play(url: String, onCompletion: (() -> Unit)?) {
        val player = mediaPlayer
        if (currentUrl == url && player != null) {
            player.setOnCompletionListener {
                currentUrl = null
                isPaused = false
                onCompletion?.invoke()
            }
            if (isPaused) {
                player.start()
                isPaused = false
            }
            return
        }

        stop()
        currentUrl = url
        try {
            mediaPlayer = createPlayer(url, onCompletion)
        } catch (e: Exception) {
            logWarning(TAG, "Could not start $url", e)
            stop()
            onCompletion?.invoke()
        }
    }

    private fun createPlayer(url: String, onCompletion: (() -> Unit)?): MediaPlayer =
        MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setOnPreparedListener { it.start() }
            setOnCompletionListener {
                currentUrl = null
                isPaused = false
                onCompletion?.invoke()
            }
            setOnErrorListener { _, what, extra ->
                logWarning(TAG, "Playback failed ($what/$extra) for $url")
                stop()
                onCompletion?.invoke()
                true
            }
            setDataSource(url)
            prepareAsync()
        }

    actual override fun pause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            isPaused = true
        }
    }

    actual override fun stop() {
        mediaPlayer?.let { player ->
            try {
                if (player.isPlaying) player.stop()
            } catch (e: IllegalStateException) {
                logWarning(TAG, "stop() called in an invalid player state", e)
            }
            player.release()
        }
        mediaPlayer = null
        currentUrl = null
        isPaused = false
    }
}
