# Play with AI - Sotiris edition

A **showcase** Android app where serious engineering meets a sassy AI Overlord.

Chat with Gemini, analyze documents, or plan a tour around your location by visiting interesting places and use e-scooters and e-bikes.

## How it looks

|                 Chat / Welcome                 |               Chat / Processing                |             Chat / Image analysis              |               Chat / AI response               |
|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/chat1.png" width="180"/> | <img src="screenshots/chat5.png" width="180"/> | <img src="screenshots/chat4.png" width="180"/> | <img src="screenshots/chat6.png" width="180"/> |


|               Explain / Dream list                |          Explain / Dream interpretating           |           Explain / Dream visualization           |         Explain / Dream image generation          |
|:-------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|
| <img src="screenshots/explain4.png" width="180"/> | <img src="screenshots/explain1.png" width="180"/> | <img src="screenshots/explain2.png" width="180"/> | <img src="screenshots/explain3.png" width="180"/> |


|                  Trip planner                  |              Agent thinking steps              |               Trip plan with map               |               Trip plan details                |
|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/plan2.png" width="180"/> | <img src="screenshots/plan5.png" width="180"/> | <img src="screenshots/plan7.png" width="180"/> | <img src="screenshots/plan9.png" width="180"/> |


|              Explore / Map overview               |                  Explore / AI suggestions                  |                  Explore / Path selection                  |             Explore / Market details              |
|:-------------------------------------------------:|:----------------------------------------------------------:|:----------------------------------------------------------:|:-------------------------------------------------:|
| <img src="screenshots/explore1.png" width="180"/> |     <img src="screenshots/explore6.png" width="180"/>      |     <img src="screenshots/explore3.png" width="180"/>      | <img src="screenshots/explore5.png" width="180"/> |


|            Design system showcase            |            Design system showcase            |            Design system showcase            |                      Settings                      |
|:--------------------------------------------:|:--------------------------------------------:|:--------------------------------------------:|:--------------------------------------------------:|
| <img src="screenshots/ds1.png" width="180"/> | <img src="screenshots/ds2.png" width="180"/> | <img src="screenshots/ds3.png" width="180"/> | <img src="screenshots/settings1.png" width="180"/> |

## The idea

This app was built to showcase how modern Android tech can be both powerful and fun. 🚀

My principles during the development of this project are the following:
- Simple coding
- Consistent design
- Two-way performance
- Meaningful scalability
- Future-proofing
- Those little extra things


## Core functionality

- **AI Chat**: Prompts to Gemini with image and document attachment support
- **Smart History**: Local Q&A history synced to Firestore
- **Map Interaction**: Discover and filter vehicles with optimal route calculation
- **Dream Interpreter**: AI-powered dream analysis with generative visual scenes and AI-generated paintings
- **Trip Planner**: Agentic AI trip planning using Gemini function calling with map integration
- **Design System Showcase**: Interactive living style guide for the "SoFa" design system — typography, colors, and all UI components

## Technical stack

- **Jetpack Compose** · **Hilt** · **Retrofit & OkHttp** · **Room** · **Firebase** (Firestore + Auth) · **WorkManager** · **Google Maps Compose**

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

## Firebase setup (optional)

The app uses Firebase for **anonymous authentication** and **Firestore cloud sync** of prompt history. These features are optional — without Firebase, the app runs fully with all chat and map functionality, storing prompts locally only.

### Setting up your own Firebase project

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project
2. Add an Android app with package name `se.onemanstudio.playaroundwithai`
3. Download the generated `google-services.json` and place it in the `app/` directory (replacing the existing one)
4. In the Firebase Console, enable:
   - **Authentication → Sign-in method → Anonymous** (toggle on)
   - **Firestore Database → Create database** (start in test mode or configure rules below)

### Firestore security rules

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

### Without Firebase

If you skip Firebase setup, the app degrades gracefully:
- A non-blocking snackbar informs you that cloud sync is unavailable
- All prompts are saved locally to Room and persist across sessions
- AI chat, image/document analysis, and the full map experience work normally

## Simple coding

Clean, readable code following modern Android guidelines:
- **Clean architecture**: Strict layer separation (domain → data → presentation)
- **MVVM**: Decoupled UI logic using ViewModels
- **Lifecycle awareness**: Efficient state collection to optimize resource consumption
- **Kotlin coroutines**: Structured concurrency for async tasks
- **Consistent naming**: Established conventions for files, variables, and resources
- **AI-assisted flow**: Make the most of the superpowers that AI gives to developers to speed up the tedious part of development

## Consistent design

Every app deserves personality. This one has its own plug-n-play design system ("SoFa") — custom fonts, text styles, color palette and UI components — all modular and easy to swap out entirely.

## Two-way performance

Smooth for both the developer and the user:
- **Compose stability**: `@Immutable` UI states minimize unnecessary recompositions
- **Modularity**: Module-per-feature structure optimizes build times and enforces clear boundaries
- **Resource management**: DI-driven lifecycle management, no unnecessary libraries or permissions

## Meaningful scalability

A highly modular structure that scales regardless of team size:

**Core modules — shared infrastructure:**
- **`:core:network`**: Retrofit, OkHttp, GeminiApiService, request/response DTOs, AuthenticationInterceptor, NetworkMonitor
- **`:core:auth`**: Firebase Auth, AuthRepository, auth use cases, AuthSession
- **`:core:config`**: API key management, BuildConfig fields, Hilt qualifiers, AppSettingsHolder
- **`:core:database`**: Shared Room database — all entities (prompts, dreams, token usage), DAOs, TypeConverters, migrations
- **`:core:tracking`**: Token usage tracking — TokenUsageTracker/Query interfaces, TrackerImpl, GetWeeklyTokenUsageUseCase
- **`:core:theme`**: Centralized design system (colors, typography)
- **`:core:ui`**: Reusable Compose components

**Data modules — domain models, repositories, use cases:**
- **`:data:chat`**: Chat use cases, ChatGeminiRepository, PromptRepository, Firestore sync, SyncWorker
- **`:data:explore`**: Explore use cases, ExploreRepository, FakeExploreItemsService, route calculation, AI place suggestions
- **`:data:dream`**: Dream use cases, DreamGeminiRepository, DreamRepository
- **`:data:plan`**: Trip planner use cases, TripPlannerRepository, agentic AI loop with Gemini function calling

**Feature modules — presentation only (Compose UI + ViewModels):**
- **`:feature:chat`**: ChatViewModel, ChatScreen
- **`:feature:explore`**: ExploreViewModel, ExploreScreen
- **`:feature:dream`**: DreamViewModel, DreamScreen
- **`:feature:plan`**: PlanViewModel, PlanScreen
- **`:feature:showcase`**: ShowcaseScreen — interactive design system guide (no ViewModel, no data layer, no Hilt)

The dependency flow runs strictly one way: `feature → data → core`. Shared infrastructure (DB, tracking, networking) lives in focused core modules. Multiple teams can work on separate features without conflicts.

## Future-proofing

CI/CD pipeline with GitHub Actions runs Detekt, Lint, builds, and unit tests on every PR — ensuring code quality standards are maintained across all future development.

## The little extra

- **Localization**: No hard-coded strings — ready for multi-language support
- **A11y**: Proper semantics, content descriptions, dynamic font sizes
- **UX polish**: Smooth animations and visual transitions throughout
- **Personality**: A unique "AI Overlord" persona for engaging interactions

## Architecture highlights

### Local-first data with two-phase background sync

Prompts are persisted to Room immediately, then synced to Firestore via `SyncWorker` in two phases:
1. **Phase 1**: User sends prompt → saved locally with `Pending` status → worker creates Firestore doc and stores the returned `firestoreDocId`
2. **Phase 2**: AI responds → local entry updated with full Q&A → worker updates the same Firestore doc

Key safeguards:
- **Retry policy**: Up to 3 attempts with exponential backoff (30s initial), then marked `Failed`
- **Work policy**: `APPEND_OR_REPLACE` avoids canceling in-progress syncs
- **Auth enforcement**: `FirestoreDataSource` rejects unauthenticated requests immediately
- **DB indexes**: `syncStatus` and `firestoreDocId` indexed for efficient worker queries
- **Scoping**: Prompts stored under `/users/{userId}/prompts/` in Firestore

### Dynamic configuration via Hilt qualifiers

API keys, base URLs, and logging levels injected at runtime through `@GeminiApiKey`, `@BaseUrl`, `@LoggingLevel` qualifiers. Debug vs release configuration (verbose logging, different API keys) requires zero code changes.

### Immutable UI state with Kotlinx Immutable Collections

All UI states use `@Immutable` with `PersistentList`/`PersistentSet`, allowing Compose to skip recompositions when state references haven't changed.

### Image processing pipeline

Images go through: URI → `ImageDecoder` → downscale to max 768px → JPEG @ 77% → Base64. Decoding runs on `Dispatchers.Default`, file reading on `Dispatchers.IO` — keeping the main thread free.

### OkHttp interceptor chain

`AuthenticationInterceptor` appends the API key as a query param, followed by `HttpLoggingInterceptor` (full body in debug, none in release). Timeouts: 30s connect/read/write.

### Use case input validation

Use cases validate inputs before delegating to repositories — blank prompts, max lengths (50K prompt, 100K file), coordinate bounds, valid IDs. Invalid data is caught at the domain boundary with clear error messages.

### Mapper layer between data and domain

Dedicated `toDomain()` / `toEntity()` extensions keep the domain layer free of serialization annotations and database concerns. Domain models use `java.time.Instant` for timestamps, with mappers converting to/from `Long` epoch millis for Room.

### Fake API service for development

`FakeExploreItemsService` implements the real `ExploreApiService` interface with simulated latency and randomized vehicle data. Swapping for a real backend requires only changing the DI binding.

### Dynamic theme for the map

Map styling adapts automatically to the app's light or dark mode.
