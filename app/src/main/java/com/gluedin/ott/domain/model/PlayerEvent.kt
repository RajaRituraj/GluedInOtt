package com.gluedin.ott.domain.model

sealed class PlayerEvent(
    open val contentId: String,
    open val contentTitle: String,
    open val timestampMs: Long = System.currentTimeMillis()
) {
    data class Ready(
        override val contentId: String,
        override val contentTitle: String,
        val durationMs: Long
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_READY [id=$contentId, title='$contentTitle', duration=${durationMs}ms]"
    }

    data class Started(
        override val contentId: String,
        override val contentTitle: String,
        val positionMs: Long
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_STARTED [id=$contentId, title='$contentTitle', position=${positionMs}ms]"
    }

    data class Paused(
        override val contentId: String,
        override val contentTitle: String,
        val positionMs: Long
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_PAUSED [id=$contentId, title='$contentTitle', position=${positionMs}ms]"
    }

    data class Seeked(
        override val contentId: String,
        override val contentTitle: String,
        val fromPositionMs: Long,
        val toPositionMs: Long
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_SEEKED [id=$contentId, title='$contentTitle', from=${fromPositionMs}ms -> to=${toPositionMs}ms]"
    }

    data class Buffering(
        override val contentId: String,
        override val contentTitle: String,
        val isBuffering: Boolean,
        val positionMs: Long
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_BUFFERING [id=$contentId, buffering=$isBuffering, at=${positionMs}ms]"
    }

    data class Completed(
        override val contentId: String,
        override val contentTitle: String,
        val totalDurationMs: Long
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_COMPLETED [id=$contentId, title='$contentTitle', totalDuration=${totalDurationMs}ms]"
    }

    data class Error(
        override val contentId: String,
        override val contentTitle: String,
        val errorCode: String,
        val errorMessage: String
    ) : PlayerEvent(contentId, contentTitle) {
        override fun toString(): String =
            "PLAYER_ERROR [id=$contentId, title='$contentTitle', code=$errorCode, error='$errorMessage']"
    }
}
