package com.gluedin.ott.domain.usecase

import com.gluedin.ott.domain.model.HomeFeed
import com.gluedin.ott.domain.model.Resource
import com.gluedin.ott.domain.repository.ContentRepository

class GetHomeFeedUseCase(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(): Resource<HomeFeed> {
        return contentRepository.getHomeFeed()
    }
}
