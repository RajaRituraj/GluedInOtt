package com.gluedin.ott.domain.usecase

import com.gluedin.ott.domain.model.PlayerEvent
import com.gluedin.ott.domain.repository.AnalyticsRepository

class LogPlayerEventUseCase(
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(event: PlayerEvent) {
        analyticsRepository.logPlayerEvent(event)
    }
}
