package id.ardion.quran.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeAudioPlayback : AudioPlayback {
    override val isAvailable = true
    val played = mutableListOf<String>()
    var pauses = 0
    var stops = 0
    private var completion: (() -> Unit)? = null

    override fun play(url: String, onCompletion: (() -> Unit)?) {
        played += url
        completion = onCompletion
    }

    override fun pause() { pauses++ }
    override fun stop() { stops++ }

    fun finish() = completion?.invoke()
}

class AudioQueueControllerTest {
    private val player = FakeAudioPlayback()
    private val controller = AudioQueueController(player)

    @Test
    fun queuePlaysInOrderAndSkipsMissingUrls() {
        controller.toggleQueue(listOf("a", null, "c"))
        assertEquals("a", controller.playingUrl.value)

        player.finish()
        assertEquals(listOf("a", "c"), player.played)
        assertEquals("c", controller.playingUrl.value)

        player.finish()
        assertNull(controller.playingUrl.value)
        assertFalse(controller.isPlayingQueue.value)
    }

    @Test
    fun pausedQueueResumesFromSameAyah() {
        val urls = listOf("a", "b", "c")
        controller.toggleQueue(urls)
        player.finish()
        controller.toggleQueue(urls)
        assertFalse(controller.isPlayingQueue.value)

        controller.toggleQueue(urls)
        assertEquals("b", player.played.last())
        assertTrue(controller.isPlayingQueue.value)
    }

    @Test
    fun staleCompletionDoesNotAdvanceQueue() {
        controller.toggleQueue(listOf("a", "b"))
        controller.toggleSingle("x")
        player.finish()
        assertEquals(listOf("a", "x"), player.played)
        assertNull(controller.playingUrl.value)
    }

    @Test
    fun toggleSingleTwicePauses() {
        controller.toggleSingle("a")
        controller.toggleSingle("a")
        assertEquals(1, player.pauses)
        assertNull(controller.playingUrl.value)
    }

    @Test
    fun emptyQueueStopsWithoutCrashing() {
        controller.toggleQueue(emptyList())
        assertFalse(controller.isPlayingQueue.value)
        assertEquals(1, player.stops)
    }

    @Test
    fun stopResetsQueuePosition() {
        val urls = listOf("a", "b")
        controller.toggleQueue(urls)
        player.finish()
        controller.stop()
        controller.toggleQueue(urls)
        assertEquals("a", player.played.last())
    }
}
