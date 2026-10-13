package com.gluedin.ott.player

import androidx.media3.common.Player
import com.gluedin.ott.domain.model.PlayerEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerEventHandlerTest {

    @Test
    fun `onPlaybackStateChanged emits Ready and Completed events properly`() {
        val capturedEvents = mutableListOf<PlayerEvent>()
        val handler = PlayerEventHandler(
            contentId = "test_item",
            contentTitle = "Test Content",
            onEvent = { capturedEvents.add(it) }
        )

        // Simulate STATE_READY
        handler.onPlaybackStateChanged(Player.STATE_READY)
        assertEquals(1, capturedEvents.size)
        assertTrue(capturedEvents[0] is PlayerEvent.Ready)
        assertEquals("test_item", capturedEvents[0].contentId)
        assertEquals("Test Content", capturedEvents[0].contentTitle)

        // Simulate STATE_ENDED
        handler.onPlaybackStateChanged(Player.STATE_ENDED)
        assertEquals(2, capturedEvents.size)
        assertTrue(capturedEvents[1] is PlayerEvent.Completed)
    }

    @Test
    fun `onIsPlayingChanged emits Started and Paused events`() {
        val capturedEvents = mutableListOf<PlayerEvent>()
        val handler = PlayerEventHandler(
            contentId = "video_99",
            contentTitle = "Action Movie",
            onEvent = { capturedEvents.add(it) }
        )

        // Playing started
        handler.onIsPlayingChanged(true)
        assertEquals(1, capturedEvents.size)
        assertTrue(capturedEvents[0] is PlayerEvent.Started)

        // Playing paused
        handler.onIsPlayingChanged(false)
        assertEquals(2, capturedEvents.size)
        assertTrue(capturedEvents[1] is PlayerEvent.Paused)
    }

    @Test
    fun `buffering state transitions emit buffering start and end events`() {
        val capturedEvents = mutableListOf<PlayerEvent>()
        val handler = PlayerEventHandler(
            contentId = "buff_1",
            contentTitle = "Buffering Show",
            onEvent = { capturedEvents.add(it) }
        )

        // Buffering started
        handler.onPlaybackStateChanged(Player.STATE_BUFFERING)
        assertEquals(1, capturedEvents.size)
        val bufferingStart = capturedEvents[0] as PlayerEvent.Buffering
        assertTrue(bufferingStart.isBuffering)

        // Buffering ended (state moves to ready)
        handler.onPlaybackStateChanged(Player.STATE_READY)
        // Two events emitted: Buffering(false) and Ready
        assertTrue(capturedEvents.any { it is PlayerEvent.Buffering && !it.isBuffering })
    }
}
