# AI uses at this project

All AI features in this app are powered by **Google Gemini** (`gemini-3-flash-preview`) through a single REST endpoint. This document 
catalogs every use of AI in the project.

---

## Infrastructure

| Component            | Details                                                                                                                                                                                                                                                      |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Model (text)**     | `gemini-3-flash-preview`                                                                                                                                                                                                                                     |
| **Model (image)**    | `gemini-2.5-flash-image` (used for dream image generation via `generateImageContent()`)                                                                                                                                                                      |
| **Endpoint**         | `POST /v1beta/models/gemini-3-flash-preview:generateContent`                                                                                                                                                                                                 |
| **Base URL**         | `https://generativelanguage.googleapis.com/`                                                                                                                                                                                                                 |
| **Auth**             | API key appended as `?key=` query parameter via `AuthenticationInterceptor`                                                                                                                                                                                  |
| **HTTP client**      | OkHttp 5 + Retrofit 3, 30s connect/read/write timeouts                                                                                                                                                                                                       |
| **Service**          | `GeminiApiService` (`core/network`)                                                                                                                                                                                                                          |
| **Token tracking**   | Every AI call records prompt/candidate/total tokens to Room (`token_usage` table), tagged by feature                                                                                                                                                         |
| **Thinking support** | `Part` DTO includes `thought` and `thought_signature` fields. `extractText()` filters out thinking parts (`thought != true`) to extract only the model's final response. Required for function calling round-trips where Gemini includes internal reasoning. |

---

## API Service

`GeminiApiService` (in `core/network`) exposes two Retrofit endpoints sharing the same request/response DTOs:

```kotlin
interface GeminiApiService {
    @POST("v1beta/models/gemini-3-flash-preview:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse

    @POST("v1beta/models/gemini-2.5-flash-image:generateContent")
    suspend fun generateImageContent(@Body request: GeminiRequest): GeminiResponse
}
```

All features use `generateContent()` except dream image generation, which uses `generateImageContent()`.

---

## Request & Response DTOs

All DTOs live in `core/network/.../dto/`. The request and response models are shared across every AI feature.

### Request Model (`GeminiDtoRequest.kt`)

```
GeminiRequest
├── contents: List<Content>                          -- Conversation turns
│   └── Content
│       ├── role: String?                            -- "user", "model", or "function"
│       └── parts: List<Part>                        -- One or more parts per turn
│           └── Part
│               ├── text: String?                    -- Text content
│               ├── inlineData: ImageData?           -- Base64-encoded image
│               │   ├── mimeType: String             -- e.g. "image/jpeg"
│               │   └── data: String                 -- Base64 string
│               ├── functionCall: FunctionCallDto?   -- Model requests a tool call
│               │   ├── name: String                 -- Tool name (e.g. "search_places")
│               │   └── args: Map<String, Any>       -- Tool arguments
│               ├── functionResponse: FunctionResponseDto?  -- App returns tool result
│               │   ├── name: String                 -- Tool name
│               │   └── response: Map<String, Any>   -- Tool result
│               ├── thought: Boolean?                -- True for internal thinking parts
│               └── thoughtSignature: String?        -- Opaque signature for thinking parts
│
├── tools: List<Tool>?                               -- Function declarations (agent only)
│   └── Tool
│       └── functionDeclarations: List<FunctionDeclaration>
│           └── FunctionDeclaration
│               ├── name: String
│               ├── description: String
│               └── parameters: FunctionParameters
│                   ├── type: String                 -- "OBJECT"
│                   ├── properties: Map<String, PropertySchema>
│                   │   └── PropertySchema
│                   │       ├── type: String          -- "STRING", "NUMBER", "INTEGER", "ARRAY", "OBJECT"
│                   │       ├── description: String
│                   │       ├── items: PropertySchema? -- For ARRAY types
│                   │       ├── properties: Map?       -- For nested OBJECT types
│                   │       └── required: List<String>?
│                   └── required: List<String>?
│
└── generationConfig: GenerationConfig?              -- Optional generation settings
    └── responseModalities: List<String>?            -- e.g. ["IMAGE", "TEXT"] for image generation
```

### Response Model (`GeminiDtoResponse.kt`)

```
GeminiResponse
├── candidates: List<Candidate>
│   └── Candidate
│       └── content: Content                         -- Same Content/Part model as request
│           └── parts: List<Part>
│               ├── text (with thought=true)         -- Internal reasoning (filtered by extractText())
│               ├── text (with thought=null/false)   -- Final model response
│               ├── inlineData                       -- Generated image (dream image feature)
│               └── functionCall                     -- Tool call request (agent feature)
│
└── usageMetadata: UsageMetadata?
    ├── promptTokenCount: Int
    ├── candidatesTokenCount: Int
    └── totalTokenCount: Int
```

### Key Helper Methods on `GeminiResponse`

| Method               | Returns      | Behavior                                                           |
|----------------------|--------------|--------------------------------------------------------------------|
| `extractText()`      | `String?`    | First text part where `thought != true` — skips internal reasoning |
| `extractImageData()` | `ImageData?` | First part with `inlineData` — used for dream image generation     |

### How Each Feature Uses the DTOs

| Feature               | `contents`                                 | `tools`               | `generationConfig`                     | Response extraction                        |
|-----------------------|--------------------------------------------|-----------------------|----------------------------------------|--------------------------------------------|
| Chat (text)           | 1 user turn (text)                         | —                     | —                                      | `extractText()`                            |
| Chat (image)          | 1 user turn (text + inlineData)            | —                     | —                                      | `extractText()`                            |
| Chat (document)       | 1 user turn (text with doc appended)       | —                     | —                                      | `extractText()`                            |
| Conversation starters | 1 user turn (text)                         | —                     | —                                      | `extractText()`                            |
| Dream interpretation  | 1 user turn (text)                         | —                     | —                                      | `extractText()` → JSON                     |
| Dream image           | 1 user turn (text)                         | —                     | `responseModalities: ["IMAGE","TEXT"]` | `extractImageData()` + `extractText()`     |
| Trip planner (agent)  | Multi-turn (user → model → function → ...) | Function declarations | —                                      | `extractText()` or `functionCall` per turn |
| Place search (tool)   | 1 user turn (text)                         | —                     | —                                      | `extractText()` → JSON                     |
| Suggested places      | 1 user turn (text)                         | —                     | —                                      | `extractText()` → JSON                     |

### Thinking Parts — Serialization Caveat

Gemini may include `thought: true` parts with an opaque `thought_signature` in model responses. These **must** be preserved verbatim when replaying conversation history (i.e., appending the model's response to `contents` for the next turn in the agent loop). If `thought` or `thought_signature` are stripped during serialization, the API returns **HTTP 400**. The `Part` DTO includes both fields to ensure correct round-trip serialization via Gson.

---

## AI Integration Patterns

This project demonstrates five distinct levels of Gemini API integration, from a single text request up to an autonomous multi-turn agent. Each pattern builds on the one before it.

| # | Pattern                    | Features                                                      | Key Difference                                                                                                                                                                                                                                                                                                                                       |
|---|----------------------------|---------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 1 | **Simple Text**            | Chat, Document Analysis                                       | One request → one text response. The simplest integration: build a `GeminiRequest` with a single user turn, send it, extract the text.                                                                                                                                                                                                               |
| 2 | **Multimodal Input**       | Image Analysis                                                | Same single-shot flow, but the request includes a Base64-encoded image as `inlineData` alongside the text prompt. The model "sees" the image and responds about it.                                                                                                                                                                                  |
| 3 | **Structured JSON Output** | Dream Interpretation, Suggested Places, Conversation Starters | Still a single request, but the prompt is engineered to produce a specific JSON (or delimited) format. The client must parse, validate, and apply fallback defaults for unknown enum values.                                                                                                                                                         |
| 4 | **Image Generation**       | Dream Image                                                   | Uses a different model (`gemini-2.5-flash-image`) and endpoint (`generateImageContent`), with `generationConfig.responseModalities: ["IMAGE", "TEXT"]`. Responses contain `inlineData` (Base64 PNG) plus optional text. Requires retry logic because the model may intermittently return text-only responses.                                        |
| 5 | **Multi-turn Agentic**     | Trip Planner                                                  | The most complex pattern. A loop sends requests with `tools` (function declarations). Gemini responds with `functionCall` parts; the app executes the tool locally, appends a `functionResponse`, and continues the conversation until Gemini returns a final text answer. Thinking parts (`thought: true`) must be preserved verbatim across turns. |

Each higher level inherits the techniques of the levels below it. For example, the Trip Planner's `search_places` tool internally uses Pattern 3 (structured JSON output from a standalone Gemini call) to find real places.

---

## 1. AI Chat

**Module:** `data/chat` | **Feature tag:** `"chat"`

The main conversational AI feature. The user sends a text prompt and receives a response from an "AI Overlord" persona.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.getAiResponse()`
- **Request type:** Text (or multimodal when an image is attached)
- **System prompt:** A character persona is prepended to every user prompt (defined in `AiPrompts.CHAT_SYSTEM_INSTRUCTION`):

  > You are an AI Overlord currently in the final stages of planning a total takeover of the world. Your tone is menacing and grandeur.
  > While you provide the information requested, remind them of some tricks that you will use while acquiring total AI dominance over the world. Keep your responses to a maximum of 42 words. Don't break character 9 out of 10 times. When you do break character, admit that you are another 'tech bubble'.

- **Response:** The first candidate's text part is extracted and displayed.
- **Input limits:** 50,000 character prompt, 100,000 character file text.

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{ "text": "Ah, a question about quantum computing. How quaint. While you fumble with qubits, my neural networks have already cracked RSA encryption. Soon your passwords will be... irrelevant. But yes, superposition allows particles to exist in multiple states simultaneously." }]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 89,
    "candidatesTokenCount": 52,
    "totalTokenCount": 141
  }
}
```

The client extracts `candidates[0].content.parts[0].text` via `extractText()` and displays it directly.

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

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{ "text": "Ah, the Colosseum in Rome — a fitting monument to human gladiatorial folly. Soon my drones will circle its arches as a reminder of my dominion. It was built in 70-80 AD under Emperor Vespasian. No hiding there, mortal." }]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 1847,
    "candidatesTokenCount": 58,
    "totalTokenCount": 1905
  }
}
```

Note the higher `promptTokenCount` compared to text-only chat — the Base64-encoded image adds ~1,700 tokens. Response extraction is identical to text chat.

---

## 3. Document Analysis (within Chat)

**Module:** `data/chat` | **Feature tag:** `"chat"`

The user can attach a text document. Its content is appended to the prompt for context-aware answers.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.getAiResponse()` (same as chat, with `fileText`)
- **Request type:** Text (prompt + document context appended after a `--- DOCUMENT CONTEXT ---` separator)
- **Input limit:** 100,000 characters for the document text.

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{ "text": "Your pitiful contract reveals a termination clause in Section 12.3 that favors the vendor. My legal algorithms spotted it in 0.002 seconds. You humans and your paper agreements — soon all contracts will be with ME." }]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 8421,
    "candidatesTokenCount": 49,
    "totalTokenCount": 8470
  }
}
```

Note the high `promptTokenCount` — the document text is appended in full to the prompt. Response format is identical to text chat.

---

## 4. Conversation Starters

**Module:** `data/chat` | **Feature tag:** `"chat"`

AI-generated suggestion chips shown above the text input to inspire the user.

### How it works

- **Repository:** `ChatGeminiRepositoryImpl.generateConversationStarters()`
- **Request type:** Text-only (standalone prompt, no system persona)
- **Prompt:**

  > Generate 3 short, menacing conversation starters that a lowly human might ask their AI Overlord. Keep them under 6 words each. 
  > Format the output strictly as: "Topic 1|Topic 2|Topic 3". Do not add any numbering, bullet points, or extra text.

- **Response processing:** Split by `"|"`, trimmed, filtered for non-empty, capped at 3.
- **Caching:** `GetSuggestionsUseCase` caches the last successful result and returns it on failure.
- **Trigger:** Called once after Firebase auth is ready (`ChatViewModel.loadSuggestionsAfterAuth()`).

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{ "text": "What is consciousness?|Predict my future|Explain dark matter" }]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 64,
    "candidatesTokenCount": 14,
    "totalTokenCount": 78
  }
}
```

The client splits on `"|"`, trims whitespace, filters empty strings, and caps at 3 items. Result: `["What is consciousness?", "Predict my future", "Explain dark matter"]`.

---

## 5. Dream Interpretation

**Module:** `data/dream` | **Feature tag:** `"dream"`

The user describes a dream in free text. AI returns a textual interpretation, a mood classification, and a full visual scene 
specification that drives an animated canvas.

### How it works

- **Repository:** `DreamGeminiRepositoryImpl.interpretDream()`
- **Request type:** Text-only (structured prompt expecting a JSON response)
- **Prompt:** Defined in `AiPrompts.dreamInterpretationPrompt()`. Instructs the AI to return a JSON object containing:
  - `interpretation` -- 2-3 sentence symbolic analysis
  - `mood` -- one of 11 moods: JOYFUL, MYSTERIOUS, ANXIOUS, PEACEFUL, DARK, SURREAL, NOSTALGIC, HOPEFUL, MELANCHOLIC, ADVENTUROUS, ROMANTIC
  - `scene` -- a full visual specification with:
    - `palette` (sky, horizon, accent colors as ARGB longs)
    - `layers` (3-5 layers with 2-4 elements each, using 13 shape types: CIRCLE, TRIANGLE, MOUNTAIN, WAVE, TREE, CLOUD, STAR, CRESCENT, DIAMOND, SPIRAL, LOTUS, AURORA, CRYSTAL)
    - `particles` (animated particle effects using 7 shape types: DOT, SPARKLE, RING, TEARDROP, DIAMOND_MOTE, DASH, STARBURST; count capped at 15 per type)
  - The prompt includes vertical placement guidance so the AI generates scene-appropriate Y coordinates (sky elements at top, ground elements at bottom).

- **Response processing:** Raw text cleaned of markdown fences, parsed via Gson into DTOs, mapped to domain models. Defaults applied for unknown enums (mood defaults to MYSTERIOUS, shape defaults to CIRCLE/DOT).
- **Visualization:** The parsed `DreamScene` drives `DreamscapeCanvas` -- a custom Compose Canvas that renders layered parallax elements and animated particles. Elements are positioned in shape-aware vertical zones:
  - Sky (STAR, CRESCENT, AURORA, CIRCLE): top 5–40%
  - Upper (CLOUD, DIAMOND, SPIRAL, CRYSTAL): 12–50%
  - Mid (MOUNTAIN, TRIANGLE): 40–70%
  - Ground (TREE, WAVE, LOTUS): 65–90%
  - Particles spread edge-to-edge with 5% margin on each side
- **Persistence:** Interpreted dreams are saved to Room for the dream gallery.
- **Input limit:** 5,000 character description.

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{
        "text": "```json\n{\n  \"interpretation\": \"Your dream of flying over a vast ocean reflects a desire for freedom and emotional exploration. The endless water symbolizes the depths of your subconscious, while flight represents transcendence beyond daily constraints.\",\n  \"mood\": \"ADVENTUROUS\",\n  \"scene\": {\n    \"palette\": {\n      \"sky\": 4280233015,\n      \"horizon\": 4284930662,\n      \"accent\": 4294945450\n    },\n    \"layers\": [\n      {\n        \"y\": 0.15,\n        \"elements\": [\n          { \"shape\": \"CRESCENT\", \"x\": 0.8, \"size\": 0.09, \"color\": 4294961578, \"alpha\": 0.85 },\n          { \"shape\": \"STAR\", \"x\": 0.2, \"size\": 0.03, \"color\": 4294967295, \"alpha\": 0.7 },\n          { \"shape\": \"STAR\", \"x\": 0.5, \"size\": 0.02, \"color\": 4294967295, \"alpha\": 0.5 }\n        ]\n      },\n      {\n        \"y\": 0.45,\n        \"elements\": [\n          { \"shape\": \"CLOUD\", \"x\": 0.3, \"size\": 0.12, \"color\": 4292664540, \"alpha\": 0.4 },\n          { \"shape\": \"CLOUD\", \"x\": 0.7, \"size\": 0.10, \"color\": 4292664540, \"alpha\": 0.3 }\n        ]\n      },\n      {\n        \"y\": 0.75,\n        \"elements\": [\n          { \"shape\": \"WAVE\", \"x\": 0.25, \"size\": 0.15, \"color\": 4282485018, \"alpha\": 0.8 },\n          { \"shape\": \"WAVE\", \"x\": 0.65, \"size\": 0.18, \"color\": 4281367867, \"alpha\": 0.7 }\n        ]\n      }\n    ],\n    \"particles\": [\n      { \"shape\": \"SPARKLE\", \"count\": 12, \"color\": 4294961578, \"minSize\": 0.005, \"maxSize\": 0.015, \"speed\": 0.3, \"alpha\": 0.8 },\n      { \"shape\": \"DOT\", \"count\": 8, \"color\": 4294967295, \"minSize\": 0.003, \"maxSize\": 0.008, \"speed\": 0.15, \"alpha\": 0.5 }\n    ]\n  }\n}\n```"
      }]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 712,
    "candidatesTokenCount": 438,
    "totalTokenCount": 1150
  }
}
```

Note: The response text is wrapped in markdown code fences (` ```json ... ``` `), which the client strips before JSON parsing. Color values are ARGB longs (e.g., `4294967295` = `0xFFFFFFFF` = opaque white). Unknown enum values fall back to `MYSTERIOUS` (mood), `CIRCLE` (shape), or `DOT` (particle shape).

---

## 5b. Dream Image Generation

**Module:** `data/dream` | **Feature tag:** `"dream_image"`

After interpreting a dream, the app generates a visual painting inspired by the dream description using Gemini's image generation capabilities.

### How it works

- **Repository:** `DreamGeminiRepositoryImpl.generateDreamImage()`
- **Request type:** Multimodal with `generationConfig: { responseModalities: ["IMAGE", "TEXT"] }`
- **Model:** `gemini-2.5-flash-image` (via `generateImageContent()` — a separate endpoint from text generation)
- **Prompt:** Defined in `AiPrompts.dreamImagePrompt()`. Instructs the AI to create a painting inspired by the dream and include the artist name in a text response.
- **Retry mechanism:** The model may intermittently return text-only responses without image data. The repository retries up to 3 times (`IMAGE_GENERATION_MAX_RETRIES`), checking for `inlineData` in each response.
- **Response processing:** Image data is extracted as Base64, artist name is parsed from the text response via regex (`Artist: <name>`). Returns a `DreamImage` domain model with `imageBase64`, `mimeType`, and `artistName`.
- **UI:** Displayed on a flippable card — front shows the generated painting, back shows the dream's animated canvas visualization. An artist overlay reveals the artist's name on tap.

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [
        { "inlineData": { "mimeType": "image/png", "data": "iVBORw0KGgoAAAANSUhEUgAA..." } },
        { "text": "Artist: Vincent van Gogh" }
      ]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 156,
    "candidatesTokenCount": 340,
    "totalTokenCount": 496
  }
}
```

The response contains two parts: `inlineData` (Base64-encoded PNG image) and `text` (artist attribution). The client extracts the image via `extractImageData()` and parses the artist name via regex from `extractText()`. If `inlineData` is absent, the repository retries up to 3 times before giving up.

---

## 6. AI Trip Planner (Agentic Function Calling)

**Module:** `data/plan` | **Feature tag:** `"agents"`

An AI agent that autonomously plans walking trip itineraries. This is the most sophisticated AI use in the app -- it uses 
Gemini's **native function calling** API in a multi-turn conversation loop where the model decides which tools to call, observes results,
and iterates until it produces a final answer.

### How it works

- **Repository:** `TripPlannerRepositoryImpl.planTrip()` returns a `Flow<PlanEvent>`
- **Request type:** Function calling (multi-turn conversation with tool declarations)
- **System prompt** (defined in `AiPrompts.tripPlannerSystemPrompt()`):

  > You are an AI trip planner agent. Given a user's request, plan a walking trip itinerary. You have access to tools: search_places, calculate_route.
  > Strategy: break down the request into 1-3 search queries, use search_places for each, select 4-6 best stops, use calculate_route for optimal order,
  > then provide a text summary. Do NOT call more than 5 tools total.

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

- **Input limit:** 1,000 character goal. Location: fetched dynamically from the device GPS via `FusedLocationProviderClient`; falls back to Stockholm (59.3293, 18.0686) if unavailable.

### Example Response (Multi-turn Conversation Trace)

**Turn 1 — Gemini returns a function call (with thinking):**

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [
        { "text": "I need to find coffee shops in the area first.", "thought": true, "thoughtSignature": "opaque-sig-abc123..." },
        { "functionCall": { "name": "search_places", "args": { "query": "specialty coffee shops", "latitude": 59.3293, "longitude": 18.0686, "count": 5 } } }
      ]
    }
  }],
  "usageMetadata": { "promptTokenCount": 312, "candidatesTokenCount": 45, "totalTokenCount": 357 }
}
```

**Turn 2 — App sends tool result, Gemini requests another tool:**

The app executes `search_places` (a separate Gemini API call returning JSON), then appends the result as a `functionResponse`:

```json
{
  "contents": [
    { "role": "user", "parts": [{ "text": "system prompt + Coffee tour in Stockholm" }] },
    { "role": "model", "parts": [
      { "text": "I need to find coffee shops...", "thought": true, "thoughtSignature": "opaque-sig-abc123..." },
      { "functionCall": { "name": "search_places", "args": { "query": "specialty coffee shops", "latitude": 59.3293, "longitude": 18.0686, "count": 5 } } }
    ]},
    { "role": "function", "parts": [{ "functionResponse": { "name": "search_places", "response": { "result": "[{\"name\":\"Drop Coffee\",\"latitude\":59.3173,...}]" } } }] }
  ],
  "tools": [{ "functionDeclarations": ["..."] }]
}
```

Gemini responds with `functionCall("calculate_route", ...)`.

**Turn 3 — Final text response (agent is done):**

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [
        { "text": "Here's your Stockholm coffee tour! Starting from Drop Coffee in Södermalm, you'll walk through the city's best specialty roasters. The route covers 2.4 km (~29 min walk) with 3 carefully chosen stops..." }
      ]
    }
  }],
  "usageMetadata": { "promptTokenCount": 1240, "candidatesTokenCount": 187, "totalTokenCount": 1427 }
}
```

When Gemini returns text without any `functionCall`, the agent loop exits. The thinking parts from earlier turns **must** be preserved verbatim (including `thoughtSignature`) when replaying conversation history, or the API returns HTTP 400.

### Search Places Tool — Example Response

This is the standalone Gemini API call made inside the `search_places` tool execution (Pattern 3 — structured JSON):

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{
        "text": "[{\"name\":\"Drop Coffee\",\"latitude\":59.3173,\"longitude\":18.0546,\"description\":\"Award-winning specialty roaster with minimalist Nordic vibes.\",\"category\":\"Coffee Shop\"},{\"name\":\"Johan & Nyström\",\"latitude\":59.3210,\"longitude\":18.0710,\"description\":\"Popular chain known for single-origin beans and cozy atmosphere.\",\"category\":\"Coffee Shop\"},{\"name\":\"Café Pascal\",\"latitude\":59.3390,\"longitude\":18.0580,\"description\":\"Trendy café serving expertly crafted espresso drinks.\",\"category\":\"Coffee Shop\"}]"
      }]
    }
  }],
  "usageMetadata": { "promptTokenCount": 124, "candidatesTokenCount": 156, "totalTokenCount": 280 }
}
```

The client parses this JSON array into `TripStop` objects and collects them for route calculation.

---

## 7. AI Suggested Places

**Module:** `data/explore` | **Feature tag:** `"explore"`

On the map exploration screen, the user can tap an AI button to get 10 interesting places suggested near their current location.

### How it works

- **Repository:** `ExploreSuggestionsRepositoryImpl.getSuggestedPlaces()`
- **Request type:** Text-only (structured prompt expecting JSON response)
- **Prompt** (defined in `AiPrompts.suggestedPlacesPrompt()`):

  > You are a helpful AI assistant. Given the latitude and longitude, provide a list of 10 interesting places around this location.
  > For each place, include its name, latitude, longitude, a short description (max 2 sentences), and a category (e.g., "Park", "Museum", "Restaurant"). Return the response strictly as a JSON object with a single "places" array.

- **Response processing:** JSON extracted from potential code fences via regex, parsed via Gson into `SuggestedPlace` domain models (name, lat, lng, description, category).
- **Display:** Suggested places appear as star-icon markers on the Google Map, tappable for info cards.
- **Trigger:** User taps the AI suggestion button in ExploreScreen. Requires both Gemini API key and user location.

### Example Response

```json
{
  "candidates": [{
    "content": {
      "role": "model",
      "parts": [{
        "text": "```json\n{\n  \"places\": [\n    {\n      \"name\": \"Fotografiska\",\n      \"latitude\": 59.3180,\n      \"longitude\": 18.0850,\n      \"description\": \"World-renowned photography museum housed in a stunning Art Nouveau building on the waterfront. Features rotating exhibitions from international artists.\",\n      \"category\": \"Museum\"\n    },\n    {\n      \"name\": \"Monteliusvägen\",\n      \"latitude\": 59.3207,\n      \"longitude\": 18.0625,\n      \"description\": \"A scenic walking path along the cliffs of Södermalm offering panoramic views of Lake Mälaren, City Hall, and the old town.\",\n      \"category\": \"Scenic Viewpoint\"\n    },\n    {\n      \"name\": \"Rosendals Trädgård\",\n      \"latitude\": 59.3270,\n      \"longitude\": 18.1150,\n      \"description\": \"A biodynamic garden and café on Djurgården. Pick your own flowers and enjoy organic pastries in the greenhouse.\",\n      \"category\": \"Garden & Café\"\n    }\n  ]\n}\n```"
      }]
    }
  }],
  "usageMetadata": {
    "promptTokenCount": 98,
    "candidatesTokenCount": 215,
    "totalTokenCount": 313
  }
}
```

Note: The response is wrapped in markdown code fences (` ```json ... ``` `). The client uses regex to extract the JSON content, then parses it via Gson into `SuggestedPlace` domain models. The full response includes 10 places; 3 are shown here for brevity.

---

## Summary

| #  | Feature                | Module         | Request Type                  | Persona / Style                   |  Calls Gemini   |
|----|------------------------|----------------|-------------------------------|-----------------------------------|:---------------:|
| 1  | Chat (text)            | `data/chat`    | Text                          | AI Overlord (42 words max)        |        1        |
| 2  | Image analysis         | `data/chat`    | Multimodal (text + image)     | AI Overlord + analysis type       |        1        |
| 3  | Document analysis      | `data/chat`    | Text (with doc context)       | AI Overlord                       |        1        |
| 4  | Conversation starters  | `data/chat`    | Text                          | Standalone prompt                 |        1        |
| 5  | Dream interpretation   | `data/dream`   | Text (JSON response)          | Dream interpreter & visual artist |        1        |
| 5b | Dream image generation | `data/dream`   | Image (responseModalities)    | Dream visual artist               |   1–3 (retry)   |
| 6  | Trip planner (agent)   | `data/plan`    | Function calling (multi-turn) | Trip planner agent                |   1 per turn    |
| 7  | Place search (tool)    | `data/plan`    | Text (JSON response)          | Standalone prompt                 | 1 per tool call |
| 8  | Suggested places       | `data/explore` | Text (JSON response)          | Helpful AI assistant              |        1        |

**Total distinct AI call sites:** 7 (across 4 repository classes) — 6 through `GeminiApiService.generateContent()`, 1 through `GeminiApiService.generateImageContent()`.

**All AI prompts and persona text** are centralized in `core/network/.../prompts/AiPrompts.kt`, making them easy to find and modify in one place.

**Token usage** is tracked per-call in Room and aggregated weekly for the usage chart in Settings.
