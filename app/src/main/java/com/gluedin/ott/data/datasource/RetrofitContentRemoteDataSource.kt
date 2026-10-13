package com.gluedin.ott.data.datasource

import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.HomeResponseDto

class RetrofitContentRemoteDataSource(
    private val apiService: OttApiService
) : ContentRemoteDataSource {

    override suspend fun fetchHomeFeed(): HomeResponseDto {
        return apiService.getHomeFeed()
    }

    override suspend fun fetchContentById(id: String): ContentDto? {
        return try {
            apiService.getContentDetails(id)
        } catch (e: Exception) {
            null
        }
    }
}
