# play-with-ai-android

Welcome to my showcase app. Here you can see and dig deep into my preferred way of working. 

Think of this as my digital sandbox—a place where serious Android engineering shakes hands with a slightly sassy 'AI Overlord.' 
Whether you're chatting with Gemini, analyzing documents, or scouting for scooters on the map, this app is built to show how the 
latest tech can be both powerful and surprisingly fun. 🚀

My approach is a holistic one: Work in a clean, simple and scalable way that spans beyond the basic coding. These are my principles:
- Simple coding
- Consistent design
- Two-way performance
- Meaningful scalability
- Future-proofing
- Those little extra things

## Simple coding
I strive to write code that is clean, easy to understand and follows a cohesive and logical way of doing things.
I use the latest and greatest of programming guidelines when it comes to Android:
- **Clean Architecture**: Separation of concerns with layers that contain the logic and functionality that they are supposed to contain
- **MVVM**: Decoupled UI logic using ViewModels
- **Lifecycle awareness**: Efficient state collection to optimize resource consumption
- **Kotlin coroutines**: Manage asynchronous tasks with structured concurrency
- **Consistent naming**: Established naming conventions for files, variables, and resources

## Consistent design
Nowadays, an app should not be just screens with lists and details, but it should also communicate an "air" 
of personality. For that reason, every one of my apps, including this one, has its own little design system that I 
use consistently across it. A complete suite of custom fonts, text styles, color palette and UI components are utilized
at the app in order to show the importance of a consistent design system and the modern way to use it. The whole DS is plug-n-play 
meaning that it is straightforward to update or change it completely, further showing the importance of a modular codebase.

## Two-way performance
Apart from how an app looks and feels, it is equally important for it to work smoothly and efficiently, both during the development and
when the user is interacting with it. For these reasons, I follow the principles below:
- **Compose stability**: Optimize Composables for performance by utilizing `@Immutable` UI states to minimize unnecessary recompositions
- **Modularity**: The project is organized into modules to optimize build times and enforce clear boundaries
- **Resource management**: Avoid memory leaks by making the most out of DI and stay away from unnecessary libraries or permissions

## Meaningful scalability
Projects on Android Studio have the tendency to grow as time goes by, therefore a scalable approach to the project's 
structure is necessary in order to keep the developer experience and maintainability possible. 

In this showcase project you can see a highly modular structure that can scale upon demand, regardless of team constellation or size: 
- **`:core-domain`**: Pure Kotlin module holding business logic, domain models, and repository definitions
- **`:core-data`**: Implementation of repositories, handling remote (Gemini AI) and local (Room) data sources
- **`:core-theme`**: Centralized theme and design language (colors, typography, etc.)
- **`:core-ui`**: Reusable UI components specific to the 'SoFa' design system
- **`:feature:chat`**: Feature module for the AI interaction experience
- **`:feature:map`**: Feature module for location-based services and route visualization

This structure allows potentially multiple teams to work on separate features without conflicts and ensures that each module only depends 
on what it absolutely needs.

## Future-proofing
An android app or project is usually part of a larger ecosystem where automations take place in the form of automated scripts, 
CI/CD pipelines, publishing and report generation. To illustrate the consideration of these steps, I have also added support for running 
scripts for some basic maintenance and code quality when a PR is opened. This makes sure that all future development will abide to the
team's or company's pipelines and will not break future releases.

## The little extra
To differentiate myself from the rest of the Android Engineers out there, I do the "little extra" that adds a nice layer of polishing:
- **Localization**: No hard-coded strings, giving an app ready for localization
- **A11y**: Proper semantics, localized content descriptions and support for dynamic font sizes
- **UX polish**: Smooth animations, easy-to-eye transitions and visual treats here and there
- **Personality**: A unique "AI Overlord" persona for the AI assistant to make interactions more engaging

## Architecture highlights

Beyond the high-level patterns, the project incorporates several notable architectural decisions worth highlighting:

### Local-first data with two-phase background sync
Prompt history follows a local-first strategy with a two-phase sync to Firestore. When the user sends a prompt, the question is immediately persisted to Room with a `Pending` sync status and a `SyncWorker` (WorkManager) is enqueued to create a new Firestore document. The auto-generated Firestore document ID is stored back in Room (`firestoreDocId`). Once the AI responds, the local entry is updated with the full Q&A text, its status is reset to `Pending`, and a second sync is scheduled — this time the worker detects the existing `firestoreDocId` and updates the same Firestore document rather than creating a new one. A race condition guard (`markSyncedIfTextMatches`) ensures the worker only marks an entry as `Synced` if the text hasn't changed during the sync, preventing stale data. Failed uploads are retried up to 3 times with exponential backoff (starting at 30 seconds) before being marked as `Failed`. The work policy uses `APPEND_OR_REPLACE` to avoid canceling in-progress syncs when new ones are enqueued. The `FirestoreDataSource` enforces authentication — unauthenticated requests fail immediately rather than falling back to anonymous data. The Room database indexes `syncStatus` and `firestoreDocId` columns for efficient querying by the sync worker. Each user's prompts are stored under `/users/{userId}/prompts/` in Firestore.

### Dynamic configuration via Hilt qualifiers
API keys, base URLs, and logging levels are injected at runtime through custom Hilt qualifiers (`@GeminiApiKey`, `@BaseUrl`, `@LoggingLevel`). A `ConfigurationModule` reads values from `BuildConfig`, which in turn are sourced from `local.properties` per build variant. This means switching between debug (verbose logging, debug API key) and release (no logging, production key) requires zero code changes — the DI graph handles it automatically.

### Immutable UI state with Kotlinx Immutable Collections
All UI state classes are annotated with `@Immutable` and use `PersistentList`/`PersistentSet` from kotlinx-collections-immutable. This guarantees that Compose can skip recompositions when state references haven't changed, resulting in measurably fewer recomposition cycles compared to using standard mutable collections.

### Image processing pipeline
Before reaching the Gemini API, images go through a multi-step pipeline: URI → Bitmap decoding (via `ImageDecoder`) → aspect-ratio-preserving downscale to max 768px → JPEG compression at 77% quality → Base64 encoding. This keeps payload sizes reasonable while preserving enough detail for the AI model to analyze. Image decoding and scaling runs on `Dispatchers.Default` while file reading runs on `Dispatchers.IO`, keeping the main thread free.

### OkHttp interceptor chain
Network requests pass through a custom `AuthenticationInterceptor` that appends the API key as a query parameter, followed by an `HttpLoggingInterceptor` whose verbosity level is injected via DI — full body logging in debug, no logging in release. Timeouts are set to 30 seconds for connect, read, and write.

### Use case input validation
Use cases enforce boundary validation before delegating to repositories. `GenerateContentUseCase` rejects blank prompts (when no attachments are present) and enforces maximum lengths for prompt text (50K chars) and file content (100K chars). `GetMapItemsUseCase` validates count ranges and coordinate bounds. `GetSuggestedPlacesUseCase` validates latitude/longitude ranges. `SavePromptUseCase` and `UpdatePromptTextUseCase` enforce non-blank text and valid IDs. This ensures invalid data is caught at the domain boundary with clear error messages, consistent with Clean Architecture's principle of enforcing business rules in the domain layer.

### Mapper layer between data and domain
Data transfer objects (DTOs) and Room entities are mapped to domain models through dedicated extension functions (`toDomain()`, `toEntity()`). This keeps the domain layer free of serialization annotations and database concerns, allowing it to remain a pure Kotlin module with no Android or framework dependencies. Domain models use `java.time.Instant` for timestamps (available without desugaring at minSdk 31), with mappers handling the conversion to/from `Long` epoch millis for Room storage.

### Fake API service for development
The map feature uses a `FakeMapApiService` that implements the same `MapApiService` interface as a real implementation would. It simulates network latency with a 1.5-second delay and generates randomized vehicle data near Stockholm. Swapping this for a real backend requires only changing the DI binding — no feature code needs to change.

### Dynamic theme for the map
The styling of the map changes depending on the light or dark mode that the app is using

Core functionality:
- **AI Chat**: Send prompts to an AI (Gemini) with support for image and document attachments
- **Smart History**: Access and reuse previous Q&A entries stored locally and synced to Firestore
- **Map Interaction**: Discover and filter locations (scooters/bikes) with optimal route calculation

Technical Details:
- **Jetpack Compose**: Native Android UI
- **Hilt**: Dependency Injection
- **Retrofit & OkHttp**: Networking
- **Room**: Local persistence
- **Firebase**: Firestore (user-scoped prompt sync), Firebase Auth (anonymous sign-in)
