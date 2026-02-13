# CLAUDE.md — Play With AI Android

## Project Overview

**Play With AI** is a showcase Android app demonstrating modern Android engineering. It features AI chat (Gemini API), smart prompt history with Firebase sync, and a map exploration feature for finding/filtering vehicles and calculating optimal routes. Built with Jetpack Compose, Clean Architecture, and Hilt DI.

## Build & Run

```bash
# Prerequisites: JDK 17, Android SDK 36
# API keys required in local.properties:
#   MAPS_API_KEY=<your-key>
#   GEMINI_API_KEY_DEBUG=<your-key>

./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install on connected device
./gradlew testDebugUnitTest      # Run unit tests
./gradlew detekt                 # Static analysis (Detekt)
./gradlew lintDebug              # Android lint
```

## SDK Versions

- **Min SDK:** 31 (Android 12)
- **Target/Compile SDK:** 36
- **JVM Target:** Java 17
- **Kotlin:** 2.3.10, **AGP:** 9.0

## Module Structure

```
:app                    → Application entry point, navigation, main activity
:core-domain            → Pure Kotlin: domain models, repository interfaces, use cases
:core-data              → Repository implementations, Room DB, Retrofit, Firebase, DI modules
:core-theme             → Design system: colors, typography ("SoFa" design language)
:core-ui                → Reusable Compose UI components
:feature:chat           → AI chat screen, ViewModel, UI states
:feature:map            → Map/vehicle screen, ViewModel, UI states
```

Dependencies flow: `feature → core-domain ← core-data`, `feature → core-ui → core-theme`

## Architecture

- **Clean Architecture** with strict layer separation (domain → data → presentation)
- **MVVM** with ViewModels managing UI state via `StateFlow`
- **Repository pattern** — interfaces in `:core-domain`, implementations in `:core-data`
- **Use cases** — one class per operation (e.g., `GenerateContentUseCase`, `GetMapItemsUseCase`)
- **Hilt** for dependency injection across all modules
- **Compose** with `@Immutable` UI states and `PersistentList`/`PersistentSet` for stability

### Key Packages

```
se.onemanstudio.playaroundwithai.core.domain.feature.{chat,auth,map}
se.onemanstudio.playaroundwithai.core.data.feature.{chat,auth,map}.{di,local,remote,mappers}
se.onemanstudio.playaroundwithai.feature.{chat,maps}
```

### Patterns & Conventions

- **UI states** are sealed interfaces/immutable data classes (e.g., `ChatUiState.Initial | Loading | Success | Error`)
- **Mappers** use `toDomain()` / `toEntity()` naming
- **Use cases** are suffixed with `UseCase`
- **No hardcoded strings** — all user-facing text in per-module `strings.xml`
- **Max line length:** 160 characters (enforced by Detekt)
- **Detekt** runs with `maxIssues: 0` — build fails on any code quality issue

## Key Dependencies

| Category   | Library                                        |
|------------|------------------------------------------------|
| UI         | Jetpack Compose, Material3, Compose Navigation |
| DI         | Hilt 2.59.1                                    |
| Network    | Retrofit 3.0.0, OkHttp 5.3.2, Gson             |
| Database   | Room 2.8.4                                     |
| Firebase   | Firestore, Firebase Auth                       |
| Async      | Coroutines 1.10.2                              |
| Images     | Coil 2.7.0                                     |
| Maps       | Google Maps Compose 8.1.0                      |
| Logging    | Timber                                         |
| Background | WorkManager                                    |

All versions managed in `gradle/libs.versions.toml`.

## Testing

- **Framework:** JUnit 4 + MockK for mocking
- **Assertions:** Google Truth
- **Coroutines:** `MainCoroutineRule` test helper
- Tests live in each module's `src/test/` directory
- Run with: `./gradlew testDebugUnitTest`

## CI

GitHub Actions workflow (`.github/workflows/android_ci.yml`):
1. Detekt → 2. Lint → 3. Build debug APK → 4. Unit tests → uploads test reports on failure

## API Keys & Secrets

The app requires two API keys to function. Both are stored in `local.properties` (gitignored) at the project root.

### 1. Gemini API Key (required for AI chat)

1. Go to [Google AI Studio](https://aistudio.google.com/apikey)
2. Sign in with your Google account
3. Click **Create API Key**
4. Select an existing Google Cloud project or create a new one
5. Copy the generated key
6. Add it to `local.properties`:
   ```properties
   GEMINI_API_KEY_DEBUG=your-gemini-api-key-here
   ```
7. For release builds, also add:
   ```properties
   GEMINI_API_KEY_RELEASE=your-gemini-api-key-here
   ```

The key is read in `core-data/build.gradle.kts` and exposed via `BuildConfig.GEMINI_API_KEY`. It is injected at runtime through Hilt using the `@GeminiApiKey` qualifier.

### 2. Google Maps API Key (required for map feature)

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Navigate to **APIs & Services → Library**
4. Enable the **Maps SDK for Android**
5. Go to **APIs & Services → Credentials**
6. Click **Create Credentials → API Key**
7. (Recommended) Restrict the key:
   - Under **Application restrictions**, select **Android apps**
   - Add your app's package name (`se.onemanstudio.playaroundwithai`) and SHA-1 fingerprint
   - Under **API restrictions**, select **Maps SDK for Android**
8. Copy the key and add it to `local.properties`:
   ```properties
   MAPS_API_KEY=your-maps-api-key-here
   ```

The key is read in `app/build.gradle.kts` and injected into the Android manifest via `manifestPlaceholders`.

### Configuration summary

Your `local.properties` file should look like:
```properties
# SDK location (auto-generated by Android Studio)
sdk.dir=/path/to/Android/sdk

# API Keys
GEMINI_API_KEY_DEBUG=your-gemini-api-key
GEMINI_API_KEY_RELEASE=your-gemini-api-key
MAPS_API_KEY=your-maps-api-key
```

### CI

- CI injects these via GitHub Secrets (environment variables with the same names)
- Accessed in code via `BuildConfig` fields
- Custom Hilt qualifiers: `@GeminiApiKey`, `@BaseUrl`, `@LoggingLevel`

## Architecture Diagram — API Endpoints, Services & Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                                 │
│                                                                                 │
│  ┌──────────────────────────────┐       ┌──────────────────────────────┐        │
│  │       :feature:chat          │       │        :feature:map          │        │
│  │                              │       │                              │        │
│  │  ChatScreen (Compose)        │       │  MapScreen (Compose)         │        │
│  │       │                      │       │       │                      │        │
│  │       ▼                      │       │       ▼                      │        │
│  │  ChatViewModel               │       │  MapViewModel                │        │
│  │   │  StateFlow<ChatUiState>  │       │   │  StateFlow<MapUiState>   │        │
│  │   │   ├─ Initial             │       │   │   (locations, filters,   │        │
│  │   │   ├─ Loading             │       │   │    route, metrics)       │        │
│  │   │   ├─ Success             │       │   │                          │        │
│  │   │   └─ Error               │       │   │                          │        │
│  └───┼──────────────────────────┘       └───┼──────────────────────────┘        │
│      │ Uses 5 use cases                     │ Uses 1 use case                   │
└──────┼──────────────────────────────────────┼───────────────────────────────────┘
       │                                      │
       ▼                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                DOMAIN LAYER                                     │
│                              :core-domain                                       │
│                                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐  │
│  │ GenerateContentUC    │  │ GetPromptHistoryUC   │  │ GetMapItemsUC         │  │
│  │ GenerateSuggestionsUC│  │ SavePromptUC         │  │                       │  │
│  │                      │  │ IsSyncingUC          │  │                       │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └───────────┬───────────┘  │
│             │                         │                          │              │
│             ▼                         ▼                          ▼              │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐  │
│  │ GeminiRepository     │  │ PromptRepository     │  │ MapRepository         │  │
│  │ (interface)          │  │ (interface)          │  │ (interface)           │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └───────────┬───────────┘  │
│             │                         │                          │              │
│             │          ┌──────────────┤                          │              │
│             │          │  AuthRepository (interface)             │              │
│             │          │              │                          │              │
└─────────────┼──────────┼──────────────┼──────────────────────────┼──────────────┘
              │          │              │                          │
              ▼          ▼              ▼                          ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                 DATA LAYER                                      │
│                               :core-data                                        │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                        Repository Implementations                       │    │
│  │                                                                         │    │
│  │  GeminiRepositoryImpl   PromptRepositoryImpl   MapRepositoryImpl        │    │
│  │         │                  │          │              │                  │    │
│  │         │                  │          │              │                  │    │
│  │  AuthRepositoryImpl        │          │              │                  │    │
│  │         │                  │          │              │                  │    │
│  └─────────┼──────────────────┼──────────┼──────────────┼──────────────────┘    │
│            │                  │          │              │                       │
│            ▼                  ▼          │              ▼                       │
│  ┌──────────────────────────────────┐   │    ┌─────────────────────────┐        │
│  │       REMOTE DATA SOURCES        │   │    │   FakeMapApiService     │        │
│  │                                  │   │    │   (Mock data generator) │        │
│  │  ┌────────────────────────────┐  │   │    │   Simulates 1.5s delay  │        │
│  │  │     GeminiApiService       │  │   │    │   Generates random      │        │
│  │  │     (Retrofit)             │  │   │    │   vehicle locations     │        │
│  │  │                            │  │   │    └─────────────────────────┘        │
│  │  │  POST v1beta/models/       │  │   │                                       │
│  │  │  gemini-3-flash-preview    │  │   │                                       │
│  │  │  :generateContent          │  │   │                                       │
│  │  └────────────┬───────────────┘  │   │                                       │
│  │               │                  │   │                                       │
│  │  ┌────────────────────────────┐  │   │                                       │
│  │  │  FirestoreDataSource       │  │   │                                       │
│  │  │                            │  │   │                                       │
│  │  │  Collection: "prompts"     │  │   │                                       │
│  │  │  Doc: {text, timestamp,    │  │   │                                       │
│  │  │        userId}             │  │   │                                       │
│  │  └────────────┬───────────────┘  │   │                                       │
│  │               │                  │   │                                       │
│  │  ┌────────────────────────────┐  │   │                                       │
│  │  │  Firebase Auth             │  │   │                                       │
│  │  │  signInAnonymously()       │  │   │                                       │
│  │  └────────────┬───────────────┘  │   │                                       │
│  └───────────────┼──────────────────┘   │                                       │
│                  │                      ▼                                       │
│                  │    ┌──────────────────────────────────┐                      │
│                  │    │       LOCAL DATA SOURCE          │                      │
│                  │    │                                  │                      │
│                  │    │   Room DB: "play_around_with_ai_db"                     │
│                  │    │   Table: "prompt_history"        │                      │
│                  │    │   ┌─────────────────────────┐    │                      │
│                  │    │   │ id          (PK, auto)  │    │                      │
│                  │    │   │ text        (String)    │    │                      │
│                  │    │   │ timestamp   (Long)      │    │                      │
│                  │    │   │ syncStatus  (Enum)      │    │                      │
│                  │    │   │  ├─ Pending             │    │                      │
│                  │    │   │  └─ Synced              │    │                      │
│                  │    │   └─────────────────────────┘    │                      │
│                  │    │                                  │                      │
│                  │    │   PromptsHistoryDao:             │                      │
│                  │    │    savePrompt()                  │                      │
│                  │    │    getPromptHistory() → Flow     │                      │
│                  │    │    getPromptsBySyncStatus()      │                      │
│                  │    │    updateSyncStatus()            │                      │
│                  │    └──────────────────────────────────┘                      │
│                  │                      ▲                                       │
│                  │                      │                                       │
│                  │    ┌─────────────────┴────────────────┐                      │
│                  │    │       BACKGROUND SYNC            │                      │
│                  │    │                                  │                      │
│                  │    │   SyncWorker (WorkManager)       │                      │
│                  │    │    1. Get Pending prompts (Room) │                      │
│                  │    │    2. Upload to Firestore ───────┼──┐                   │
│                  │    │    3. Mark as Synced (Room)      │  │                   │
│                  │    │                                  │  │                   │
│                  │    │   Constraints:                   │  │                   │
│                  │    │    - Requires network            │  │                   │
│                  │    │   Policy: REPLACE existing       │  │                   │
│                  │    └──────────────────────────────────┘  │                   │
│                  │                                          │                   │
└──────────────────┼──────────────────────────────────────────┼───────────────────┘
                   │                                          │
                   ▼                                          ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            EXTERNAL SERVICES                                    │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │  Google Gemini API                                                       │   │
│  │  Base: https://generativelanguage.googleapis.com/                        │   │
│  │  Endpoint: POST /v1beta/models/gemini-3-flash-preview:generateContent    │   │
│  │  Auth: ?key={GEMINI_API_KEY} (query param via AuthenticationInterceptor) │   │
│  │                                                                          │   │
│  │  Request:  { contents: [{ parts: [{ text, inlineData? }] }] }            │   │
│  │  Response: { candidates: [{ content: { parts: [{ text }] } }] }          │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │  Firebase Firestore                                                      │   │
│  │  Collection: "prompts"                                                   │   │
│  │  Document: { text: String, timestamp: Long, userId: String }             │   │
│  │  Operations: add (write only, no reads from Firestore)                   │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │  Firebase Auth                                                           │   │
│  │  Method: Anonymous sign-in                                               │   │
│  │  Purpose: Identify user for Firestore documents                          │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
│  ┌──────────────────────────────────────────────────────────────────────────┐   │
│  │  Google Maps SDK                                                         │   │
│  │  Auth: MAPS_API_KEY (manifest placeholder)                               │   │
│  │  Services: Map tiles, markers, polylines, camera                         │   │
│  │  Location: FusedLocationProviderClient (last known location)             │   │
│  └──────────────────────────────────────────────────────────────────────────┘   │
│                                                                                 │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Chat Feature — Request/Response Flow

```
User types prompt (+ optional image/document)
  │
  ▼
ChatViewModel.generateContent(prompt, imageUri?, documentUri?)
  │
  ├─ Image? → ContentResolver → decode → scale to max 768px → JPEG @ 77% → Base64
  ├─ Document? → ContentResolver → read text content
  │
  ▼
GenerateContentUseCase.invoke(prompt, imageBytes?, fileText?, analysisType?)
  │
  ▼
GeminiRepositoryImpl.generateContent()
  │
  ├─ Prepend system instruction ("AI Overlord" persona, max 42 words)
  ├─ Append file content if present
  ├─ Build GeminiRequest { contents: [{ parts: [{ text }, { inlineData? }] }] }
  │
  ▼
POST https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent
     ?key={GEMINI_API_KEY}
  │
  ├── OkHttp Pipeline:
  │    AuthenticationInterceptor → adds ?key= param
  │    HttpLoggingInterceptor   → BODY (debug) / NONE (release)
  │    Timeouts: 30s connect/read/write
  │
  ▼
GeminiResponse → candidates[0].content.parts[0].text → Result<String>
  │
  ▼
ChatViewModel receives Result
  ├─ Success → ChatUiState.Success(response)
  └─ Failure → ChatUiState.Error(message)

  (In parallel)
  │
  ▼
SavePromptUseCase.invoke(prompt)
  │
  ▼
PromptRepositoryImpl.savePrompt()
  ├─ Set syncStatus = Pending
  ├─ INSERT INTO prompt_history (Room)
  └─ Enqueue SyncWorker (WorkManager, requires network)
       │
       ▼ (when network available)
     SyncWorker.doWork()
       ├─ SELECT * FROM prompt_history WHERE syncStatus = 'Pending'
       ├─ For each: Firestore.collection("prompts").add({text, timestamp, userId})
       ├─ UPDATE prompt_history SET syncStatus = 'Synced' WHERE id = ?
       └─ Return success / retry
```

### Map Feature — Data Flow

```
MapScreen launched
  │
  ▼
MapViewModel.loadMapData()
  │
  ▼
GetMapItemsUseCase.invoke(count = 30)
  │
  ▼
MapRepositoryImpl.getMapItems(30)
  │
  ▼
FakeMapApiService.getMapItems(30)       ← Mock implementation
  ├─ delay(1500ms)                        (no real API yet)
  ├─ Generate 30 random vehicles
  │   ├─ Type: SCOOTER or BICYCLE
  │   ├─ Position: random lat/lng near Stockholm
  │   ├─ Battery: random level
  │   └─ Nickname: random name
  └─ Return List<MapItemDto>
  │
  ▼
Map DTO → Domain (MapItem) → UI Model (MapItemUiModel)
  │
  ▼
MapUiState updated
  ├─ allLocations: PersistentList<MapItemUiModel>
  ├─ visibleLocations: PersistentList<MapItemUiModel>   (filtered subset)
  ├─ activeFilters: PersistentSet<VehicleType>
  ├─ optimalRoute: PersistentList<LatLng>?
  └─ metrics: RouteMetrics?
  │
  ▼
GoogleMap renders markers + polylines
  │
  ├─ User toggles filter → MapViewModel.toggleFilter(type)
  │   └─ Recompute visibleLocations from allLocations
  │
  └─ User taps "Calculate Route" → MapViewModel.calculateOptimalRoute(userLoc)
      ├─ Get user location via FusedLocationProviderClient
      ├─ Compute all permutations of selected locations
      ├─ Find minimum-distance path (brute force TSP)
      └─ Update optimalRoute + animate camera to bounds
```
