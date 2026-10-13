# GluedIn OTT Android Application

A modern, high-performance OTT-style Android application built using **Kotlin**, **XML-based UI**, **MVVM**, **Clean Architecture**, and **AndroidX Media3 / ExoPlayer**.

---

## 📋 Submission Checklist (Section 12 Compliance)

| Requirement | Status | Description & References |
| :--- | :---: | :--- |
| **1. Git Repository** | ✅ Ready | Complete source code committed on branch `main` with atomic commit history. |
| **2. Testing APK** | ✅ Ready | Standalone test APK located at [`app-debug.apk`](./app-debug.apk) (10.48 MB) and `app/build/outputs/apk/debug/app-debug.apk`. |
| **3. Steps to build and run** | ✅ Documented | Full Gradle CLI and Android Studio steps provided in [Steps to Build and Run](#-steps-to-build-and-run). |
| **3. Architecture overview** | ✅ Documented | 3-tier Clean Architecture (Presentation, Domain, Data) detailed in [Architecture Overview](#️-architecture-overview). |
| **3. Project/module structure** | ✅ Documented | Full package tree mapped in [Project Directory Structure](#-project-directory-structure). |
| **3. Libraries used** | ✅ Documented | All libraries, exact versions, and rationale listed in [Libraries Used](#-libraries-used). |
| **3. Key technical/architectural decisions** | ✅ Documented | Documented in [Key Technical & Architectural Decisions](#-key-technical--architectural-decisions). |
| **3. Player event handling approach** | ✅ Documented | Documented in [Player Event Handling Approach](#-player-event-handling-approach). |
| **3. How mock data source is replaced with real API** | ✅ Documented | Ready-to-use Retrofit implementation detailed in [Replacing Mock Data Source with Real API](#-replacing-mock-data-source-with-real-api-retrofit). |
| **3. Known limitations, if any** | ✅ Documented | Detailed in [Known Limitations & Future Enhancements](#-known-limitations--future-enhancements). |

---

## 📱 Features & Highlights

- **Hero Banner:** Displays featured OTT content with high-resolution poster backdrop, dark gradient scrim, badge, title, metadata (duration, release year, IMDb-style rating, genre), short synopsis, and CTA buttons (*"Watch Now"* and *"More Info"*).
- **Horizontal Content Rails:** Horizontally scrolling rails (*Trending Now*, *Recommended for You*, *Popular in Action & Thriller*) with card thumbnails, titles, badges (*4K*, *Top 10*), and rating badges using nested `RecyclerView` with recycled view pools.
- **Content Detail Screen:** Full content synopsis, backdrop banner, director/cast credits, and prominent *"Play"* button.
- **Media3 / ExoPlayer Integration:** Smooth playback for MP4 and HLS streams with custom transparent controls (Play/Pause centered toggle, Seek forward 10s, Rewind 10s, progress seekbar, and timestamps).
- **Auto-Hiding Transparent Controls:** Cinematic top bar (Back button, Title, Telemetry badge) and bottom bar that automatically and synchronously fade in and out when tapping the screen.
- **Decoupled Player Event Pipeline:** Telemetry & playback analytics pipeline adhering to strict separation of concerns:
  $$\text{Player} \longrightarrow \text{PlayerEventHandler} \longrightarrow \text{ViewModel} \longrightarrow \text{AnalyticsRepository}$$
  Handles 7 core events: `PLAYER_READY`, `PLAYER_STARTED`, `PLAYER_PAUSED`, `PLAYER_SEEKED`, `BUFFERING_STARTED / BUFFERING_ENDED`, `PLAYER_COMPLETED`, and `PLAYER_ERROR`.
  Also displays real-time telemetry badge overlay during playback.
- **Application State Handling:** Robust representation of `Loading`, `Success`, `Empty`, and `Error` states with one-tap Retry functionality.
- **API-Ready Architecture:** Clean separation where the data source parses mock JSON with simulated latency (600ms), and can be swapped with Retrofit without touching presentation, domain, or repository contracts.

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

---

## 💡 Key Technical & Architectural Decisions

1. **Clean Architecture Separation of Concerns:**
   - The domain layer contains zero Android framework dependencies, making use cases and models 100% unit testable on the JVM without emulators or Robolectric.
2. **XML UI over Jetpack Compose:**
   - Adheres strictly to the assignment requirement (*"Jetpack Compose should not be used for this assignment"*). Uses ViewBinding for type-safe view lookups without boilerplate `findViewById`.
3. **Manual Dependency Injection Container (`AppContainer`):**
   - Implemented an interface-driven dependency container (`AppContainer`) rather than heavy annotation processors (Hilt/KSP). This guarantees zero annotation-processor compatibility issues, lightning-fast compilation, and easy mocking in tests.
4. **Resilient HTTP Stack in Media3 ExoPlayer:**
   - Configured `DefaultHttpDataSource.Factory` with a mobile browser `User-Agent`, cross-protocol redirects, and 15s connection timeouts to prevent CDN 403 Forbidden bot protection errors.
5. **Shared RecycledViewPool for Rails:**
   - Created a shared `RecyclerView.RecycledViewPool` in `ContentRailsAdapter` to reuse view holders across horizontal content rails, minimizing view inflation overhead and ensuring 60fps scrolling.

---

## 🎯 Player Event Handling Approach

Player events are decoupled from Activities and UI fragments through a unidirectional pipeline:

```
Media3 ExoPlayer
       ↓ (Player.Listener)
PlayerEventHandler
       ↓ (Emits sealed PlayerEvent)
PlayerViewModel
       ↓ (Dispatches to UseCase)
LogPlayerEventUseCase
       ↓
AnalyticsRepositoryImpl
       ↓ (Outputs formatted logs & emits StateFlow)
OTT_ANALYTICS logcat & Telemetry UI Badge
```

### Handled Player Events:
1. **`PLAYER_READY`**: Triggered when ExoPlayer prepares media and becomes ready for immediate rendering.
2. **`PLAYER_STARTED`**: Fired when playback begins or resumes (`isPlaying == true`).
3. **`PLAYER_PAUSED`**: Fired when playback pauses (`isPlaying == false`).
4. **`PLAYER_SEEKED`**: Intercepted via `onPositionDiscontinuity` when a seek discontinuity occurs, logging `fromPositionMs` and `toPositionMs`.
5. **`BUFFERING_STARTED` / `BUFFERING_ENDED`**: Emitted when playback state switches to `STATE_BUFFERING` and back to `STATE_READY`.
6. **`PLAYER_COMPLETED`**: Emitted when video playback reaches `STATE_ENDED`.
7. **`PLAYER_ERROR`**: Intercepted via `onPlayerError`, capturing the error code name and message.

All events are logged with structured tags to logcat (`OTT_ANALYTICS`) and displayed live in the on-screen telemetry chip overlay.

---

## 📁 Project Directory Structure

```
com.gluedin.ott
├── data
│   ├── datasource
│   │   ├── ContentRemoteDataSource.kt
│   │   ├── MockContentRemoteDataSource.kt
│   │   ├── OttApiService.kt (Retrofit contract)
│   │   └── RetrofitContentRemoteDataSource.kt (Production implementation)
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

| Library | Version | Purpose | Rationale |
| :--- | :--- | :--- | :--- |
| **AndroidX Media3 ExoPlayer** | `1.5.1` | Video playback core | Google's modern video playback standard supporting HLS & MP4. |
| **AndroidX Media3 UI** | `1.5.1` | Player controls & TimeBar | Customizable player views and scrubbers. |
| **Coil** | `2.7.0` | Image loading | Lightweight, Kotlin-first image loader with coroutines & memory caching. |
| **AndroidX Lifecycle & ViewModel** | `2.8.7` | MVVM state persistence | Retains UI state across configuration changes (rotation). |
| **Kotlin Coroutines & Flow** | `1.9.0` | Asynchronous operations | Structured concurrency and reactive StateFlow streams. |
| **Google Gson** | `2.11.0` | JSON serialization | Fast, type-safe parsing of mock API responses. |
| **Material Components** | `1.12.0` | Design system | Material3 cards, ripple buttons, and dark styling. |
| **JUnit 4** | `4.13.2` | Unit testing | Fast JVM-based testing of business logic and mappers. |

---

## 🔌 Replacing Mock Data Source with Real API (Retrofit)

To transition this application from static mock JSON to a live production backend, only **one class** needs to be swapped in `AppContainer.kt`:

1. Retrofit service contract ([OttApiService.kt](file:///d:/TaskGluedin/GluedInOtt/app/src/main/java/com/gluedin/ott/data/datasource/OttApiService.kt)):
   ```kotlin
   interface OttApiService {
       @GET("v1/home/feed")
       suspend fun getHomeFeed(): HomeResponseDto
       
       @GET("v1/content/{id}")
       suspend fun getContentDetails(@Path("id") id: String): ContentDto
   }
   ```
2. Production data source ([RetrofitContentRemoteDataSource.kt](file:///d:/TaskGluedin/GluedInOtt/app/src/main/java/com/gluedin/ott/data/datasource/RetrofitContentRemoteDataSource.kt)):
   ```kotlin
   class RetrofitContentRemoteDataSource(
       private val apiService: OttApiService
   ) : ContentRemoteDataSource {
       override suspend fun fetchHomeFeed(): HomeResponseDto = apiService.getHomeFeed()
       override suspend fun fetchContentById(id: String): ContentDto? = apiService.getContentDetails(id)
   }
   ```
3. Update [AppContainer.kt](file:///d:/TaskGluedin/GluedInOtt/app/src/main/java/com/gluedin/ott/di/AppContainer.kt):
   ```kotlin
   val remoteDataSource: ContentRemoteDataSource by lazy {
       RetrofitContentRemoteDataSource(retrofitService)
   }
   ```
**Zero changes are required in ViewModels, Use Cases, Domain Models, or Presentation Layouts!**

---

## 🚀 Steps to Build and Run

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

- **Offline Caching:** Currently, `ContentRepositoryImpl` fetches in-memory/remote. A Room database cache could be introduced for full offline caching.
- **DRM Playback:** Widevine DRM license integration can be added with Media3 `DrmConfiguration` for protected studio streams.
- **Picture-in-Picture (PiP):** PlayerActivity includes PiP lifecycle hooks that can be expanded with custom remote actions.
