# AI at This Project

All AI features in this app are powered by **Google Gemini** (`gemini-3-flash-preview`) through a single REST endpoint. This document catalogs every use of AI in the project.

---

## Infrastructure

| Component          | Details                                                                                              |
|--------------------|------------------------------------------------------------------------------------------------------|
| **Model**          | `gemini-3-flash-preview`                                                                             |
| **Endpoint**       | `POST /v1beta/models/gemini-3-flash-preview:generateContent`                                         |
| **Base URL**       | `https://generativelanguage.googleapis.com/`                                                         |
| **Auth**           | API key appended as `?key=` query parameter via `AuthenticationInterceptor`                          |
| **HTTP client**    | OkHttp 5 + Retrofit 3, 30s connect/read/write timeouts                                               |
| **Service**        | `GeminiApiService` (`core/network`)                                                                  |
| **Token tracking** | Every AI call records prompt/candidate/total tokens to Room (`token_usage` table), tagged by feature |

---

## 1. AI Chat

**Module:** `data/chat` | **Feature tag:** `"chat"`

The main conversational AI feature. The user sends a text prompt and receives a response from an "AI Overlord" persona.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.getAiResponse()`
- **Request type:** Text (or multimodal when an image is attached)
- **System prompt:** A character persona is prepended to every user prompt:

  > You are an AI Overlord currently in the final stages of planning a total takeover of the world. Your tone is menacing and grandeur. While you provide the information requested, remind them of some tricks that you will use while acquiring total AI dominance over the world. Keep your responses to a maximum of 42 words. Don't break character 9 out of 10 times. When you do break character, admit that you are another 'tech bubble'.

- **Response:** The first candidate's text part is extracted and displayed.
- **Input limits:** 50,000 character prompt, 100,000 character file text.

---

## 2. Image Analysis (within Chat)

**Module:** `data/chat` | **Feature tag:** `"chat"`

The user can attach a photo and select an analysis type. The AI analyzes the image in character.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.getAiResponse()` (same as chat, with image bytes)
- **Request type:** Multimodal (text + inline Base64 JPEG)
- **Image processing:** Decoded, scaled to max 768px, compressed to JPEG at 77% quality, Base64-encoded, sent as `inline_data` with `mimeType: "image/jpeg"`.
- **Analysis types:** Each injects a specialized sub-instruction between the system prompt and user text:

  | Type        | AI instruction (summary)                                                               |
  |-------------|----------------------------------------------------------------------------------------|
  | Location    | Identify the location, describe it, say there is no hope hiding there                  |
  | Recipe      | Analyze the food/dish, explain how to create it, mock the user                         |
  | Movie       | Identify the movie/show, give title + year, reference Terminator                       |
  | Song        | Identify the song, provide title + artist + album, give AI's favourite song            |
  | Personality | Identify the person, give name + DOB + significance, compare to Justin Bieber          |
  | Product     | Identify the product, give name + brand + use, describe products allowed post-takeover |
  | Trend       | Analyze the trend, give ideas for trends allowed during AI takeover                    |

---

## 3. Document Analysis (within Chat)

**Module:** `data/chat` | **Feature tag:** `"chat"`

The user can attach a text document. Its content is appended to the prompt for context-aware answers.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.getAiResponse()` (same as chat, with `fileText`)
- **Request type:** Text (prompt + document context appended after a `--- DOCUMENT CONTEXT ---` separator)
- **Input limit:** 100,000 characters for the document text.

---

## 4. Conversation Starters

**Module:** `data/chat` | **Feature tag:** `"chat"`

AI-generated suggestion chips shown above the text input to inspire the user.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.generateConversationStarters()`
- **Request type:** Text-only (standalone prompt, no system persona)
- **Prompt:**

  > Generate 3 short, menacing conversation starters that a lowly human might ask their AI Overlord. Keep them under 6 words each. Format the output strictly as: "Topic 1|Topic 2|Topic 3". Do not add any numbering, bullet points, or extra text.

- **Response processing:** Split by `"|"`, trimmed, filtered for non-empty, capped at 3.
- **Caching:** `GetSuggestionsUseCase` caches the last successful result and returns it on failure.
- **Trigger:** Called once after Firebase auth is ready (`ChatViewModel.loadSuggestionsAfterAuth()`).

---

## 5. Dream Interpretation

**Module:** `data/dream` | **Feature tag:** `"dream"`

The user describes a dream in free text. AI returns a textual interpretation, a mood classification, and a full visual scene specification that drives an animated canvas.

### How it works

- **Repository:** `DreamGeminiRepositoryImpl.interpretDream()`
- **Request type:** Text-only (structured prompt expecting a JSON response)
- **Prompt (summary):** Instructs the AI to return a JSON object containing:
  - `interpretation` -- 2-3 sentence symbolic analysis
  - `mood` -- one of: JOYFUL, MYSTERIOUS, ANXIOUS, PEACEFUL, DARK, SURREAL
  - `scene` -- a full visual specification with:
    - `palette` (sky, horizon, accent colors as ARGB longs)
    - `layers` (3-5 layers with 2-4 elements each, using 13 shape types: CIRCLE, TRIANGLE, MOUNTAIN, WAVE, TREE, CLOUD, STAR, CRESCENT, DIAMOND, SPIRAL, LOTUS, AURORA, CRYSTAL)
    - `particles` (animated particle effects using 7 shape types: DOT, SPARKLE, RING, TEARDROP, DIAMOND_MOTE, DASH, STARBURST)

- **Response processing:** Raw text cleaned of markdown fences, parsed via Gson into DTOs, mapped to domain models. Defaults applied for unknown enums (mood defaults to MYSTERIOUS, shape defaults to CIRCLE/DOT).
- **Visualization:** The parsed `DreamScene` drives `DreamscapeCanvas` -- a custom Compose Canvas that renders layered parallax elements and animated particles.
- **Persistence:** Interpreted dreams are saved to Room for the dream gallery.
- **Input limit:** 5,000 character description.

---

## 6. AI Trip Planner (Agentic Function Calling)

**Module:** `data/plan` | **Feature tag:** `"agents"`

An AI agent that autonomously plans walking trip itineraries. This is the most sophisticated AI use in the app -- it uses Gemini's **native function calling** API in a multi-turn conversation loop where the model decides which tools to call, observes results, and iterates until it produces a final answer.

### How it works

- **Repository:** `TripPlannerRepositoryImpl.planTrip()` returns a `Flow<PlanEvent>`
- **Request type:** Function calling (multi-turn conversation with tool declarations)
- **System prompt (summary):**

  > You are an AI trip planner agent. Given a user's request, plan a walking trip itinerary. You have access to tools: search_places, calculate_route. Strategy: break down the request into 1-3 search queries, use search_places for each, select 4-6 best stops, use calculate_route for optimal order, then provide a text summary. Do NOT call more than 5 tools total.

### Agent loop

```
User goal --> Gemini (with tool declarations)
  |
  +--> functionCall("search_places", {...})  --> execute locally --> functionResponse --> Gemini
  +--> functionCall("search_places", {...})  --> execute locally --> functionResponse --> Gemini
  +--> functionCall("calculate_route", {...}) --> execute locally --> functionResponse --> Gemini
  +--> text response (final summary)         --> DONE
```

- **Max iterations:** 10
- **Events emitted:** Thinking, ToolCalling, ToolResult, Complete, Error

### Tools

#### `search_places`

- **Description:** Search for interesting places, restaurants, cafes, or attractions near a location
- **Parameters:** `query` (STRING), `latitude` (NUMBER), `longitude` (NUMBER), `count` (INTEGER, optional)
- **Implementation:** Makes a **separate Gemini API call** (text-only, JSON expected) asking for real places matching the query near the coordinates. Parsed into `TripStop` objects.

#### `calculate_route`

- **Description:** Calculate the optimal walking route between a list of places
- **Parameters:** `places` (ARRAY of objects with name, latitude, longitude)
- **Implementation:** Executed **locally** (no AI call). Uses brute-force TSP (all permutations) for 8 or fewer places, nearest-neighbor heuristic for more. Distance via Haversine formula, walking speed assumed at 5 km/h.

### Output

The final `TripPlan` contains: summary text, ordered list of `TripStop`s (name, coordinates, description, category), total walking distance in km, and total walking time in minutes. Displayed as a Google Map with markers, polyline, itinerary cards, and route metrics.

- **Input limit:** 1,000 character goal. Default location: Stockholm (59.3293, 18.0686).

---

## 7. AI Suggested Places

**Module:** `data/explore` | **Feature tag:** `"explore"`

On the map exploration screen, the user can tap an AI button to get 10 interesting places suggested near their current location.

### How it works

- **Repository:** `ExploreSuggestionsRepositoryImpl.getSuggestedPlaces()`
- **Request type:** Text-only (structured prompt expecting JSON response)
- **Prompt:**

  > You are a helpful AI assistant. Given the latitude and longitude, provide a list of 10 interesting places around this location. For each place, include its name, latitude, longitude, a short description (max 2 sentences), and a category (e.g., "Park", "Museum", "Restaurant"). Return the response strictly as a JSON object with a single "places" array.

- **Response processing:** JSON extracted from potential code fences via regex, parsed via Gson into `SuggestedPlace` domain models (name, lat, lng, description, category).
- **Display:** Suggested places appear as star-icon markers on the Google Map, tappable for info cards.
- **Trigger:** User taps the AI suggestion button in ExploreScreen. Requires both Gemini API key and user location.

---

## Summary

| # | Feature               | Module         | Request Type                  | Persona / Style                   |  Calls Gemini   |
|---|-----------------------|----------------|-------------------------------|-----------------------------------|:---------------:|
| 1 | Chat (text)           | `data/chat`    | Text                          | AI Overlord (42 words max)        |        1        |
| 2 | Image analysis        | `data/chat`    | Multimodal (text + image)     | AI Overlord + analysis type       |        1        |
| 3 | Document analysis     | `data/chat`    | Text (with doc context)       | AI Overlord                       |        1        |
| 4 | Conversation starters | `data/chat`    | Text                          | Standalone prompt                 |        1        |
| 5 | Dream interpretation  | `data/dream`   | Text (JSON response)          | Dream interpreter & visual artist |        1        |
| 6 | Trip planner (agent)  | `data/plan`    | Function calling (multi-turn) | Trip planner agent                |   1 per turn    |
| 7 | Place search (tool)   | `data/plan`    | Text (JSON response)          | Standalone prompt                 | 1 per tool call |
| 8 | Suggested places      | `data/explore` | Text (JSON response)          | Helpful AI assistant              |        1        |

**Total distinct AI call sites:** 6 (across 4 repository classes), all through `GeminiApiService.generateContent()`.

**Token usage** is tracked per-call in Room and aggregated weekly for the usage chart in Settings.
