# Play with AI - Sotiris edition

A **showcase** Android app where serious engineering meets a sassy AI Overlord.

## TL;DR

Production-grade Android showcase app with four distinct Gemini API integration patterns — conversational chat, structured JSON generation, AI image synthesis, and an autonomous agentic loop with native function calling. Built with Jetpack Compose, Clean Architecture, multi-module Gradle, Hilt, Room, Firebase, and WorkManager.

## How it looks

|                 Chat / Welcome                 |               Chat / Processing                |             Chat / Image analysis              |               Chat / AI response               |
|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/chat1.png" width="180"/> | <img src="screenshots/chat5.png" width="180"/> | <img src="screenshots/chat4.png" width="180"/> | <img src="screenshots/chat6.png" width="180"/> |


|              Dream / History                      |          Dream / Interpreting                     |           Dream / Scene visualization             |         Dream / AI-generated painting             |
|:-------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|
| <img src="screenshots/explain4.png" width="180"/> | <img src="screenshots/explain1.png" width="180"/> | <img src="screenshots/explain2.png" width="180"/> | <img src="screenshots/explain3.png" width="180"/> |


|              Plan / Trip planner               |          Plan / Agent thinking steps           |            Plan / Results with map             |            Plan / Trip plan details            |
|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|:----------------------------------------------:|
| <img src="screenshots/plan2.png" width="180"/> | <img src="screenshots/plan5.png" width="180"/> | <img src="screenshots/plan7.png" width="180"/> | <img src="screenshots/plan9.png" width="180"/> |


|              Explore / Map overview               |             Explore / AI suggestions              |             Explore / Path selection              |             Explore / Marker details              |
|:-------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|:-------------------------------------------------:|
| <img src="screenshots/explore1.png" width="180"/> | <img src="screenshots/explore6.png" width="180"/> | <img src="screenshots/explore3.png" width="180"/> | <img src="screenshots/explore5.png" width="180"/> |


|            Design system showcase            |            Design system showcase            |            Design system showcase            |                      Settings                      |
|:--------------------------------------------:|:--------------------------------------------:|:--------------------------------------------:|:--------------------------------------------------:|
| <img src="screenshots/ds1.png" width="180"/> | <img src="screenshots/ds2.png" width="180"/> | <img src="screenshots/ds3.png" width="180"/> | <img src="screenshots/settings1.png" width="180"/> |

## Features

- **AI Chat** — Gemini-powered conversation with image and document attachment support
- **Dream Interpreter** — AI analysis with a generative visual scene and an AI-painted artwork
- **Trip Planner** — Autonomous AI agent plans a real trip using Gemini function calling
- **Map Explorer** — Discover and filter nearby vehicles, get AI place suggestions, calculate optimal routes
- **Design System Showcase** — Interactive living style guide for the "SoFa" design system

## Tech stack

**Jetpack Compose · Hilt · Retrofit & OkHttp · Room · Firebase (Firestore + Auth) · WorkManager · Google Maps Compose**

## Build & run

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

The app uses Firebase for anonymous auth and Firestore sync of chat history. Without it, everything still works — prompts are saved locally to Room and a non-blocking snackbar lets you know sync is unavailable.

To set up your own Firebase project: create a project in the [Firebase Console](https://console.firebase.google.com/), add an Android app with package `se.onemanstudio.playaroundwithai`, drop the `google-services.json` into `app/`, then enable Anonymous Auth and Firestore. Use this security rule:

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

---

## Simple coding

Clean, readable code following modern Android guidelines — strict layer separation (domain → data → presentation), MVVM, structured concurrency with coroutines, lifecycle-aware state collection, and consistent naming conventions throughout.

## Consistent design

Every app deserves personality. This one ships its own design system ("SoFa") — custom fonts, text styles, a bold color palette, and reusable Compose components — all modular and easy to swap out entirely.

## Two-way performance

Smooth for both the developer and the user:
- **Compose stability**: `@Immutable` UI states with `PersistentList`/`PersistentSet` minimize unnecessary recompositions
- **Modularity**: Module-per-feature structure optimizes build times and enforces clear boundaries
- **Resource management**: DI-driven lifecycle management, correct coroutine dispatchers, no unnecessary libraries

## Meaningful scalability

A highly modular structure that scales regardless of team size:

**Core modules — shared infrastructure:**
- **`:core:network`**: Retrofit, OkHttp, GeminiApiService, DTOs (text, function calling, thinking), AuthenticationInterceptor, NetworkMonitor
- **`:core:auth`**: Firebase Auth, AuthRepository, auth use cases, AuthSession
- **`:core:config`**: API key management, BuildConfig fields, Hilt qualifiers, AppSettingsHolder
- **`:core:database`**: Shared Room DB — all entities (prompts, dreams, token usage), DAOs, TypeConverters, migrations
- **`:core:tracking`**: Cross-feature token usage tracking — interfaces, TrackerImpl, GetWeeklyTokenUsageUseCase
- **`:core:testing`**: Shared test helpers (MainCoroutineRule) used across all modules
- **`:core:theme`**: Centralized design system (colors, typography)
- **`:core:ui`**: Reusable Compose components

**Data modules — domain models, repositories, use cases:**
- **`:data:chat`**: Chat use cases, ChatGeminiRepository, PromptRepository, Firestore sync, SyncWorker
- **`:data:explore`**: Explore use cases, ExploreRepository, FakeExploreItemsService, route calculation, AI place suggestions
- **`:data:dream`**: Dream use cases, DreamGeminiRepository, DreamRepository, image persistence
- **`:data:plan`**: Trip planner use cases, TripPlannerRepository, agentic AI loop with Gemini function calling

**Feature modules — presentation only (Compose UI + ViewModels):**
- **`:feature:chat`** · **`:feature:explore`** · **`:feature:dream`** · **`:feature:plan`**
- **`:feature:showcase`**: ShowcaseScreen — interactive design system guide (no ViewModel, no data layer, no Hilt)

The dependency flow runs strictly one way: `feature → data → core`. Multiple teams can work on separate features without stepping on each other.

## Gemini AI — four distinct integration patterns

The app doesn't just call Gemini once and call it a day. Each feature uses the API in a fundamentally different way.

### 1. Conversational chat with multimodal input
**Feature: Chat**

Classic request → response with a custom "AI Overlord" system prompt. What makes it non-trivial is the multimodal pipeline: images are decoded, downscaled to max 768px, JPEG-compressed at 77%, and Base64-encoded as `inlineData` — all on the right dispatcher (`Dispatchers.Default` for CPU work, `Dispatchers.IO` for file reads). Documents have their text extracted and appended to the prompt. Token usage is tracked after every response.

### 2. Structured JSON generation
**Features: Dream interpreter, Explore AI suggestions**

The prompt instructs Gemini to return a strict JSON schema. In Dream, that means a full scene specification — `DreamPalette`, `DreamLayer[]`, `DreamElement[]`, `DreamParticle[]` — plus a mood classification. In Explore, a list of nearby place objects. Responses are stripped of markdown code fences before being parsed with Gson. No SDK schema enforcement — just precise prompts and reliable parsing via a shared `JsonExtractor` utility in `:core:network`.

### 3. AI image generation
**Feature: Dream interpreter**

Uses the `gemini-2.5-flash-image` model with `responseModalities: [IMAGE, TEXT]`. After the dream is interpreted, a second parallel coroutine sends the dream description for painting — Gemini returns the artwork as Base64 inline data. The image is decoded and saved to local storage, linked to the dream record in Room for persistence across sessions. If the interpretation doesn't produce a valid dream ID within 5 seconds, image saving times out gracefully.

### 4. Agentic loop with native function calling
**Feature: Trip planner**

The most sophisticated pattern. Gemini autonomously decides which tools to call across up to 10 iterations. The app executes each tool locally, appends the result to the conversation history, and loops until Gemini produces a final text answer.

Two tools are declared as `functionDeclarations`:
- **`search_places`** — triggers a sub-agent Gemini call to generate real-looking places near given coordinates, returning structured JSON
- **`calculate_route`** — runs a local TSP solver (brute-force for ≤8 points, nearest-neighbour for more) using the Haversine formula

One important detail: Gemini's model turns can include thinking parts (`thought: true` + `thought_signature`). These **must be preserved verbatim** when replaying conversation history — omitting them causes the API to return HTTP 400.

## Future-proofing

CI/CD with GitHub Actions runs Detekt, Lint, debug build, and unit tests on every PR — code quality is enforced, not optional.

## The little extra

- **Localization**: All strings in `strings.xml` with full Swedish (`sv`) translations across every module
- **A11y**: Proper semantics, content descriptions, live regions, dynamic font sizes
- **UX polish**: Smooth animations, animated loading states, flippable cards
- **Personality**: The "AI Overlord" persona makes interactions memorable

---

## Architecture highlights

### Local-first sync with two-phase background worker

Chat prompts are persisted to Room instantly, then synced to Firestore via `SyncWorker`:
1. **Phase 1**: User sends prompt → saved locally (`Pending`) → worker creates a Firestore doc and stores the returned `firestoreDocId`
2. **Phase 2**: AI responds → local entry updated with full Q&A → worker updates the same Firestore doc

Race condition handled: if the AI responds while Phase 1 is still in flight, `markSyncedIfTextMatches()` returns 0 (text changed) → prompt stays `Pending` → Phase 2 picks it up. Retry policy: up to 3 attempts with exponential backoff (30s initial), then marked `Failed`.

### Dynamic configuration via Hilt qualifiers

API keys, base URLs, and logging levels are injected at runtime through `@GeminiApiKey`, `@BaseUrl`, `@LoggingLevel`. Debug vs release configuration requires zero code changes.

### Use case input validation at the domain boundary

Use cases validate before delegating — blank prompts, max lengths (50K prompt, 100K file text), coordinate bounds. Invalid data never reaches the repository layer.

### Mapper layer between data and domain

`toDomain()` / `toEntity()` extensions keep the domain layer free of serialization annotations. Domain models use `java.time.Instant`; mappers convert to/from `Long` epoch millis for Room.

### Fake API for development

`FakeExploreItemsService` implements the real `ExploreApiService` interface with simulated latency and randomized vehicle data. Swapping for a real backend is a single DI binding change.
