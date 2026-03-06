package se.onemanstudio.playaroundwithai.data.explore.prompts

internal object ExplorePrompts {

    private const val SUGGESTED_PLACES_COUNT = 5

    fun suggestedPlacesPrompt(latitude: Double, longitude: Double): String = """
You are a helpful AI assistant. Given the latitude and longitude,
provide a list of $SUGGESTED_PLACES_COUNT interesting places within a 5 to 10 km radius of this location.
Spread them out geographically — do not cluster them in one area.
For each place, include its name, latitude, longitude,
a short description (max 2 sentences),
and a category (e.g., "Park", "Museum", "Restaurant").
Return the response strictly as a JSON object with a single "places" array,
where each element is a place object.
Latitude: $latitude, Longitude: $longitude
    """.trimIndent()
}
