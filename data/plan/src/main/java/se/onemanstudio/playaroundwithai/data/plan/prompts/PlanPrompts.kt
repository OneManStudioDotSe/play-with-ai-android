package se.onemanstudio.playaroundwithai.data.plan.prompts

internal object PlanPrompts {

    private const val TRIP_PLANNER_SYSTEM_PROMPT = """
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

Keep the itinerary to 4-6 stops for a half-day trip. The user is near latitude %s, longitude %s.

When you have finished planning, respond with a text summary of the itinerary. The summary should describe each stop in order, mention what makes each place special, and include practical tips.
Do NOT call more than 5 tools total.
    """

    private const val SEARCH_PLACES_PROMPT = """
Return a JSON array of %d real places matching "%s" near latitude %s, longitude %s.

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
    """

    fun tripPlannerSystemPrompt(latitude: Double, longitude: Double): String =
        TRIP_PLANNER_SYSTEM_PROMPT.trimIndent().format(latitude.toString(), longitude.toString())

    fun searchPlacesPrompt(query: String, latitude: Double, longitude: Double, count: Int): String =
        SEARCH_PLACES_PROMPT.trimIndent().format(count, query, latitude.toString(), longitude.toString())
}
