## Architecture Diagram — API Endpoints, Services & Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PRESENTATION LAYER                                 │
│                                                                                 │
│  ┌──────────────────────────────┐       ┌──────────────────────────────────┐    │
│  │       :feature:chat          │       │        :feature:explore            │    │
│  │                              │       │                                    │    │
│  │  ChatScreen (Compose)        │       │  ExploreScreen (Compose)           │    │
│  │       │                      │       │       │                            │    │
│  │       ▼                      │       │       ▼                            │    │
│  │  ChatViewModel               │       │  ExploreViewModel                  │    │
│  │   │  StateFlow<ChatUiState>  │       │   │  StateFlow<ExploreUiState>     │    │
│  │   │   ├─ Initial             │       │   │   (locations, filters,         │    │
│  │   │   ├─ Loading             │       │   │    route, metrics)             │    │
│  │   │   ├─ Success             │       │   │                                │    │
│  │   │   └─ Error               │       │   │                                │    │
│  └───┼──────────────────────────┘       └───┼────────────────────────────────┘    │
│      │ Uses 9 use cases                     │ Uses 2 use cases                  │
└──────┼──────────────────────────────────────┼───────────────────────────────────┘
       │                                      │
       ▼                                      ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                                DOMAIN LAYER                                     │
│                                :core-domain                                     │
│                                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐  │
│  │ GenerateContentUC    │  │ GetPromptHistoryUC   │  │ GetExploreItemsUC     │  │
│  │ GetSuggestionsUC     │  │ SavePromptUC         │  │ GetSuggestedPlacesUC  │  │
│  │                      │  │ UpdatePromptTextUC   │  │                       │  │
│  │                      │  │ GetSyncStateUC       │  │                       │  │
│  │                      │  │ GetFailedSyncCountUC │  │                       │  │
│  │                      │  │ RetryPendingSyncsUC  │  │                       │  │
│  └──────────┬───────────┘  └──────────┬───────────┘  └───────────┬───────────┘  │
│             │                         │                          │              │
│             ▼                         ▼                          ▼              │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐  │
│  │ GeminiRepository     │  │ PromptRepository     │  │ ExploreRepository     │  │
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
│                                 :core-data                                      │
│                                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                        Repository Implementations                       │    │
│  │                                                                         │    │
│  │  GeminiRepositoryImpl   PromptRepositoryImpl   ExploreRepositoryImpl    │    │
│  │         │                  │          │              │                  │    │
│  │         │                  │          │              │                  │    │
│  │  AuthRepositoryImpl        │          │              │                  │    │
│  │         │                  │          │              │                  │    │
│  └─────────┼──────────────────┼──────────┼──────────────┼──────────────────┘    │
│            │                  │          │              │                       │
│            ▼                  ▼          │              ▼                       │
│  ┌──────────────────────────────────┐    │    ┌─────────────────────────┐       │
│  │       REMOTE DATA SOURCES        │    │    │  FakeExploreItemsService│       │
│  │                                  │    │    │   (Mock data generator) │       │
│  │  ┌────────────────────────────┐  │    │    │   * Simulates  delay    │       │
│  │  │     GeminiApiService       │  │    │    │   * Generates random    │       │
│  │  │     (Retrofit)             │  │    │    │     vehicle locations   │       │
│  │  │                            │  │    │    └─────────────────────────┘       │
│  │  │  POST v1beta/models/       │  │    │                                      │
│  │  │  gemini-3-flash-preview    │  │    │                                      │
│  │  │  :generateContent          │  │    │                                      │
│  │  └────────────┬───────────────┘  │    │                                      │
│  │               │                  │    │                                      │
│  │  ┌────────────────────────────┐  │    │                                      │
│  │  │  FirestoreDataSource       │  │    │                                      │
│  │  │                            │  │    │                                      │
│  │  │  Path: /users/{userId}/    │  │    │                                      │
│  │  │        prompts/{docId}     │  │    │                                      │
│  │  │  Doc: {text, timestamp}    │  │    │                                      │
│  │  │  Ops: add, update          │  │    │                                      │
│  │  └────────────┬───────────────┘  │    │                                      │
│  │               │                  │    │                                      │
│  │  ┌────────────────────────────┐  │    │                                      │
│  │  │  Firebase Auth             │  │    │                                      │
│  │  │  signInAnonymously()       │  │    │                                      │
│  │  └────────────┬───────────────┘  │    │                                      │
│  └───────────────┼──────────────────┘    │                                      │
│                  │                       ▼                                      │
│                  │    ┌──────────────────────────────────┐                      │
│                  │    │       LOCAL DATA SOURCE          │                      │
│                  │    │                                  │                      │
│                  │    │   Room DB: "play_with_ai_db" v3  │                      │
│                  │    │   Table: "prompt_history"        │                      │
│                  │    │   ┌─────────────────────────┐    │                      │
│                  │    │   │ id             (PK,auto)│    │                      │
│                  │    │   │ text           (String) │    │                      │
│                  │    │   │ timestamp      (Long)   │    │                      │
│                  │    │   │ syncStatus     (Enum)   │    │                      │
│                  │    │   │  ├─ Pending             │    │                      │
│                  │    │   │  ├─ Synced              │    │                      │
│                  │    │   │  └─ Failed              │    │                      │
│                  │    │   │ firestoreDocId (String) │    │                      │
│                  │    │   │                         │    │                      │
│                  │    │   │ Indexes:                │    │                      │
│                  │    │   │  syncStatus,            │    │                      │
│                  │    │   │  firestoreDocId         │    │                      │
│                  │    │   └─────────────────────────┘    │                      │
│                  │    │                                  │                      │
│                  │    │   PromptsHistoryDao:             │                      │
│                  │    │    savePrompt()                  │                      │
│                  │    │    getPromptHistory() → Flow     │                      │
│                  │    │    getPromptsBySyncStatus()      │                      │
│                  │    │    updateSyncStatus()            │                      │
│                  │    │    updateFirestoreDocId()        │                      │
│                  │    │    markSyncedIfTextMatches()     │                      │
│                  │    └──────────────────────────────────┘                      │
│                  │                      ▲                                       │
│                  │                      │                                       │
│                  │    ┌─────────────────┴────────────────┐                      │
│                  │    │       BACKGROUND SYNC            │                      │
│                  │    │                                  │                      │
│                  │    │   SyncWorker (WorkManager)       │                      │
│                  │    │    1. Get Pending prompts (Room) │                      │
│                  │    │    2. No docId? → CREATE in ─────┼──┐                   │
│                  │    │       Firestore, store docId     │  │                   │
│                  │    │       Has docId? → UPDATE ───────┼──┤                   │
│                  │    │       existing Firestore doc     │  │                   │
│                  │    │    3. Mark Synced if text        │  │                   │
│                  │    │       unchanged (race guard)     │  │                   │
│                  │    │                                  │  │                   │
│                  │    │   Constraints:                   │  │                   │
│                  │    │    - Requires network            │  │                   │
│                  │    │   Policy: APPEND_OR_REPLACE      │  │                   │
│                  │    │   Backoff: Exponential (30s)     │  │                   │
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
│  │  Path: /users/{userId}/prompts/{autoDocId}                               │   │
│  │  Document: { text: String, timestamp: Long }                             │   │
│  │  Operations: add (create), update (update text with AI answer)           │   │
│  │  userId is encoded in the document path, not stored as a field           │   │
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
  ├─ Image? → withContext(Default) → decode → scale to max 768px → JPEG @ 77% → Base64
  ├─ Document? → withContext(IO) → read text content
  │
  ▼
GenerateContentUseCase.invoke(prompt, imageBytes?, fileText?, analysisType?)
  ├─ Validate: prompt not blank (unless attachment present)
  ├─ Validate: prompt ≤ 50K chars, fileText ≤ 100K chars
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

### Explore Feature — Data Flow

```
ExploreScreen launched
  │
  ▼
ExploreViewModel.loadExploreData()
  │
  ▼
GetExploreItemsUseCase.invoke(count = 30)
  │
  ▼
ExploreRepositoryImpl.getExploreItems(30)
  │
  ▼
FakeExploreItemsService.getExploreItems(30)   ← Mock implementation
  ├─ delay(1500ms)                               (no real API yet)
  ├─ Generate 30 random vehicles
  │   ├─ Type: SCOOTER or BICYCLE
  │   ├─ Position: random lat/lng near Stockholm
  │   ├─ Battery: random level
  │   └─ Nickname: random name
  └─ Return List<ExploreItemDto>
  │
  ▼
Explore DTO → Domain (ExploreItem) → UI Model (ExploreItemUiModel)
  │
  ▼
ExploreUiState updated
  ├─ allLocations: PersistentList<ExploreItemUiModel>
  ├─ visibleLocations: PersistentList<ExploreItemUiModel>   (filtered subset)
  ├─ activeFilters: PersistentSet<VehicleType>
  ├─ optimalRoute: PersistentList<LatLng>?
  └─ metrics: RouteMetrics?
  │
  ▼
GoogleMap renders markers + polylines
  │
  ├─ User toggles filter → ExploreViewModel.toggleFilter(type)
  │   └─ Recompute visibleLocations from allLocations
  │
  └─ User taps "Calculate Route" → ExploreViewModel.calculateOptimalRoute(userLoc)
      ├─ Get user location via FusedLocationProviderClient
      ├─ Compute all permutations of selected locations
      ├─ Find minimum-distance path (brute force TSP)
      └─ Update optimalRoute + animate camera to bounds
```
