# IDEAS.md — Settings Panel Feature Ideas

Brainstorming ideas for new settings to add to the Settings bottom sheet.
The current panel has: vehicle count slider, search radius slider, weekly usage chart, and about section.

Each idea below is backed by a hardcoded value or missing toggle found in the codebase.

---

### 1. [COMPLETED] Typewriter Speed / Instant Mode
Toggle the character-by-character typing animation off entirely, or offer speed presets (slow / normal / fast / instant). 
Power users and accessibility-minded users will want to read the full answer immediately.


### 2. [COMPLETED] Haptic Feedback Toggle
The app fires `HapticFeedbackConstants.CLOCK_TICK` on every typed character in chat and on map interactions. 
There is no way to turn this off.


### 3. AI Persona Selector
The chat uses a hardcoded "AI Overlord" persona with a 42-word max response length. Offer preset personas (e.g., concise
assistant, creative storyteller, technical expert) or a free-text system instruction field.


### 4. [COMPLETED] Walking Speed
Route time estimates assume 5.0 km/h in two places. Expose a "walking speed" setting (slow 3 / normal 5 / 
fast 7 km/h) and feed it to both locations to keep them consistent.


### 5. [COMPLETED] Firebase Sync Toggle
Cloud sync to Firestore is always attempted when authenticated. Add an opt-out toggle so users can keep prompts 
local-only even when signed in.


### 6. Gemini Model Picker
The model name (`gemini-3-flash-preview`) is hardcoded in the Retrofit `@POST` annotation. Allow switching between models
(e.g., Flash, Pro, Flash Preview) at runtime via a dynamic base path or query param.


### 7. [COMPLETED] Agent Max Iterations
The agentic trip planner loop hard-stops at 10 iterations and the system prompt says "no more than 5 tools." Let users
pick a budget (quick 5 / standard 10 / thorough 15) to trade speed for depth.


### 8. [COMPLETED] Image Quality / Compression
Images sent to Gemini are scaled to max 768 px and compressed at JPEG quality 77. A slider or presets (low / medium / high) 
would let users balance upload size vs. detail.


### 9. [COMPLETED] AI Suggested Places Count
The map feature asks Gemini for exactly 10 suggested places. A slider (5–20) would let users control how many AI suggestions appear.


### 10. [COMPLETED] Max Route Points
Users can select up to 8 points for route calculation. Raising or lowering this (3–12) gives control over route complexity.


### 11. [COMPLETED] Network Timeout
API calls time out after 30 seconds. A slider (15–120 s) would help users on slow connections or when using heavier models.


### 12. Default Map Location
Both Explore and Plan screens now fetch the user's real GPS location via `FusedLocationProviderClient` and fall back to
Stockholm (59.3293, 18.0686) if unavailable. A "home location" setting could let users override the fallback.


### 13. [COMPLETED] Trip Length Preset
The agent system prompt targets "4–6 stops for a half-day trip." Offer presets (quick 2–3 stops / standard 4–6 / extended
7–10) so the user can control itinerary size.


### 14. [COMPLETED] Token Usage Tracking Toggle
Token usage is always tracked to the local Room database. Some users may want to disable tracking for privacy or storage 
reasons.

---
