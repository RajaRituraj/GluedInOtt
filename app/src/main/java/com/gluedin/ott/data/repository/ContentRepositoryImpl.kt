package com.gluedin.ott.data.repository

import com.gluedin.ott.data.datasource.ContentRemoteDataSource
import com.gluedin.ott.data.mapper.ContentMapper
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.HomeFeed
import com.gluedin.ott.domain.model.Resource
import com.gluedin.ott.domain.repository.ContentRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentRepositoryImpl(
    private val remoteDataSource: ContentRemoteDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ContentRepository {

    override suspend fun getHomeFeed(): Resource<HomeFeed> = withContext(ioDispatcher) {
        try {
            val responseDto = remoteDataSource.fetchHomeFeed()
            val homeFeed = ContentMapper.toDomain(responseDto)
            if (homeFeed.rails.isEmpty() && homeFeed.heroBanner.title.isEmpty()) {
                Resource.Error("No content available at this time.")
            } else {
                Resource.Success(homeFeed)
            }
        } catch (e: Exception) {
            Resource.Error(
                message = e.localizedMessage ?: "Failed to load content. Please check connection.",
                cause = e
            )
        }
    }

    override suspend fun getContentDetails(contentId: String): Resource<ContentItem> =
        withContext(ioDispatcher) {
            try {
                val itemDto = remoteDataSource.fetchContentById(contentId)
                if (itemDto != null) {
                    Resource.Success(ContentMapper.toDomain(itemDto))
                } else {
                    Resource.Error("Content with ID '$contentId' was not found.")
                }
            } catch (e: Exception) {
                Resource.Error(
                    message = e.localizedMessage ?: "Failed to retrieve content details.",
                    cause = e
                )
            }
        }
}
