package com.gluedin.ott.data.repository

import android.util.Log
import com.gluedin.ott.domain.model.PlayerEvent
import com.gluedin.ott.domain.repository.AnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Logs player events to logcat and keeps in-memory history
class AnalyticsRepositoryImpl : AnalyticsRepository {

    companion object {
        private const val TAG = "OTT_ANALYTICS"
    }

    private val mutex = Mutex()
    private val eventsList = mutableListOf<PlayerEvent>()
    private val _eventsFlow = MutableStateFlow<List<PlayerEvent>>(emptyList())

    override suspend fun logPlayerEvent(event: PlayerEvent) {
        Log.i(TAG, ">>> EVENT RECEIVED: $event")

        mutex.withLock {
            eventsList.add(event)
            _eventsFlow.value = eventsList.toList()
        }
    }

    override fun getLoggedEvents(): Flow<List<PlayerEvent>> {
        return _eventsFlow.asStateFlow()
    }
}
