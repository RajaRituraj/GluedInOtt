package com.gluedin.ott.data.datasource

import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.HomeResponseDto

// Contract for fetching OTT content data
interface ContentRemoteDataSource {
    suspend fun fetchHomeFeed(): HomeResponseDto
    suspend fun fetchContentById(id: String): ContentDto?
}
