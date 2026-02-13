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
│                                :core-domain                                     │
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
│                                 :core-data                                      │
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
│  ┌──────────────────────────────────┐    │    ┌─────────────────────────┐       │
│  │       REMOTE DATA SOURCES        │    │    │   FakeMapApiService     │       │
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
│  │  │  Collection: "prompts"     │  │    │                                      │
│  │  │  Doc: {text, timestamp,    │  │    │                                      │
│  │  │        userId}             │  │    │                                      │
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
│                  │    │   Room DB: "play_with_ai_db"     │                      |
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
