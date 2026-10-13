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

/**
 * Dependency container adhering to Dependency Inversion and Clean Architecture.
 * Provides easy mocking and swappable data sources for production vs testing.
 */
class AppContainer(context: Context) {

    val gson: Gson by lazy { Gson() }

    // --- Retrofit Setup (Ready for production) ---
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
            // IMPORTANT: Replace with your actual production API base URL
            .baseUrl("https://api.example.com/") 
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private val apiService: com.gluedin.ott.data.datasource.OttApiService by lazy {
        retrofit.create(com.gluedin.ott.data.datasource.OttApiService::class.java)
    }

    // Toggle between mock and real API by changing this variable.
    // Currently using MockContentRemoteDataSource so the app can run out-of-the-box 
    // without needing a real backend.
    val remoteDataSource: ContentRemoteDataSource by lazy {
        MockContentRemoteDataSource(context.applicationContext, gson)
        // To use real API, uncomment the line below and comment the line above:
        // com.gluedin.ott.data.datasource.RetrofitContentRemoteDataSource(apiService)
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
