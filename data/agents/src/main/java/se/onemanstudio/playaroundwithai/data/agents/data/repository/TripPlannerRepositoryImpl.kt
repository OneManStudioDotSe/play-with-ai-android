@file:Suppress("TooManyFunctions")

package se.onemanstudio.playaroundwithai.data.agents.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.tracking.TokenUsageTracker
import se.onemanstudio.playaroundwithai.core.network.dto.FunctionCallDto
import se.onemanstudio.playaroundwithai.core.network.dto.FunctionResponseDto
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.data.agents.data.tools.RouteCalculator
import se.onemanstudio.playaroundwithai.data.agents.data.tools.buildToolDeclarations
import se.onemanstudio.playaroundwithai.data.agents.domain.model.AgentEvent
import se.onemanstudio.playaroundwithai.data.agents.domain.model.TripPlan
import se.onemanstudio.playaroundwithai.data.agents.domain.model.TripStop
import se.onemanstudio.playaroundwithai.data.agents.domain.repository.TripPlannerRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_ITERATIONS = 10
private const val DEFAULT_COUNT = 5
private const val ERROR_BODY_PREVIEW_LENGTH = 200
private const val SEARCH_PLACES_PROMPT_PREFIX = "Return a JSON array of"
private const val LOG_TAG = "TripPlanner"

@Singleton
class TripPlannerRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    private val tokenUsageTracker: TokenUsageTracker,
) : TripPlannerRepository {

    @Suppress("TooGenericExceptionCaught")
    override fun planTrip(goal: String, latitude: Double, longitude: Double): Flow<AgentEvent> = flow {
        try {
            val tools = listOf(buildToolDeclarations())
            val history = mutableListOf<Content>()
            val collectedStops = mutableListOf<TripStop>()
            var routeResult: se.onemanstudio.playaroundwithai.data.agents.data.tools.RouteResult? = null

            val systemPrompt = buildSystemPrompt(latitude, longitude)
            history.add(Content(role = "user", parts = listOf(Part(text = "$systemPrompt\n\nUser request: $goal"))))

            emit(AgentEvent.Thinking("Understanding your request..."))

            var iterations = 0
            while (iterations < MAX_ITERATIONS) {
                iterations++
                Timber.d("$LOG_TAG - Iteration $iterations")

                val request = GeminiRequest(contents = history, tools = tools)
                val response = apiService.generateContent(request)
                tokenUsageTracker.record("agents", response.usageMetadata)
                val modelContent = response.candidates.firstOrNull()?.content ?: break

                history.add(modelContent)

                val functionCall = modelContent.parts.firstOrNull { it.functionCall != null }?.functionCall
                if (functionCall != null) {
                    emit(AgentEvent.ToolCalling(functionCall.name, summarizeArgs(functionCall)))

                    val result = dispatchTool(functionCall.name, functionCall.args, latitude, longitude, collectedStops)
                    if (functionCall.name == "calculate_route") {
                        routeResult = extractRouteResult(result)
                    }

                    history.add(
                        Content(
                            role = "function",
                            parts = listOf(
                                Part(
                                    functionResponse = FunctionResponseDto(
                                        name = functionCall.name,
                                        response = result,
                                    )
                                )
                            ),
                        )
                    )

                    emit(AgentEvent.ToolResult(functionCall.name, summarizeResult(functionCall.name, result)))
                    emit(AgentEvent.Thinking("Analyzing results..."))
                } else {
                    val text = modelContent.parts.firstOrNull { it.text != null }?.text.orEmpty()
                    val plan = buildTripPlan(text, collectedStops, routeResult)
                    emit(AgentEvent.Complete(plan))
                    return@flow
                }
            }

            emit(AgentEvent.Error("Agent reached maximum iterations without completing"))
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Timber.e(e, "$LOG_TAG - HTTP ${e.code()} error. Body: $errorBody")
            emit(AgentEvent.Error("API error (${e.code()}): ${errorBody?.take(ERROR_BODY_PREVIEW_LENGTH) ?: e.message()}"))
        } catch (e: Exception) {
            Timber.e(e, "$LOG_TAG - Agent error")
            emit(AgentEvent.Error(e.message ?: "An unexpected error occurred"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun dispatchTool(
        name: String,
        args: Map<String, Any>,
        latitude: Double,
        longitude: Double,
        collectedStops: MutableList<TripStop>,
    ): Map<String, Any> {
        return when (name) {
            "search_places" -> handleSearchPlaces(args, latitude, longitude, collectedStops)
            "calculate_route" -> handleCalculateRoute(args, collectedStops)
            else -> mapOf("error" to "Unknown tool: $name")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun handleSearchPlaces(
        args: Map<String, Any>,
        defaultLat: Double,
        defaultLng: Double,
        collectedStops: MutableList<TripStop>,
    ): Map<String, Any> {
        val query = args["query"]?.toString() ?: "interesting places"
        val lat = (args["latitude"] as? Number)?.toDouble() ?: defaultLat
        val lng = (args["longitude"] as? Number)?.toDouble() ?: defaultLng
        val count = (args["count"] as? Number)?.toInt() ?: DEFAULT_COUNT

        Timber.d("$LOG_TAG - search_places: query='$query' lat=$lat lng=$lng count=$count")

        val prompt = buildSearchPrompt(query, lat, lng, count)
        val request = GeminiRequest(contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))))
        val response = apiService.generateContent(request)
        tokenUsageTracker.record("agents", response.usageMetadata)
        val text = response.extractText().orEmpty()

        val places = parsePlacesFromResponse(text)

        places.forEachIndexed { index, place ->
            val name = place["name"]?.toString() ?: "Place ${collectedStops.size + 1}"
            val placeLat = (place["latitude"] as? Number)?.toDouble() ?: lat
            val placeLng = (place["longitude"] as? Number)?.toDouble() ?: lng
            val description = place["description"]?.toString().orEmpty()
            val category = place["category"]?.toString().orEmpty()

            collectedStops.add(
                TripStop(
                    name = name,
                    latitude = placeLat,
                    longitude = placeLng,
                    description = description,
                    category = category,
                    orderIndex = collectedStops.size + index,
                )
            )
        }

        Timber.d("$LOG_TAG - Found ${places.size} places, total stops now: ${collectedStops.size}")
        return mapOf("places" to places, "count" to places.size)
    }

    @Suppress("UNCHECKED_CAST")
    private fun handleCalculateRoute(
        args: Map<String, Any>,
        collectedStops: MutableList<TripStop>,
    ): Map<String, Any> {
        val places = (args["places"] as? List<Map<String, Any>>).orEmpty()

        val coordinates = if (places.isNotEmpty()) {
            places.mapNotNull { place ->
                val lat = (place["latitude"] as? Number)?.toDouble()
                val lng = (place["longitude"] as? Number)?.toDouble()
                if (lat != null && lng != null) lat to lng else null
            }
        } else {
            collectedStops.map { it.latitude to it.longitude }
        }

        if (coordinates.isEmpty()) {
            return mapOf("error" to "No places to calculate route for")
        }

        Timber.d("$LOG_TAG - calculate_route for ${coordinates.size} places")
        val result = RouteCalculator.findOptimalRoute(coordinates)

        val reorderedStops = result.orderedIndices.mapIndexed { newIndex, originalIndex ->
            if (originalIndex < collectedStops.size) {
                collectedStops[originalIndex].copy(orderIndex = newIndex)
            } else {
                null
            }
        }.filterNotNull()

        collectedStops.clear()
        collectedStops.addAll(reorderedStops)

        return mapOf(
            "ordered_places" to result.orderedIndices.mapNotNull { idx ->
                if (idx < reorderedStops.size) {
                    mapOf(
                        "name" to reorderedStops[idx].name,
                        "latitude" to reorderedStops[idx].latitude,
                        "longitude" to reorderedStops[idx].longitude,
                    )
                } else {
                    null
                }
            },
            "total_distance_km" to result.totalDistanceKm,
            "total_walking_minutes" to result.totalWalkingMinutes,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parsePlacesFromResponse(text: String): List<Map<String, Any>> {
        val cleaned = text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return try {
            val list = gson.fromJson(cleaned, List::class.java) as? List<Map<String, Any>>
            list.orEmpty()
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.w(e, "$LOG_TAG - Failed to parse places JSON, attempting fallback")
            emptyList()
        }
    }

    private fun extractRouteResult(result: Map<String, Any>): se.onemanstudio.playaroundwithai.data.agents.data.tools.RouteResult? {
        val distance = (result["total_distance_km"] as? Number)?.toDouble()
        val minutes = (result["total_walking_minutes"] as? Number)?.toInt()
        if (distance == null || minutes == null) return null
        return se.onemanstudio.playaroundwithai.data.agents.data.tools.RouteResult(
            orderedIndices = emptyList(),
            totalDistanceKm = distance,
            totalWalkingMinutes = minutes,
        )
    }

    private fun buildTripPlan(
        summary: String,
        stops: List<TripStop>,
        routeResult: se.onemanstudio.playaroundwithai.data.agents.data.tools.RouteResult?,
    ): TripPlan {
        val orderedStops = stops.sortedBy { it.orderIndex }.mapIndexed { index, stop ->
            stop.copy(orderIndex = index)
        }
        return TripPlan(
            summary = summary,
            stops = orderedStops,
            totalDistanceKm = routeResult?.totalDistanceKm ?: calculateFallbackDistance(orderedStops),
            totalWalkingMinutes = routeResult?.totalWalkingMinutes ?: calculateFallbackMinutes(orderedStops),
        )
    }

    private fun calculateFallbackDistance(stops: List<TripStop>): Double {
        if (stops.size < 2) return 0.0
        val coords = stops.map { it.latitude to it.longitude }
        return RouteCalculator.pathDistanceKm(coords)
    }

    @Suppress("MagicNumber")
    private fun calculateFallbackMinutes(stops: List<TripStop>): Int {
        val distance = calculateFallbackDistance(stops)
        return (distance / 5.0 * 60).toInt()
    }

    private fun summarizeArgs(functionCall: FunctionCallDto): String {
        return when (functionCall.name) {
            "search_places" -> "Searching for \"${functionCall.args["query"]}\""
            "calculate_route" -> "Calculating optimal walking route"
            else -> functionCall.name
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun summarizeResult(name: String, result: Map<String, Any>): String {
        return when (name) {
            "search_places" -> {
                val count = result["count"] as? Number
                "Found ${count?.toInt() ?: 0} places"
            }
            "calculate_route" -> {
                val distance = result["total_distance_km"] as? Number
                val minutes = result["total_walking_minutes"] as? Number
                "Route: %.1f km, ~%d min walk".format(distance?.toDouble() ?: 0.0, minutes?.toInt() ?: 0)
            }
            else -> "Completed"
        }
    }

    companion object {
        @Suppress("MaxLineLength")
        private fun buildSystemPrompt(latitude: Double, longitude: Double): String = """
You are an AI trip planner agent. Given a user's request, plan a walking trip itinerary.

You have access to these tools:
- search_places: Find places matching a query near a location
- calculate_route: Calculate the optimal walking route between places

Strategy:
1. Break down the user's request into 1-3 search queries (e.g., "specialty coffee shops", "scenic viewpoints")
2. Use search_places for each query to find candidate locations
3. Select the best 4-6 stops that create a coherent itinerary
4. Use calculate_route to find the optimal walking order
5. Provide a final summary describing the trip with personality and helpful tips

Keep the itinerary to 4-6 stops for a half-day trip. The user is near latitude $latitude, longitude $longitude.

When you have finished planning, respond with a text summary of the itinerary. The summary should describe each stop in order, mention what makes each place special, and include practical tips.
Do NOT call more than 5 tools total.
        """.trimIndent()

        @Suppress("MaxLineLength")
        private fun buildSearchPrompt(query: String, lat: Double, lng: Double, count: Int): String = """
$SEARCH_PLACES_PROMPT_PREFIX $count real places matching "$query" near latitude $lat, longitude $lng.

Each place must be a real, existing establishment or location. Return ONLY a JSON array with this format:
[
  {
    "name": "Place Name",
    "latitude": 59.3293,
    "longitude": 18.0686,
    "description": "A brief 1-2 sentence description of what makes this place special",
    "category": "cafe"
  }
]

Use realistic coordinates near the specified location. Return ONLY valid JSON, no markdown, no backticks, no extra text.
        """.trimIndent()
    }
}
