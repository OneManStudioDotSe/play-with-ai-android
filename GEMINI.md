# GEMINI.md — Project Intelligence for AI Agents

## 🎯 Project Overview
**Play With AI** is a high-fidelity Android showcase application demonstrating advanced Gemini API integrations. It uses a strictly decoupled multi-module architecture, modern Jetpack Compose UI ("SoFa" design system), and production-grade engineering patterns.

---

## 🏗️ Architecture & Modules
The project follows **Clean Architecture** with feature-based modularization.
Dependency flow: `feature → data → core`.

### Core Modules (`:core:*`)
- **`:network`**: GeminiApiService, shared DTOs, `AuthenticationInterceptor` (API key injection), `NetworkMonitor`.
- **`:config`**: Centralized DI for API keys, settings, and build-time constants.
- **`:database`**: Shared Room DB (v6) with entities for `Prompt`, `Dream`, and `TokenUsage`.
- **`:tracking`**: Cross-feature token usage tracking (prompt/candidate/total).
- **`:testing`**: Shared test helpers (`MainCoroutineRule`).
- **`:theme` / `:ui`**: "SoFa" Design System. **Prefer `:ui:components` components** (e.g., `NeoBrutalCard`) over raw Material3.
- **`:auth`**: Firebase Anonymous Auth for Firestore sync.

### Data & Feature Modules
- **Chat (`:data:chat`, `:feature:chat`)**: Multimodal chat with image/doc support and Firestore background sync (`SyncWorker`).
- **Dream (`:data:dream`, `:feature:dream`)**: Structured JSON generation for animated scenes + AI image synthesis (`gemini-2.5-flash-image`).
- **Plan (`:data:plan`, `:feature:plan`)**: **Agentic Loop** using Gemini function calling with local tool execution.
- **Explore (`:data:explore`, `:feature:explore`)**: Google Maps integration with AI-powered place suggestions.
- **Nano (`:feature:nano`)**: **On-device AI** — checks Gemini Nano support via ML Kit GenAI. No `:data` module, no `:core:network` (no cloud call); depends only on `:ui:components` / `:ui:theme` + the ML Kit SDK.
- **Settings (`:feature:settings`)**: Screen-aware Settings bottom sheet. **`:core`-only** (no `:data` dependency) — reads/writes the holders in `:core:config`.
- **Showcase (`:feature:showcase`)**: Living style guide for the "SoFa" design system (no ViewModel, no Hilt).

---

## 🧭 Symbol Mapping & Navigation
Quick reference for prompt definitions and logic ownership:

- **Chat**: `ChatPrompts.kt` (`:data:chat`) — conversation starters & analysis instructions. `AiPersona.kt` (`:core:config`) — persona system prompts (user-selectable in Settings). `ChatGeminiRepositoryImpl.kt`.
- **Dream**: `DreamPrompts.kt` (`:data:dream`), `DreamGeminiRepositoryImpl.kt`.
- **Plan (Agent)**: `PlanPrompts.kt` (`:data:plan`), `TripPlannerRepositoryImpl.kt`.
- **Explore**: `ExplorePrompts.kt` (`:data:explore`), `ExploreGeminiRepositoryImpl.kt`.
- **Nano (On-device)**: `NanoViewModel.kt` (`:feature:nano`) — ML Kit GenAI probe; no prompt file, no repository.
- **Sync Logic**: `SyncWorker.kt` (`:data:chat`) handles local-to-cloud history persistence.

---

## 🎨 Design System ("SoFa")
All UI must follow the "NeoBrutalism" aesthetic defined in `:ui:components`.
**Core Components** (in `:ui:components/.../sofa/`):
- `NeoBrutalCard`, `NeoBrutalButton`, `NeoBrutalTextField`, `NeoBrutalChip`.
- `NeoBrutalTopBar`: Standard header for all screens.
- `UsageChart`: Animated token consumption visualization.

---

## 📐 Shared Algorithms & Geo-Logic
Common logic used across AI features for spatial reasoning:
- **`Algorithms.kt`** (`:core:network`): `haversineKm` (distance) and `permutations` (route discovery).
- **`RouteCalculator.kt`** (`:data:plan`): TSP (Traveling Salesman) solver used by both Agent and Explore features.

---

## 🔄 Sync & Persistence (Local-First)
- **Local**: All prompts/dreams are saved to Room immediately (`Pending` status).
- **Cloud**: `SyncWorker` (WorkManager) triggers after auth to push `Pending` items to Firestore.
- **Status Mapping**: `SyncStatus` (v6) maps to `Pending`, `Synced`, or `Failed`.

---

## 🤖 Gemini API Integration Patterns
Both models are **user-configurable** via Settings. Defaults: `gemini-3-flash-preview` (text/chat/plan/explore) and `gemini-2.5-flash-image` (dream image generation). The model name is injected into each API call via Retrofit `@Path` from `AppSettingsHolder`.

### 1. Simple & Multimodal (Chat)
- Images: Scaled to 512/768/1024 px and JPEG-compressed at 40/77/93% — both configurable in Settings via the Image Quality setting. Base64 `inlineData`.
- Documents: Text extracted and appended to prompt context.

### 2. Structured JSON (Dream, Explore)
- Prompt engineers a strict JSON schema.
- **Protocol**: Raw text is cleaned of markdown fences before parsing with Gson.

### 3. Agentic Function Calling (Plan)
- **CRITICAL**: Gemini may include `thought: true` and `thought_signature` in model turns.
- **Mandate**: These parts **MUST** be preserved verbatim when replaying conversation history for subsequent turns. Stripping them results in **HTTP 400**.

### 4. Image Generation (Dream)
- Uses `generateImageContent` endpoint.
- Returns `inlineData` (Base64 PNG) + text (artist name).
- Includes retry logic (up to 3x) as the model sometimes returns text-only.

### 5. On-device Inference (Nano)
- **No cloud call.** Uses ML Kit GenAI (`com.google.mlkit:genai-summarization`), backed by Gemini Nano via AICore.
- ML Kit has no "is Nano available" call, so `NanoViewModel` uses a `Summarizer` purely as a probe: `checkFeatureStatus()` returns `AVAILABLE` / `DOWNLOADABLE` / `UNAVAILABLE`; `downloadFeature()` pulls the model with byte-progress.
- No API key, no token tracking, no `:core:network` dependency.

---

## 🛠️ Developer Workflows

### Build & Validate
- **Clean Build**: `./gradlew clean assembleDebug`
- **Tests**: `./gradlew testDebugUnitTest` (JUnit 4 + MockK + Google Truth).
- **Quality**: `./gradlew detekt` (Strictly enforced, `maxIssues: 0`).
- **Lint**: `./gradlew lintDebug`.

### Project Standards
- **UI State**: Sealed interfaces (e.g., `UiState.Loading`, `UiState.Success(data)`).
- **Immutability**: Use `PersistentList`/`PersistentSet` for Compose stability.
- **Conventions**: Mappers (`toDomain()`, `toEntity()`), UseCases (`DoSomethingUseCase`).
- **Resources**: All strings must be in `strings.xml` (supports English and Swedish).

### API Keys
- Stored in `local.properties`: `GEMINI_API_KEY_DEBUG`, `MAPS_API_KEY`.
- Accessed via Hilt qualifiers: `@GeminiApiKey`, `@MapsApiKey`.

---

## ⚠️ Known Constraints
- **Line Length**: Max 160 characters (Detekt).
- **Java/Kotlin**: JDK 17, Kotlin 2.3.10, AGP 9.1.0.
- **Compose**: Target SDK 36. Use `@Immutable` for all UI state classes.
- **Firebase**: Sync is non-blocking; app must degrade gracefully if auth/sync fails.
- **Model selection**: `AppSettingsHolder` holds the active model names in memory (no persistence across cold starts). Persona system prompts are properties of `AiPersona` enum values in `:core:config` — both `data:chat` and `feature:settings` depend on `:core:config`, so no extra Gradle dependencies are needed.
- **Settings architecture**: `:feature:settings` depends only on `:core` modules (no `:data:*` dependency) — `AppSettingsHolder` and `ExploreSettingsHolder` are the shared state boundary.
