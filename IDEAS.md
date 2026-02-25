# IDEAS.md — Settings Panel Feature Ideas

Brainstorming ideas for new settings to add to the Settings bottom sheet.
The current panel has: vehicle count slider, search radius slider, weekly usage chart, and about section.

Each idea below is backed by a hardcoded value or missing toggle found in the codebase.

---

## Tier 1 — High Impact, Low Effort

### 1. Typewriter Speed / Instant Mode

Toggle the character-by-character typing animation off entirely, or offer speed presets (slow / normal / fast / instant). 
Power users and accessibility-minded users will want to read the full answer immediately.

- **Code:** `feature/chat/src/main/java/.../views/TypewriterText.kt` — `TYPING_DELAY = 10L` (ms per character)

### 2. Haptic Feedback Toggle

The app fires `HapticFeedbackConstants.CLOCK_TICK` on every typed character in chat and on map interactions. 
There is no way to turn this off.

- **Code:** `feature/chat/src/main/java/.../views/TypewriterText.kt` — line ~44
- **Code:** `feature/explore/src/main/java/.../ExploreScreen.kt` — line ~198

### 3. AI Persona Selector

The chat uses a hardcoded "AI Overlord" persona with a 42-word max response length. Offer preset personas (e.g., concise 
assistant, creative storyteller, technical expert) or a free-text system instruction field.

- **Code:** `data/chat/src/main/java/.../repository/ChatGeminiRepositoryImpl.kt` — `SYSTEM_INSTRUCTION` constant and `"maximum of 42 words"` embedded in the prompt

### 4. Walking Speed

Route time estimates assume 5.0 km/h in two independent places. Expose a "walking speed" setting (slow 3 / normal 5 / 
fast 7 km/h) and feed it to both locations to keep them consistent.

- **Code:** `data/plan/src/main/java/.../tools/RouteCalculator.kt` — `WALKING_SPEED_KMH = 5.0`
- **Code:** `feature/explore/src/main/java/.../ExploreViewModel.kt` — `WALKING_SPEED_METERS_PER_MIN = 83.0` (equivalent to 5 km/h, duplicated independently)

### 5. Firebase Sync Toggle

Cloud sync to Firestore is always attempted when authenticated. Add an opt-out toggle so users can keep prompts 
local-only even when signed in.

- **Code:** `data/chat/src/main/java/.../sync/SyncWorker.kt` — sync is always enqueued
- **Code:** `data/chat/src/main/java/.../repository/PromptRepositoryImpl` — calls enqueue unconditionally

---

## Tier 2 — Medium Impact, Medium Effort

### 6. Gemini Model Picker

The model name (`gemini-3-flash-preview`) is hardcoded in the Retrofit `@POST` annotation. Allow switching between models
(e.g., Flash, Pro, Flash Preview) at runtime via a dynamic base path or query param.

- **Code:** `core/network/src/main/java/.../api/GeminiApiService.kt` — `@POST("v1beta/models/gemini-3-flash-preview:generateContent")`

### 7. Agent Max Iterations

The agentic trip planner loop hard-stops at 10 iterations and the system prompt says "no more than 5 tools." Let users
pick a budget (quick 5 / standard 10 / thorough 15) to trade speed for depth.

- **Code:** `data/plan/src/main/java/.../repository/TripPlannerRepositoryImpl.kt` — `MAX_ITERATIONS = 10`, `DEFAULT_COUNT = 5`
- **Code:** Same file, `buildSystemPrompt()` — `"Do NOT call more than 5 tools total"`

### 8. Image Quality / Compression

Images sent to Gemini are scaled to max 768 px and compressed at JPEG quality 77. A slider or presets (low / medium / high) 
would let users balance upload size vs. detail.

- **Code:** `data/chat/src/main/java/.../repository/ChatGeminiRepositoryImpl.kt` — `MAX_IMAGE_SIZE = 768`, `COMPRESSION_QUALITY = 77`

### 9. AI Suggested Places Count

The map feature asks Gemini for exactly 10 suggested places. A slider (5–20) would let users control how many AI suggestions
appear.

- **Code:** `data/explore/src/main/java/.../repository/ExploreSuggestionsRepositoryImpl.kt` — `SUGGESTED_PLACES_COUNT = 10`

### 10. Max Route Points

Users can select up to 8 points for route calculation. Raising or lowering this (3–12) gives control over route complexity.

- **Code:** `feature/explore/src/main/java/.../ExploreConstants.kt` — `MAX_SELECTABLE_POINTS = 8`

---

## Tier 3 — Nice to Have / Power User

### 11. Network Timeout

API calls time out after 30 seconds. A slider (15–120 s) would help users on slow connections or when using heavier models.

- **Code:** `core/network/src/main/java/.../di/NetworkModule.kt` — `TIMEOUT_SECONDS = 30L`

### 12. Default Map Location

The map always centers on Stockholm (59.3293, 18.0686) — hardcoded in three independent places. Add a "home location" 
setting or auto-detect from last-known GPS.

- **Code:** `data/explore/src/main/java/.../util/ExploreDataGenerator.kt` — `CENTER_LAT / CENTER_LNG`
- **Code:** `feature/explore/src/main/java/.../ExploreConstants.kt` — `STOCKHOLM_LAT / STOCKHOLM_LNG`
- **Code:** `feature/plan/src/main/java/.../PlanViewModel.kt` — duplicated `STOCKHOLM_LAT / STOCKHOLM_LNG`

### 13. Trip Length Preset

The agent system prompt targets "4–6 stops for a half-day trip." Offer presets (quick 2–3 stops / standard 4–6 / extended 
7–10) so the user can control itinerary size.

- **Code:** `data/plan/src/main/java/.../repository/TripPlannerRepositoryImpl.kt` — `buildSystemPrompt()` contains `"Keep the itinerary to 4-6 stops for a half-day trip."`

### 14. Token Usage Tracking Toggle

Token usage is always tracked to the local Room database. Some users may want to disable tracking for privacy or storage 
reasons.

- **Code:** `data/chat/src/main/java/.../tracking/TokenUsageTrackerImpl` — tracking runs unconditionally
- **Code:** `data/chat/src/main/java/.../local/dao/TokenUsageDao.kt` — always written to

### 15. Map Theme Override

The map style follows the system dark/light theme with no manual override. Add a three-way toggle (system / light / dark) 
for the map tiles specifically.

- **Code:** `feature/explore/src/main/java/.../ExploreScreen.kt` — map style follows system theme (line ~236–241)

---

## Bonus: Bug to Fix

**Hardcoded app version** — `SettingsBottomSheetContainer.kt` passes `appVersion = "1.0"` as a string literal instead of 
reading from `BuildConfig.VERSION_NAME`.

- **Code:** `app/src/main/java/.../settings/SettingsBottomSheetContainer.kt` — line ~39
