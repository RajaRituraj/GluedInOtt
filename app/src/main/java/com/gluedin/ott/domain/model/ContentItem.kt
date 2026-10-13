package com.gluedin.ott.domain.model

import java.io.Serializable

data class ContentItem(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val bannerUrl: String,
    val videoUrl: String,
    val duration: String,
    val releaseYear: String,
    val rating: String,
    val badge: String? = null,
    val genre: String? = null,
    val cast: String? = null,
    val director: String? = null
) : Serializable
