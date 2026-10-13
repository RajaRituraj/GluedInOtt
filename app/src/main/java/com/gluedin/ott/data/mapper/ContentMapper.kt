package com.gluedin.ott.data.mapper

import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.ContentRailDto
import com.gluedin.ott.data.dto.HomeResponseDto
import com.gluedin.ott.domain.model.ContentItem
import com.gluedin.ott.domain.model.ContentRail
import com.gluedin.ott.domain.model.HomeFeed

object ContentMapper {

    fun toDomain(dto: ContentDto): ContentItem {
        return ContentItem(
            id = dto.id.orEmpty(),
            title = dto.title.orEmpty(),
            description = dto.description.orEmpty(),
            thumbnailUrl = dto.thumbnailUrl.orEmpty(),
            bannerUrl = dto.bannerUrl ?: dto.thumbnailUrl.orEmpty(),
            videoUrl = dto.videoUrl.orEmpty(),
            duration = dto.duration.orEmpty(),
            releaseYear = dto.releaseYear.orEmpty(),
            rating = dto.rating.orEmpty(),
            badge = dto.badge,
            genre = dto.genre,
            cast = dto.cast,
            director = dto.director
        )
    }

    fun toDomain(railDto: ContentRailDto): ContentRail {
        return ContentRail(
            id = railDto.id.orEmpty(),
            title = railDto.title.orEmpty(),
            items = railDto.items?.map { toDomain(it) }.orEmpty()
        )
    }

    fun toDomain(response: HomeResponseDto): HomeFeed {
        val hero = response.heroBanner?.let { toDomain(it) }
            ?: ContentItem(
                id = "default_hero",
                title = "Featured Content",
                description = "Experience next-generation streaming.",
                thumbnailUrl = "",
                bannerUrl = "",
                videoUrl = "",
                duration = "",
                releaseYear = "",
                rating = ""
            )

        val rails = response.rails?.map { toDomain(it) }.orEmpty()

        return HomeFeed(
            heroBanner = hero,
            rails = rails
        )
    }
}
