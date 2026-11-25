package id.ardion.quran.audio

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioQueueController(private val player: AudioPlayback) {

    val isAvailable: Boolean get() = player.isAvailable

    private val _playingUrl = MutableStateFlow<String?>(null)
    val playingUrl: StateFlow<String?> = _playingUrl.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPlayingQueue = MutableStateFlow(false)
    val isPlayingQueue: StateFlow<Boolean> = _isPlayingQueue.asStateFlow()

    private var queueIndex = 0

    private var playbackToken = 0

    fun toggleSingle(url: String) {
        _isPlayingQueue.value = false
        if (_playingUrl.value == url && _isPlaying.value) {
            pause()
        } else {
            play(url) { 
                _playingUrl.value = null
                _isPlaying.value = false
            }
        }
    }

    fun toggleQueue(urls: List<String?>) {
        if (_isPlayingQueue.value && _isPlaying.value) {
            pause()
        } else {
            _isPlayingQueue.value = true
            playQueueFrom(queueIndex, urls)
        }
    }

    fun pause() {
        playbackToken++
        _isPlayingQueue.value = false
        _isPlaying.value = false
        player.pause()
    }

    fun stop() {
        playbackToken++
        _isPlayingQueue.value = false
        _isPlaying.value = false
        _playingUrl.value = null
        queueIndex = 0
        player.stop()
    }

    private fun playQueueFrom(startIndex: Int, urls: List<String?>) {
        val next = urls.withIndex().drop(startIndex).firstOrNull { it.value != null }
        val url = next?.value
        if (next == null || url == null) {
            stop()
            return
        }
        queueIndex = next.index
        play(url) { playQueueFrom(next.index + 1, urls) }
    }

    private fun play(url: String, onCompletion: () -> Unit) {
        val token = ++playbackToken
        _playingUrl.value = url
        _isPlaying.value = true
        player.play(url) {
            if (token == playbackToken) onCompletion()
        }
    }
}
