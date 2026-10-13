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
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Service locator holding app-wide dependencies
class AppContainer(context: Context) {

    val gson: Gson by lazy { Gson() }

    // Retrofit setup for production backend
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.example.com/") // TODO: set live backend endpoint
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val apiService: com.gluedin.ott.data.datasource.OttApiService by lazy {
        retrofit.create(com.gluedin.ott.data.datasource.OttApiService::class.java)
    }

    // Switch to RetrofitContentRemoteDataSource once backend API is deployed
    val remoteDataSource: ContentRemoteDataSource by lazy {
        MockContentRemoteDataSource(context.applicationContext, gson)
        // RetrofitContentRemoteDataSource(apiService)
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
