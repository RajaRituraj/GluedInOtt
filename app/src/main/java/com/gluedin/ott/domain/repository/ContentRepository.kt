package com.gluedin.ott.domain.repository

import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.HomeFeed
import com.gluedin.ott.domain.model.Resource

interface ContentRepository {
    suspend fun getHomeFeed(): Resource<HomeFeed>
    suspend fun getContentDetails(contentId: String): Resource<ContentItem>
}
