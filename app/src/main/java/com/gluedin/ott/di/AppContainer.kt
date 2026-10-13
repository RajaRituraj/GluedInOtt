package com.gluedin.ott.di

import android.content.Context
import com.gluedin.ott.data.datasource.ContentRemoteDataSource
import com.gluedin.ott.data.datasource.MockContentRemoteDataSource
import com.gluedin.ott.data.repository.AnalyticsRepositoryImpl
import com.gluedin.ott.data.repository.ContentRepositoryImpl
import com.gluedin.ott.domain.repository.AnalyticsRepository
import com.gluedin.ott.domain.repository.ContentRepository
import com.gluedin.ott.domain.usecase.GetContentDetailsUseCase
import com.gluedin.ott.domain.usecase.GetHomeFeedUseCase
import com.gluedin.ott.domain.usecase.LogPlayerEventUseCase
import com.google.gson.Gson

/**
 * Dependency container adhering to Dependency Inversion and Clean Architecture.
 * Provides easy mocking and swappable data sources for production vs testing.
 */
class AppContainer(context: Context) {

    val gson: Gson by lazy { Gson() }

    val remoteDataSource: ContentRemoteDataSource by lazy {
        MockContentRemoteDataSource(context.applicationContext, gson)
    }

    val contentRepository: ContentRepository by lazy {
        ContentRepositoryImpl(remoteDataSource)
    }

    val analyticsRepository: AnalyticsRepository by lazy {
        AnalyticsRepositoryImpl()
    }

    val getHomeFeedUseCase: GetHomeFeedUseCase by lazy {
        GetHomeFeedUseCase(contentRepository)
    }

    val getContentDetailsUseCase: GetContentDetailsUseCase by lazy {
        GetContentDetailsUseCase(contentRepository)
    }

    val logPlayerEventUseCase: LogPlayerEventUseCase by lazy {
        LogPlayerEventUseCase(analyticsRepository)
    }
}
