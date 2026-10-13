package com.gluedin.ott.data.mapper

import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.ContentRailDto
import com.gluedin.ott.data.dto.HomeResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ContentMapperTest {

    @Test
    fun `toDomain maps ContentDto properly`() {
        val dto = ContentDto(
            id = "c1",
            title = "Test Movie",
            description = "Test Description",
            thumbnailUrl = "https://example.com/thumb.jpg",
            bannerUrl = "https://example.com/banner.jpg",
            videoUrl = "https://example.com/video.mp4",
            duration = "1h 30m",
            releaseYear = "2026",
            rating = "4.8",
            badge = "4K",
            genre = "Action",
            cast = "Actor A",
            director = "Director B"
        )

        val item = ContentMapper.toDomain(dto)

        assertEquals("c1", item.id)
        assertEquals("Test Movie", item.title)
        assertEquals("Test Description", item.description)
        assertEquals("https://example.com/thumb.jpg", item.thumbnailUrl)
        assertEquals("https://example.com/banner.jpg", item.bannerUrl)
        assertEquals("https://example.com/video.mp4", item.videoUrl)
        assertEquals("1h 30m", item.duration)
        assertEquals("2026", item.releaseYear)
        assertEquals("4.8", item.rating)
        assertEquals("4K", item.badge)
        assertEquals("Action", item.genre)
    }

    @Test
    fun `toDomain handles null fields gracefully with safe fallbacks`() {
        val dto = ContentDto(
            id = null,
            title = null,
            description = null,
            thumbnailUrl = null,
            bannerUrl = null,
            videoUrl = null,
            duration = null,
            releaseYear = null,
            rating = null,
            badge = null,
            genre = null,
            cast = null,
            director = null
        )

        val item = ContentMapper.toDomain(dto)

        assertEquals("", item.id)
        assertEquals("", item.title)
        assertEquals("", item.description)
        assertEquals("", item.thumbnailUrl)
        assertEquals("", item.bannerUrl)
        assertEquals("", item.videoUrl)
    }

    @Test
    fun `toDomain maps HomeResponseDto with rails correctly`() {
        val heroDto = ContentDto(
            id = "hero1",
            title = "Hero Movie",
            description = "Epic",
            thumbnailUrl = "thumb",
            bannerUrl = "banner",
            videoUrl = "video",
            duration = "2h",
            releaseYear = "2026",
            rating = "5.0",
            badge = "Exclusive",
            genre = "Sci-Fi",
            cast = null,
            director = null
        )

        val railDto = ContentRailDto(
            id = "rail1",
            title = "Trending",
            items = listOf(heroDto)
        )

        val response = HomeResponseDto(
            status = "success",
            heroBanner = heroDto,
            rails = listOf(railDto)
        )

        val feed = ContentMapper.toDomain(response)

        assertNotNull(feed.heroBanner)
        assertEquals("hero1", feed.heroBanner.id)
        assertEquals(1, feed.rails.size)
        assertEquals("Trending", feed.rails[0].title)
        assertEquals(1, feed.rails[0].items.size)
    }
}
