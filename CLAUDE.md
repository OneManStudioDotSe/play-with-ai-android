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
:core:network           → OkHttp, Retrofit, Gson, GeminiApiService, DTOs, interceptor, GeminiModel
:core:auth              → Firebase Auth, AuthRepository (interface+impl), auth use cases, AuthSession
:core:config            → ApiKeyAvailability, ConfigurationModule, qualifier annotations, BuildConfig
:core:theme             → Design system: colors, typography ("SoFa" design language)
:core:ui                → Reusable Compose UI components
:feature:chat           → ALL chat: domain + data + presentation (Room, Firestore, WorkManager, sync)
:feature:map            → ALL map: domain + data + presentation (fake API, route calculation)
```

Dependencies flow: `feature → core:network + core:config + core:auth`, `feature → core:ui → core:theme`

## Architecture

- **Clean Architecture** with domain/data/presentation layers co-located per feature
- **MVVM** with ViewModels managing UI state via `StateFlow`
- **Repository pattern** — interfaces and implementations within each feature module
- **Use cases** — one class per operation (e.g., `AskAiUseCase`, `GetMapItemsUseCase`)
- **Hilt** for dependency injection across all modules
- **Compose** with `@Immutable` UI states and `PersistentList`/`PersistentSet` for stability

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

The key is read in `core/config/build.gradle.kts` and exposed via `BuildConfig.GEMINI_API_KEY`. It is injected at runtime through Hilt using the `@GeminiApiKey` qualifier.

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

### 3. Firebase (optional — for cloud sync)

Firebase provides anonymous authentication and Firestore cloud sync for prompt history. Without it, the app runs fully with local-only storage.

**To set up your own Firebase project:**

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project
2. Add an Android app with package name `se.onemanstudio.playaroundwithai`
3. Download `google-services.json` and place it in `app/` (replacing the existing one)
4. Enable **Authentication → Anonymous** and **Firestore Database**

**Firestore security rules:**
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/prompts/{promptId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

**Graceful degradation:** If Firebase is unavailable or not configured, a non-blocking snackbar informs the user. All prompts are saved locally to Room. AI chat, image/document analysis, and the full map experience work normally.

### CI

- CI injects these via GitHub Secrets (environment variables with the same names)
- Accessed in code via `BuildConfig` fields
- Custom Hilt qualifiers: `@GeminiApiKey`, `@BaseUrl`, `@LoggingLevel`

## Architecture Diagram — API Endpoints, Services & Data Flow

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                          FEATURE MODULES (domain + data + presentation)           │
│                                                                                  │
│  ┌─────────────────────────────────┐    ┌─────────────────────────────────────┐  │
│  │        :feature:chat            │    │          :feature:map               │  │
│  │                                 │    │                                     │  │
│  │  Presentation:                  │    │  Presentation:                      │  │
│  │    ChatViewModel, ChatScreen    │    │    MapViewModel, MapScreen          │  │
│  │                                 │    │                                     │  │
│  │  Domain:                        │    │  Domain:                            │  │
│  │    AskAiUseCase                 │    │    GetMapItemsUseCase               │  │
│  │    GetSuggestionsUseCase        │    │    GetSuggestedPlacesUseCase        │  │
│  │    SavePromptUseCase            │    │    MapRepository (interface)        │  │
│  │    ChatGeminiRepository (intf)  │    │    MapGeminiRepository (interface)  │  │
│  │    PromptRepository (interface) │    │                                     │  │
│  │                                 │    │  Data:                              │  │
│  │  Data:                          │    │    MapRepositoryImpl                │  │
│  │    ChatGeminiRepositoryImpl     │    │    MapGeminiRepositoryImpl          │  │
│  │    PromptRepositoryImpl         │    │    FakeMapApiService                │  │
│  │    Room DB, FirestoreDataSource │    │    MapDataGenerator                 │  │
│  │    SyncWorker (WorkManager)     │    │                                     │  │
│  └────────────┬────────────────────┘    └──────────────┬──────────────────────┘  │
│               │                                        │                        │
└───────────────┼────────────────────────────────────────┼────────────────────────┘
                │                                        │
                ▼                                        ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                              SHARED CORE MODULES                                 │
│                                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐  ┌───────────────────────────────┐  │
│  │   :core:network   │  │   :core:auth     │  │       :core:config            │  │
│  │                   │  │                  │  │                               │  │
│  │  GeminiApiService │  │  AuthRepository  │  │  ApiKeyAvailability           │  │
│  │  GeminiModel      │  │  AuthSession     │  │  @GeminiApiKey, @BaseUrl      │  │
│  │  DTOs, Interceptor│  │  Firebase Auth   │  │  ConfigurationModule          │  │
│  │  NetworkModule     │  │  Auth Use Cases  │  │  BuildConfig fields           │  │
│  └──────────────────┘  └──────────────────┘  └───────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────┐  ┌──────────────────┐                                     │
│  │   :core:theme     │  │    :core:ui      │                                     │
│  │  Colors, Typo     │  │  Compose widgets │                                     │
│  └──────────────────┘  └──────────────────┘                                     │
└──────────────────────────────────────────────────────────────────────────────────┘
                │
                ▼
┌──────────────────────────────────────────────────────────────────────────────────┐
│                            EXTERNAL SERVICES                                     │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐    │
│  │  Google Gemini API                                                       │    │
│  │  Base: https://generativelanguage.googleapis.com/                        │    │
│  │  Endpoint: POST /v1beta/models/gemini-3-flash-preview:generateContent    │    │
│  │  Auth: ?key={GEMINI_API_KEY} (query param via AuthenticationInterceptor) │    │
│  │                                                                          │    │
│  │  Request:  { contents: [{ parts: [{ text, inlineData? }] }] }            │    │
│  │  Response: { candidates: [{ content: { parts: [{ text }] } }] }          │    │
│  └──────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐    │
│  │  Firebase Firestore                                                      │    │
│  │  Path: /users/{userId}/prompts/{autoDocId}                               │    │
│  │  Document: { text: String, timestamp: Long }                             │    │
│  │  Operations: add (create), update (update text with AI answer)           │    │
│  │  userId is encoded in the document path, not stored as a field           │    │
│  └──────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐    │
│  │  Firebase Auth                                                           │    │
│  │  Method: Anonymous sign-in                                               │    │
│  │  Purpose: Identify user for Firestore documents                          │    │
│  └──────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐    │
│  │  Google Maps SDK                                                         │    │
│  │  Auth: MAPS_API_KEY (manifest placeholder)                               │    │
│  │  Services: Map tiles, markers, polylines, camera                         │    │
│  │  Location: FusedLocationProviderClient (last known location)             │    │
│  └──────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
└──────────────────────────────────────────────────────────────────────────────────┘
```

### Chat Feature — Request/Response Flow

```
User types prompt (+ optional image/document)
  │
  ▼
ChatViewModel.generateContent(prompt, imageUri?, documentUri?)
  │
  ├─ Image? → withContext(Default) → decode → scale to max 768px → JPEG @ 77% → Base64
  ├─ Document? → withContext(IO) → read text content
  │
  ▼
AskAiUseCase.invoke(prompt, imageBytes?, fileText?, analysisType?)
  ├─ Validate: prompt not blank (unless attachment present)
  ├─ Validate: prompt ≤ 50K chars, fileText ≤ 100K chars
  │
  ▼
ChatGeminiRepositoryImpl.getAiResponse()
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

  (Save & Sync — runs in parallel with Gemini API call)
  │
  ▼
SavePromptUseCase.invoke(prompt)
  │
  ▼
PromptRepositoryImpl.savePrompt()
  ├─ Set syncStatus = Pending
  ├─ INSERT INTO prompt_history (Room) — returns insertedId
  └─ Enqueue SyncWorker (if authenticated)
       │
       ▼ (Phase 1: sync question to Firestore)
     SyncWorker.doWork()
       ├─ SELECT * FROM prompt_history WHERE syncStatus = 'Pending'
       ├─ firestoreDocId is NULL → CREATE:
       │    ├─ Firestore /users/{userId}/prompts/.add({text, timestamp})
       │    ├─ Store returned docId → UPDATE prompt_history SET firestoreDocId = ?
       │    └─ Mark Synced only if text unchanged (race condition protection)
       └─ Return success / retry

  (After Gemini API responds successfully)
  │
  ▼
UpdatePromptTextUseCase.invoke(savedId, "Q: prompt\nA: response")
  │
  ▼
PromptRepositoryImpl.updatePromptText()
  ├─ UPDATE prompt_history SET text = 'Q: ...\nA: ...' WHERE id = ?
  ├─ UPDATE prompt_history SET syncStatus = 'Pending' WHERE id = ?
  └─ Enqueue SyncWorker again (if authenticated)
       │
       ▼ (Phase 2: update Firestore document with full Q&A)
     SyncWorker.doWork()
       ├─ SELECT * FROM prompt_history WHERE syncStatus = 'Pending'
       ├─ firestoreDocId is NOT NULL → UPDATE:
       │    ├─ Firestore /users/{userId}/prompts/{docId}.update("text", ...)
       │    └─ Mark Synced only if text unchanged (race condition protection)
       └─ Return success / retry

  Race condition: If AI responds while Phase 1 is still running,
  markSyncedIfTextMatches returns 0 (text changed) → stays Pending → Phase 2 handles it
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
