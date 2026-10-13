package com.gluedin.ott.data.datasource

import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.HomeResponseDto

/**
 * Interface contract for remote content data source.
 * In production, this can be swapped with a Retrofit-backed implementation
 * without touching repositories, use cases, or presentation layers.
 */
interface ContentRemoteDataSource {
    suspend fun fetchHomeFeed(): HomeResponseDto
    suspend fun fetchContentById(id: String): ContentDto?
}
