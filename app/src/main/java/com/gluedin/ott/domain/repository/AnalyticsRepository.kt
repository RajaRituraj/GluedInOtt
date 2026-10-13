package com.gluedin.ott.domain.repository

import com.gluedin.ott.domain.model.PlayerEvent
import kotlinx.coroutines.flow.Flow

interface AnalyticsRepository {
    suspend fun logPlayerEvent(event: PlayerEvent)
    fun getLoggedEvents(): Flow<List<PlayerEvent>>
}
