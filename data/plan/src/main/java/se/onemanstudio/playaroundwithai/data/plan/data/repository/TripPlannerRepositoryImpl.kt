package se.onemanstudio.playaroundwithai.data.plan.data.repository

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import se.onemanstudio.playaroundwithai.core.network.api.GeminiApiService
import se.onemanstudio.playaroundwithai.core.network.utils.JsonExtractor
import se.onemanstudio.playaroundwithai.core.network.dto.Content
import se.onemanstudio.playaroundwithai.core.network.dto.FunctionCallDto
import se.onemanstudio.playaroundwithai.core.tracking.repository.TokenUsageTracker
import se.onemanstudio.playaroundwithai.core.network.dto.FunctionResponseDto
import se.onemanstudio.playaroundwithai.core.network.dto.GeminiRequest
import se.onemanstudio.playaroundwithai.core.network.dto.Part
import se.onemanstudio.playaroundwithai.core.config.settings.AppSettingsHolder
import se.onemanstudio.playaroundwithai.data.plan.data.tools.MINUTES_PER_HOUR
import se.onemanstudio.playaroundwithai.data.plan.data.tools.RouteCalculator
import se.onemanstudio.playaroundwithai.data.plan.data.tools.RouteResult
import se.onemanstudio.playaroundwithai.data.plan.prompts.PlanPrompts
import se.onemanstudio.playaroundwithai.data.plan.data.tools.buildToolDeclarations
import se.onemanstudio.playaroundwithai.data.plan.domain.model.PlanEvent
import se.onemanstudio.playaroundwithai.data.plan.domain.model.TripPlan
import se.onemanstudio.playaroundwithai.data.plan.domain.model.TripStop
import se.onemanstudio.playaroundwithai.data.plan.domain.repository.TripPlannerRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

private const val DEFAULT_COUNT = 5
private const val ERROR_BODY_PREVIEW_LENGTH = 200
private const val LOG_TAG = "TripPlanner"

private const val TRIP_LENGTH_QUICK_MIN = 2
private const val TRIP_LENGTH_QUICK_MAX = 3
private const val TRIP_LENGTH_STANDARD_MAX = 6
private const val TRIP_LENGTH_EXTENDED_MIN = 7
private const val TRIP_LENGTH_EXTENDED_MAX = 10

private const val AGENT_ITERATIONS_QUICK = 5
private const val AGENT_ITERATIONS_THOROUGH = 15
private const val AGENT_TOOL_BUDGET_QUICK = 3
private const val AGENT_TOOL_BUDGET_STANDARD = 5
private const val AGENT_TOOL_BUDGET_THOROUGH = 8

private fun tripLengthMaxStops(minStops: Int): Int = when (minStops) {
    TRIP_LENGTH_QUICK_MIN -> TRIP_LENGTH_QUICK_MAX
    TRIP_LENGTH_EXTENDED_MIN -> TRIP_LENGTH_EXTENDED_MAX
    else -> TRIP_LENGTH_STANDARD_MAX
}

private fun toolBudgetForIterations(maxIterations: Int): Int = when (maxIterations) {
    AGENT_ITERATIONS_QUICK -> AGENT_TOOL_BUDGET_QUICK
    AGENT_ITERATIONS_THOROUGH -> AGENT_TOOL_BUDGET_THOROUGH
    else -> AGENT_TOOL_BUDGET_STANDARD
}

private object ToolNames {
    const val SEARCH_PLACES = "search_places"
    const val CALCULATE_ROUTE = "calculate_route"
}

@Singleton
class TripPlannerRepositoryImpl @Inject constructor(
    private val apiService: GeminiApiService,
    private val gson: Gson,
    private val tokenUsageTracker: TokenUsageTracker,
    private val appSettingsHolder: AppSettingsHolder,
) : TripPlannerRepository {

    @Suppress("LongMethod", "TooGenericExceptionCaught") // agent loop with last-resort catch
    override fun planTrip(goal: String, latitude: Double, longitude: Double): Flow<PlanEvent> = flow {
        try {
            val tools = listOf(buildToolDeclarations())
            val history = mutableListOf<Content>()
            val collectedStops = mutableListOf<TripStop>()
            var routeResult: RouteResult? = null

            val minStops = appSettingsHolder.tripLengthMinStops.value
            val maxStops = tripLengthMaxStops(minStops)
            val maxIterations = appSettingsHolder.agentMaxIterations.value
            val maxTools = toolBudgetForIterations(maxIterations)
            val systemPrompt = PlanPrompts.tripPlannerSystemPrompt(latitude, longitude, minStops, maxStops, maxTools)
            history.add(Content(role = "user", parts = listOf(Part(text = "$systemPrompt\n\nUser request: $goal"))))

            emit(PlanEvent.Thinking("Understanding your request..."))

            var iterations = 0
            while (iterations < maxIterations) {
                currentCoroutineContext().ensureActive()
                iterations++

                val request = GeminiRequest(contents = history, tools = tools)
                val response = apiService.generateContent(request)
                tokenUsageTracker.record("agents", response.usageMetadata)
                val modelContent = response.candidates.firstOrNull()?.content ?: break

                history.add(modelContent)

                val functionCall = modelContent.parts.firstOrNull { it.functionCall != null }?.functionCall
                if (functionCall != null) {
                    emit(PlanEvent.ToolCalling(functionCall.name, summarizeArgs(functionCall)))

                    val result = dispatchTool(functionCall.name, functionCall.args, latitude, longitude, collectedStops)
                    if (functionCall.name == ToolNames.CALCULATE_ROUTE) {
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

                    emit(PlanEvent.ToolResult(functionCall.name, summarizeResult(functionCall.name, result)))
                    emit(PlanEvent.Thinking("Analyzing results..."))
                } else {
                    val text = modelContent.parts.firstOrNull { it.text != null && it.thought != true }?.text.orEmpty()
                    val plan = buildTripPlan(text, collectedStops, routeResult)

                    emit(PlanEvent.Complete(plan))

                    return@flow
                }
            }

            emit(PlanEvent.Error("Agent reached maximum iterations without completing"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            Timber.e(e, "$LOG_TAG - HTTP ${e.code()} error. Body: $errorBody")
            emit(PlanEvent.Error("API error (${e.code()}): ${errorBody?.take(ERROR_BODY_PREVIEW_LENGTH) ?: e.message()}"))
        } catch (e: java.io.IOException) {
            Timber.e(e, "$LOG_TAG - Network error")
            emit(PlanEvent.Error("Network error: ${e.message ?: "Please check your connection"}"))
        } catch (e: Exception) { // last-resort catch after HttpException and IOException
            Timber.e(e, "$LOG_TAG - Agent error")
            emit(PlanEvent.Error("An unexpected error occurred: ${e.message}"))
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
            ToolNames.SEARCH_PLACES -> handleSearchPlaces(args, latitude, longitude, collectedStops)
            ToolNames.CALCULATE_ROUTE -> handleCalculateRoute(collectedStops)
            else -> mapOf("error" to "Unknown tool: $name")
        }
    }

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

        val prompt = PlanPrompts.searchPlacesPrompt(query, lat, lng, count)
        val request = GeminiRequest(contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt)))))
        val response = apiService.generateContent(request)
        tokenUsageTracker.record("agents", response.usageMetadata)
        val text = response.extractText().orEmpty()

        val places = parsePlacesFromResponse(text)

        places.forEach { place ->
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
                    orderIndex = collectedStops.size,
                )
            )
        }

        return mapOf("places" to places, "count" to places.size)
    }

    private fun handleCalculateRoute(collectedStops: MutableList<TripStop>): Map<String, Any> {
        val coordinates = collectedStops.map { it.latitude to it.longitude }

        if (coordinates.isEmpty()) {
            return mapOf("error" to "No places to calculate route for")
        }

        val result = RouteCalculator.findOptimalRoute(coordinates, appSettingsHolder.walkingSpeedKmh.value.toDouble())

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
            "ordered_places" to reorderedStops.map { stop ->
                mapOf(
                    "name" to stop.name,
                    "latitude" to stop.latitude,
                    "longitude" to stop.longitude,
                )
            },
            "total_distance_km" to result.totalDistanceKm,
            "total_walking_minutes" to result.totalWalkingMinutes,
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parsePlacesFromResponse(text: String): List<Map<String, Any>> {
        val cleaned = JsonExtractor.extract(text)

        return try {
            val list = gson.fromJson(cleaned, List::class.java) as? List<Map<String, Any>>
            list.orEmpty()
        } catch (e: com.google.gson.JsonSyntaxException) {
            Timber.w(e, "$LOG_TAG - Failed to parse places JSON, attempting fallback")
            emptyList()
        }
    }

    private fun extractRouteResult(result: Map<String, Any>): RouteResult? {
        val distance = (result["total_distance_km"] as? Number)?.toDouble()
        val minutes = (result["total_walking_minutes"] as? Number)?.toInt()
        if (distance == null || minutes == null) return null
        return RouteResult(
            orderedIndices = emptyList(),
            totalDistanceKm = distance,
            totalWalkingMinutes = minutes,
        )
    }

    private fun buildTripPlan(summary: String, stops: List<TripStop>, routeResult: RouteResult?, ): TripPlan {
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

    private fun calculateFallbackMinutes(stops: List<TripStop>): Int {
        val distance = calculateFallbackDistance(stops)
        return (distance / appSettingsHolder.walkingSpeedKmh.value.toDouble() * MINUTES_PER_HOUR).toInt()
    }

    private fun summarizeArgs(functionCall: FunctionCallDto): String {
        return when (functionCall.name) {
            ToolNames.SEARCH_PLACES -> "Searching for \"${functionCall.args["query"]}\""
            ToolNames.CALCULATE_ROUTE -> "Calculating optimal walking route"
            else -> functionCall.name
        }
    }

    private fun summarizeResult(name: String, result: Map<String, Any>): String {
        return when (name) {
            ToolNames.SEARCH_PLACES -> {
                val count = result["count"] as? Number
                "Found ${count?.toInt() ?: 0} places"
            }
            ToolNames.CALCULATE_ROUTE -> {
                val distance = result["total_distance_km"] as? Number
                val minutes = result["total_walking_minutes"] as? Number
                "Route: %.1f km, ~%d min walk".format(distance?.toDouble() ?: 0.0, minutes?.toInt() ?: 0)
            }
            else -> "Completed"
        }
    }
}
