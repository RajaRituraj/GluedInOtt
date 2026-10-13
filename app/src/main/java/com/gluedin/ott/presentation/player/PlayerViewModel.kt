package com.gluedin.ott.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gluedin.ott.domain.model.PlayerEvent
import com.gluedin.ott.domain.usecase.LogPlayerEventUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PlayerUiState(
    val contentTitle: String = "",
    val isBuffering: Boolean = false,
    val isReady: Boolean = false,
    val errorMessage: String? = null,
    val latestEventDisplay: String = "INITIALIZED"
)

class PlayerViewModel(
    private val logPlayerEventUseCase: LogPlayerEventUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    fun onPlayerEventReceived(event: PlayerEvent) {
        // Log analytics event
        viewModelScope.launch {
            logPlayerEventUseCase(event)
        }

        // Update UI state for current playback status
        val current = _uiState.value
        _uiState.value = when (event) {
            is PlayerEvent.Ready -> current.copy(
                isReady = true,
                isBuffering = false,
                latestEventDisplay = "PLAYER_READY"
            )
            is PlayerEvent.Started -> current.copy(
                latestEventDisplay = "PLAYER_STARTED"
            )
            is PlayerEvent.Paused -> current.copy(
                latestEventDisplay = "PLAYER_PAUSED"
            )
            is PlayerEvent.Seeked -> current.copy(
                latestEventDisplay = "PLAYER_SEEKED (${event.toPositionMs / 1000}s)"
            )
            is PlayerEvent.Buffering -> current.copy(
                isBuffering = event.isBuffering,
                latestEventDisplay = if (event.isBuffering) "BUFFERING_STARTED" else "BUFFERING_ENDED"
            )
            is PlayerEvent.Completed -> current.copy(
                latestEventDisplay = "PLAYER_COMPLETED"
            )
            is PlayerEvent.Error -> current.copy(
                errorMessage = "${event.errorCode}: ${event.errorMessage}",
                latestEventDisplay = "PLAYER_ERROR"
            )
        }
    }

    fun setContentTitle(title: String) {
        _uiState.value = _uiState.value.copy(contentTitle = title)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

class PlayerViewModelFactory(
    private val logPlayerEventUseCase: LogPlayerEventUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(logPlayerEventUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
