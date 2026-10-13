package com.gluedin.ott.data.datasource

import android.content.Context
import com.gluedin.ott.data.dto.ContentDto
import com.gluedin.ott.data.dto.HomeResponseDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

/**
 * Mock implementation of [ContentRemoteDataSource] reading static JSON from app assets.
 * Simulates network latency (500ms) to test loading states realistically.
 */
class MockContentRemoteDataSource(
    private val context: Context,
    private val gson: Gson = Gson(),
    private val simulatedDelayMs: Long = 600L
) : ContentRemoteDataSource {

    private var cachedResponse: HomeResponseDto? = null

    override suspend fun fetchHomeFeed(): HomeResponseDto = withContext(Dispatchers.IO) {
        if (simulatedDelayMs > 0) {
            delay(simulatedDelayMs)
        }

        cachedResponse?.let { return@withContext it }

        context.assets.open("mock_home_feed.json").use { inputStream ->
            InputStreamReader(inputStream).use { reader ->
                val response = gson.fromJson(reader, HomeResponseDto::class.java)
                    ?: throw IllegalStateException("Parsed JSON response is null")
                cachedResponse = response
                response
            }
        }
    }

    override suspend fun fetchContentById(id: String): ContentDto? = withContext(Dispatchers.IO) {
        val feed = fetchHomeFeed()
        if (feed.heroBanner?.id == id) {
            return@withContext feed.heroBanner
        }
        feed.rails?.forEach { rail ->
            rail.items?.firstOrNull { it.id == id }?.let { return@withContext it }
        }
        null
    }
}
