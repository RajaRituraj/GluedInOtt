package com.gluedin.ott.data.dto

import com.google.gson.annotations.SerializedName

data class ContentDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    @SerializedName("banner_url")
    val bannerUrl: String?,
    @SerializedName("video_url")
    val videoUrl: String?,
    @SerializedName("duration")
    val duration: String?,
    @SerializedName("release_year")
    val releaseYear: String?,
    @SerializedName("rating")
    val rating: String?,
    @SerializedName("badge")
    val badge: String?,
    @SerializedName("genre")
    val genre: String?,
    @SerializedName("cast")
    val cast: String?,
    @SerializedName("director")
    val director: String?
)

data class ContentRailDto(
    @SerializedName("id")
    val id: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("items")
    val items: List<ContentDto>?
)

data class HomeResponseDto(
    @SerializedName("status")
    val status: String?,
    @SerializedName("hero_banner")
    val heroBanner: ContentDto?,
    @SerializedName("rails")
    val rails: List<ContentRailDto>?
)
