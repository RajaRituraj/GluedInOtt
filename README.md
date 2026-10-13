# GluedIn OTT Android Application

A modern, high-performance OTT-style Android application built using **Kotlin**, **XML-based UI**, **MVVM**, **Clean Architecture**, and **AndroidX Media3 / ExoPlayer**.

---

## 📱 Features & Highlights

- **Hero Banner:** Displays featured OTT content with high-resolution poster backdrop, dark gradient scrim, badge, title, metadata (duration, release year, IMDb-style rating, genre), short synopsis, and CTA buttons (*"Watch Now"* and *"More Info"*).
- **Horizontal Content Rails:** Horizontally scrolling rails (*Trending Now*, *Recommended for You*, *Popular in Action & Thriller*) with card thumbnails, titles, badges (*4K*, *Top 10*), and rating badges using nested `RecyclerView` with recycled view pools.
- **Content Detail Screen:** Full content synopsis, backdrop banner, director/cast credits, and prominent *"Play"* button.
- **Media3 / ExoPlayer Integration:** Smooth playback for MP4 and HLS streams with standard controls (Play, Pause, Seek forward 10s, Rewind 10s, progress seekbar, and timestamps).
- **Decoupled Player Event Pipeline:** Telemetry & playback analytics pipeline adhering to strict separation of concerns:
  $$\text{Player} \longrightarrow \text{PlayerEventHandler} \longrightarrow \text{ViewModel} \longrightarrow \text{AnalyticsRepository}$$
  Handles 7 core events: `PLAYER_READY`, `PLAYER_STARTED`, `PLAYER_PAUSED`, `PLAYER_SEEKED`, `BUFFERING_STARTED / BUFFERING_ENDED`, `PLAYER_COMPLETED`, and `PLAYER_ERROR`.
  Also displays real-time telemetry badge overlay during playback.
- **Application State Handling:** Robust representation of `Loading`, `Success`, `Empty`, and `Error` states with one-tap Retry functionality.
- **API-Ready Architecture:** Clean separation where the data source currently parses mock JSON with simulated latency (600ms), and can be swapped with Retrofit/Ktor without touching presentation, domain, or repository contracts.

---

## 🏛️ Architecture Overview

The project is structured according to **Clean Architecture** principles and **MVVM (Model-View-ViewModel)**:

```
UI (Activity/Fragment + ViewBinding + Adapters)
       ↕
  ViewModel (StateFlow + CoroutineScope)
       ↕
    Use Cases (Domain Business Logic)
       ↕
Repository Interface (Domain Layer Contract)
       ↕
Repository Implementation (Data Layer)
       ↕
Remote Data Source (Mock JSON / Retrofit / Network)
```

### 1. Presentation Layer (`com.gluedin.ott.presentation`)
- **HomeActivity & HomeViewModel:** Manages the home feed UI state (`UiState.Loading`, `UiState.Success`, `UiState.Error`, `UiState.Empty`), binds the Hero Banner, and dispatches clicks to navigation.
- **ContentDetailActivity & ContentDetailViewModel:** Presents rich metadata, synopsis, cast/crew details, and triggers video playback.
- **PlayerActivity & PlayerViewModel:** Manages ExoPlayer lifecycle, surface rendering, controls overlay, and receives normalized player events.
- **Adapters:** `ContentRailsAdapter` (vertical category rows) & `ContentCardAdapter` (horizontal cards with DiffUtil).

### 2. Domain Layer (`com.gluedin.ott.domain`)
- **Models:** Pure Kotlin business models (`ContentItem`, `ContentRail`, `HomeFeed`, `PlayerEvent`, `Resource`). No Android framework dependencies.
- **Repository Contracts:** `ContentRepository`, `AnalyticsRepository`.
- **Use Cases:**
  - `GetHomeFeedUseCase`
  - `GetContentDetailsUseCase`
  - `LogPlayerEventUseCase`

### 3. Data Layer (`com.gluedin.ott.data`)
- **DTOs:** Network models (`HomeResponseDto`, `ContentRailDto`, `ContentDto`).
- **Mappers:** `ContentMapper` safely transforming DTOs into domain models with null-safety and default values.
- **Data Sources:** `ContentRemoteDataSource` (interface) and `MockContentRemoteDataSource` (reads `mock_home_feed.json` from assets and simulates network latency).
- **Repositories:** `ContentRepositoryImpl` and `AnalyticsRepositoryImpl`.

### 4. Player & Telemetry Pipeline (`com.gluedin.ott.player`)
- **PlayerEventHandler:** Implements `Player.Listener`, intercepts playback state transitions, seek discontinuities, buffering intervals, and errors, translating them into immutable `PlayerEvent` domain objects.
- Dispatched through `PlayerViewModel` into `LogPlayerEventUseCase` -> `AnalyticsRepositoryImpl` which outputs structured logs (`OTT_ANALYTICS`) and exposes a live event stream.

---

## 📁 Project Directory Structure

```
com.gluedin.ott
├── data
│   ├── datasource
│   │   ├── ContentRemoteDataSource.kt
│   │   └── MockContentRemoteDataSource.kt
│   ├── dto
│   │   └── HomeResponseDto.kt (HomeResponseDto, ContentRailDto, ContentDto)
│   ├── mapper
│   │   └── ContentMapper.kt
│   └── repository
│       ├── AnalyticsRepositoryImpl.kt
│       └── ContentRepositoryImpl.kt
├── di
│   └── AppContainer.kt
├── domain
│   ├── model
│   │   ├── ContentItem.kt
│   │   ├── ContentRail.kt
│   │   ├── HomeFeed.kt
│   │   ├── PlayerEvent.kt
│   │   └── Resource.kt
│   ├── repository
│   │   ├── AnalyticsRepository.kt
│   │   └── ContentRepository.kt
│   └── usecase
│       ├── GetContentDetailsUseCase.kt
│       ├── GetHomeFeedUseCase.kt
│       └── LogPlayerEventUseCase.kt
├── player
│   └── PlayerEventHandler.kt
├── presentation
│   ├── base
│   │   └── UiState.kt
│   ├── detail
│   │   ├── ContentDetailActivity.kt
│   │   └── ContentDetailViewModel.kt
│   ├── home
│   │   ├── adapter
│   │   │   ├── ContentCardAdapter.kt
│   │   │   └── ContentRailsAdapter.kt
│   │   ├── HomeActivity.kt
│   │   └── HomeViewModel.kt
│   └── player
│       ├── PlayerActivity.kt
│       └── PlayerViewModel.kt
└── OttApplication.kt
```

---

## 🛠️ Libraries Used

| Library | Version | Purpose |
| :--- | :--- | :--- |
| **AndroidX Media3 ExoPlayer** | `1.5.1` | Video playback engine, buffering, and codecs |
| **AndroidX Media3 UI** | `1.5.1` | PlayerView, TimeBar, and playback controls |
| **Coil** | `2.7.0` | Asynchronous image loading, caching, and crossfades |
| **AndroidX Lifecycle & ViewModel** | `2.8.7` | MVVM state persistence across lifecycle and configuration changes |
| **Kotlin Coroutines & Flow** | `1.9.0` | Asynchronous programming and reactive StateFlow streams |
| **Google Gson** | `2.11.0` | Type-safe JSON serialization/deserialization |
| **Material Components** | `1.12.0` | Cards, buttons, theme styling |
| **JUnit 4** | `4.13.2` | Unit testing |

---

## 🔌 Replacing Mock Data Source with Real API (Retrofit)

To transition this application from static mock JSON to a live production backend, only **one class** needs to be added:

1. Define Retrofit service interface:
   ```kotlin
   interface OttApiService {
       @GET("v1/home/feed")
       suspend fun getHomeFeed(): HomeResponseDto
       
       @GET("v1/content/{id}")
       suspend fun getContentDetails(@Path("id") id: String): ContentDto
   }
   ```
2. Implement `ContentRemoteDataSource`:
   ```kotlin
   class RetrofitContentRemoteDataSource(
       private val apiService: OttApiService
   ) : ContentRemoteDataSource {
       override suspend fun fetchHomeFeed(): HomeResponseDto = apiService.getHomeFeed()
       override suspend fun fetchContentById(id: String): ContentDto? = apiService.getContentDetails(id)
   }
   ```
3. Update `AppContainer.kt`:
   ```kotlin
   val remoteDataSource: ContentRemoteDataSource by lazy {
       RetrofitContentRemoteDataSource(retrofitService)
   }
   ```
**Zero changes are required in ViewModels, UseCases, Domain Models, or Presentation Layouts!**

---

## 🚀 How to Build and Run

### Prerequisites
- Android Studio Ladybug / Meerkat or Command Line Tools
- JDK 17 or JDK 21
- Android SDK Platform 34+

### Command Line
1. Clone or extract the repository:
   ```bash
   cd GluedInOtt
   ```
2. Run Unit Tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```
3. Assemble Debug APK:
   ```bash
   ./gradlew assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

4. Install to connected device or emulator:
   ```bash
   ./gradlew installDebug
   ```

---

## ⚠️ Known Limitations & Future Enhancements

- **Offline Caching:** Currently, `ContentRepositoryImpl` fetches in-memory/remote. A Room database cache could be introduced for full offline playback and caching.
- **DRM Playback:** Widevine DRM license integration can be added with Media3 `DrmConfiguration` for protected studio streams.
- **Picture-in-Picture (PiP):** PlayerActivity can easily be configured to support Android PiP mode on supported devices.
