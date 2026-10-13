package com.gluedin.ott.domain.usecase

import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.Resource
import com.gluedin.ott.domain.repository.ContentRepository

class GetContentDetailsUseCase(
    private val contentRepository: ContentRepository
) {
    suspend operator fun invoke(contentId: String): Resource<ContentItem> {
        return contentRepository.getContentDetails(contentId)
    }
}
