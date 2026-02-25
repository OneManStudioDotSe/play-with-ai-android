## Architecture Diagram — API Endpoints, Services & Data Flow

```
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       PRESENTATION LAYER                                              │
│                                                                                                      │
│  ┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐ │
│  │   :feature:chat      │ │  :feature:explore    │ │   :feature:dream     │ │   :feature:plan      │ │
│  │                      │ │                      │ │                      │ │                      │ │
│  │  ChatScreen          │ │  ExploreScreen       │ │  DreamScreen         │ │  PlanScreen          │ │
│  │       │              │ │       │              │ │       │              │ │       │              │ │
│  │       ▼              │ │       ▼              │ │       ▼              │ │       ▼              │ │
│  │  ChatViewModel       │ │  ExploreViewModel    │ │  DreamViewModel      │ │  PlanViewModel       │ │
│  │  StateFlow:          │ │  StateFlow:          │ │  StateFlow:          │ │  StateFlow:          │ │
│  │   Initial|Loading|   │ │   (locations,filters │ │   Initial|Interpreting│ │   Initial|Running|  │ │
│  │   Success|Error      │ │    route,metrics)    │ │   |Result|Error      │ │   Result|Error       │ │
│  └───────┬──────────────┘ └───────┬──────────────┘ └───────┬──────────────┘ └───────┬──────────────┘ │
│          │ Uses 9 use cases       │ Uses 2 use cases       │ Uses 3 use cases       │ Uses 1 use case│
└──────────┼────────────────────────┼────────────────────────┼────────────────────────┼────────────────┘
           │                        │                        │                        │
           ▼                        ▼                        ▼                        ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                              DOMAIN + DATA LAYER (co-located per feature)                              │
│                                                                                                      │
│  ┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐ ┌──────────────────────┐ │
│  │    :data:chat        │ │   :data:explore      │ │    :data:dream       │ │    :data:plan        │ │
│  │                      │ │                      │ │                      │ │                      │ │
│  │ Use Cases:           │ │ Use Cases:           │ │ Use Cases:           │ │ Use Cases:           │ │
│  │  AskAiUseCase        │ │  GetExploreItemsUC   │ │  InterpretDreamUC    │ │  PlanTripUseCase     │ │
│  │  SavePromptUseCase   │ │  GetSuggestedPlacesUC│ │  SaveDreamUseCase    │ │                      │ │
│  │  UpdatePromptTextUC  │ │                      │ │  GetDreamHistoryUC   │ │ Repositories:        │ │
│  │  GetPromptHistoryUC  │ │ Repositories:        │ │                      │ │  TripPlannerRepo     │ │
│  │  GetSyncStateUseCase │ │  ExploreRepository   │ │ Repositories:        │ │                      │ │
│  │  GetFailedSyncCountUC│ │  ExploreGeminiRepo   │ │  DreamGeminiRepo     │ │ Tools:               │ │
│  │  RetryPendingSyncsUC │ │                      │ │  DreamRepository     │ │  search_places       │ │
│  │                      │ │ Data Sources:        │ │                      │ │  calculate_route     │ │
│  │ Repositories:        │ │  FakeExploreItemsSvc │ │ Data Sources:        │ │  RouteCalculator     │ │
│  │  ChatGeminiRepo      │ │  RouteCalculator     │ │  Room (dreams table) │ │                      │ │
│  │  PromptRepository    │ │                      │ │                      │ │                      │ │
│  │                      │ │                      │ │                      │ │                      │ │
│  │ Data Sources:        │ │                      │ │                      │ │                      │ │
│  │  Room (prompt_history│ │                      │ │                      │ │                      │ │
│  │  FirestoreDataSource │ │                      │ │                      │ │                      │ │
│  │  SyncWorker          │ │                      │ │                      │ │                      │ │
│  └───────┬──────────────┘ └───────┬──────────────┘ └───────┬──────────────┘ └───────┬──────────────┘ │
│          │                        │                        │                        │                │
└──────────┼────────────────────────┼────────────────────────┼────────────────────────┼────────────────┘
           │                        │                        │                        │
           ▼                        ▼                        ▼                        ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       SHARED CORE MODULES                                             │
│                                                                                                      │
│  ┌──────────────────────┐  ┌──────────────────────┐  ┌──────────────────────────────────────────┐   │
│  │   :core:network      │  │    :core:auth        │  │          :core:config                    │   │
│  │  GeminiApiService    │  │  AuthRepository      │  │  ApiKeyAvailability                      │   │
│  │  DTOs (text +        │  │  AuthSession         │  │  @GeminiApiKey, @BaseUrl, @LoggingLevel  │   │
│  │  function calling)   │  │  Firebase Auth       │  │  ConfigurationModule                     │   │
│  │  Interceptor         │  │  Auth Use Cases      │  │  BuildConfig fields                      │   │
│  └──────────────────────┘  └──────────────────────┘  └──────────────────────────────────────────┘   │
│                                                                                                      │
│  ┌──────────────────────┐  ┌──────────────────────┐                                                 │
│  │   :core:theme        │  │    :core:ui          │                                                 │
│  │  Colors, Typography  │  │  Compose widgets     │                                                 │
│  └──────────────────────┘  └──────────────────────┘                                                 │
└──────────────────────────────────────────────────────────────────────────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                       EXTERNAL SERVICES                                               │
│                                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────┐    │
│  │  Google Gemini API                                                                           │    │
│  │  Base: https://generativelanguage.googleapis.com/                                            │    │
│  │  Endpoint: POST /v1beta/models/gemini-3-flash-preview:generateContent                        │    │
│  │  Auth: ?key={GEMINI_API_KEY} (query param via AuthenticationInterceptor)                      │    │
│  │                                                                                              │    │
│  │  Standard:  { contents: [{ parts: [{ text, inlineData? }] }] }                               │    │
│  │  With tools: + { tools: [{ functionDeclarations }] }  (plan feature — agent loop)            │    │
│  │  Response: { candidates: [{ content: { parts: [text | functionCall] } }] }                   │    │
│  └──────────────────────────────────────────────────────────────────────────────────────────────┘    │
│                                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────┐    │
│  │  Firebase Firestore                                                                          │    │
│  │  Path: /users/{userId}/prompts/{autoDocId}                                                   │    │
│  │  Document: { text: String, timestamp: Long }                                                 │    │
│  │  Operations: add (create), update (update text with AI answer)                               │    │
│  └──────────────────────────────────────────────────────────────────────────────────────────────┘    │
│                                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────┐    │
│  │  Firebase Auth — Method: Anonymous sign-in                                                    │    │
│  └──────────────────────────────────────────────────────────────────────────────────────────────┘    │
│                                                                                                      │
│  ┌──────────────────────────────────────────────────────────────────────────────────────────────┐    │
│  │  Google Maps SDK — Auth: MAPS_API_KEY (manifest placeholder)                                  │    │
│  │  Services: Map tiles, markers, polylines, camera, FusedLocationProviderClient                 │    │
│  └──────────────────────────────────────────────────────────────────────────────────────────────┘    │
│                                                                                                      │
└──────────────────────────────────────────────────────────────────────────────────────────────────────┘
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
