package com.gluedin.ott.domain.model

data class ContentRail(
    val id: String,
    val title: String,
    val items: List<ContentItem>
)
