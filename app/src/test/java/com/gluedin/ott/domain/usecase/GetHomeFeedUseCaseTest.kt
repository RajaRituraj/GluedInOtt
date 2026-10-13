package com.gluedin.ott.domain.usecase

import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.HomeFeed
import com.gluedin.ott.domain.model.Resource
import com.gluedin.ott.domain.repository.ContentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetHomeFeedUseCaseTest {

    private val fakeHero = ContentItem(
        id = "h1",
        title = "Fake Hero",
        description = "Desc",
        thumbnailUrl = "",
        bannerUrl = "",
        videoUrl = "",
        duration = "",
        releaseYear = "",
        rating = ""
    )

    private val fakeFeed = HomeFeed(heroBanner = fakeHero, rails = emptyList())

    @Test
    fun `invoke returns success when repository succeeds`() = runBlocking {
        val repo = object : ContentRepository {
            override suspend fun getHomeFeed(): Resource<HomeFeed> = Resource.Success(fakeFeed)
            override suspend fun getContentDetails(contentId: String): Resource<ContentItem> =
                Resource.Success(fakeHero)
        }

        val useCase = GetHomeFeedUseCase(repo)
        val result = useCase()

        assertTrue(result is Resource.Success)
        assertEquals(fakeFeed, (result as Resource.Success).data)
    }

    @Test
    fun `invoke returns error when repository fails`() = runBlocking {
        val repo = object : ContentRepository {
            override suspend fun getHomeFeed(): Resource<HomeFeed> =
                Resource.Error("Network failure")
            override suspend fun getContentDetails(contentId: String): Resource<ContentItem> =
                Resource.Error("Network failure")
        }

        val useCase = GetHomeFeedUseCase(repo)
        val result = useCase()

        assertTrue(result is Resource.Error)
        assertEquals("Network failure", (result as Resource.Error).message)
    }
}
