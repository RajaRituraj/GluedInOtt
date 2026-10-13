package com.gluedin.ott.data.datasource

import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.HomeResponseDto
import retrofit2.http.GET
import retrofit2.http.Path

interface OttApiService {
    @GET("v1/home/feed")
    suspend fun getHomeFeed(): HomeResponseDto
    
    @GET("v1/content/{id}")
    suspend fun getContentDetails(@Path("id") id: String): ContentDto
}
