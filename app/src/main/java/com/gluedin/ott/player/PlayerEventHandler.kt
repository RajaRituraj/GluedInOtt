package com.gluedin.ott.player

import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import com.gluedin.ott.domain.model.PlayerEvent

/**
 * Listens to Media3 Player callbacks, normalizes them, and delivers domain [PlayerEvent]s.
 * Separates player framework concerns from ViewModel and Business/Analytics logic.
 */
class PlayerEventHandler(
    private val contentId: String,
    private val contentTitle: String,
    private val onEvent: (PlayerEvent) -> Unit
) : Player.Listener {

    private var lastRecordedPositionMs: Long = 0L
    private var isCurrentlyBuffering: Boolean = false
    private var hasFiredReady: Boolean = false

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                if (isCurrentlyBuffering) {
                    isCurrentlyBuffering = false
                    onEvent(
                        PlayerEvent.Buffering(
                            contentId = contentId,
                            contentTitle = contentTitle,
                            isBuffering = false,
                            positionMs = lastRecordedPositionMs
                        )
                    )
                }
                if (!hasFiredReady) {
                    hasFiredReady = true
                    onEvent(
                        PlayerEvent.Ready(
                            contentId = contentId,
                            contentTitle = contentTitle,
                            durationMs = 0L // Updated dynamically when duration is known
                        )
                    )
                }
            }
            Player.STATE_BUFFERING -> {
                isCurrentlyBuffering = true
                onEvent(
                    PlayerEvent.Buffering(
                        contentId = contentId,
                        contentTitle = contentTitle,
                        isBuffering = true,
                        positionMs = lastRecordedPositionMs
                    )
                )
            }
            Player.STATE_ENDED -> {
                onEvent(
                    PlayerEvent.Completed(
                        contentId = contentId,
                        contentTitle = contentTitle,
                        totalDurationMs = lastRecordedPositionMs
                    )
                )
            }
            Player.STATE_IDLE -> {
                // Idle / Initialized
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (isPlaying) {
            onEvent(
                PlayerEvent.Started(
                    contentId = contentId,
                    contentTitle = contentTitle,
                    positionMs = lastRecordedPositionMs
                )
            )
        } else {
            // Only fire paused if not ended or buffering
            onEvent(
                PlayerEvent.Paused(
                    contentId = contentId,
                    contentTitle = contentTitle,
                    positionMs = lastRecordedPositionMs
                )
            )
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            onEvent(
                PlayerEvent.Seeked(
                    contentId = contentId,
                    contentTitle = contentTitle,
                    fromPositionMs = oldPosition.positionMs,
                    toPositionMs = newPosition.positionMs
                )
            )
        }
        lastRecordedPositionMs = newPosition.positionMs
    }

    override fun onPlayerError(error: PlaybackException) {
        onEvent(
            PlayerEvent.Error(
                contentId = contentId,
                contentTitle = contentTitle,
                errorCode = error.errorCodeName,
                errorMessage = error.message ?: "Unknown playback error"
            )
        )
    }

    fun updateCurrentPosition(positionMs: Long) {
        lastRecordedPositionMs = positionMs
    }
}
