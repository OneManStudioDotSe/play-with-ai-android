package se.onemanstudio.playaroundwithai.feature.plan

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.plan.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.plan.states.TripStopUi

private const val TOTAL_DISTANCE_KM = 3.8
private const val TOTAL_WALKING_MINUTES = 46

fun samplePlanSteps(): PersistentList<PlanStepUi> = persistentListOf(
    PlanStepUi(icon = StepIcon.THINKING, label = "Understanding your request..."),
    PlanStepUi(
        icon = StepIcon.TOOL_CALL,
        label = "Searching for \"historical landmarks in Stockholm\"",
        toolName = "search_places",
    ),
    PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 5 places", toolName = "search_places", detail = "Found 5 places"),
    PlanStepUi(
        icon = StepIcon.TOOL_CALL,
        label = "Searching for \"scenic waterfront spots in Stockholm\"",
        toolName = "search_places",
    ),
    PlanStepUi(icon = StepIcon.TOOL_RESULT, label = "Found 3 places", toolName = "search_places", detail = "Found 3 places"),
    PlanStepUi(icon = StepIcon.THINKING, label = "Selecting the best stops for your itinerary..."),
    PlanStepUi(icon = StepIcon.TOOL_CALL, label = "Calculating optimal walking route", toolName = "calculate_route"),
    PlanStepUi(
        icon = StepIcon.TOOL_RESULT,
        label = "Route: 3.8 km, ~46 min walk",
        toolName = "calculate_route",
        detail = "Route: 3.8 km, ~46 min walk",
    ),
)

@Suppress("LongMethod")
fun sampleTripPlan(): TripPlanUi = TripPlanUi(
    summary = "A walking tour through Stockholm's historic heart, starting at the Royal Palace in Gamla Stan " +
        "and winding past medieval churches and cobblestone squares before crossing to Kungsholmen " +
        "for a grand finale at City Hall — home of the Nobel Prize banquet.",
    stops = persistentListOf(
        TripStopUi(
            name = "Royal Palace (Kungliga Slottet)",
            latitude = 59.3268,
            longitude = 18.0717,
            description = "One of Europe's largest royal palaces, with over 600 rooms and five museums. " +
                "Don't miss the daily changing of the guard ceremony.",
            category = "Historical Landmark",
            orderIndex = 0,
        ),
        TripStopUi(
            name = "Storkyrkan (Stockholm Cathedral)",
            latitude = 59.3258,
            longitude = 18.0708,
            description = "Stockholm's oldest church, dating back to the 13th century. " +
                "Home to the famous sculpture of Saint George and the Dragon.",
            category = "Church",
            orderIndex = 1,
        ),
        TripStopUi(
            name = "Stortorget",
            latitude = 59.3252,
            longitude = 18.0703,
            description = "The oldest square in Stockholm, surrounded by colorful merchant houses. " +
                "Site of the 1520 Stockholm Bloodbath and today a charming gathering spot.",
            category = "Historic Square",
            orderIndex = 2,
        ),
        TripStopUi(
            name = "Riddarholmen Church",
            latitude = 59.3238,
            longitude = 18.0642,
            description = "A medieval abbey church and the burial place of Swedish monarchs. " +
                "Its cast-iron spire is one of Stockholm's most recognizable landmarks.",
            category = "Church",
            orderIndex = 3,
        ),
        TripStopUi(
            name = "Stockholm City Hall (Stadshuset)",
            latitude = 59.3275,
            longitude = 18.0545,
            description = "Iconic red-brick building on the waterfront, famous for hosting the Nobel Prize banquet " +
                "in its stunning Blue Hall. Climb the tower for panoramic city views.",
            category = "Landmark",
            orderIndex = 4,
        ),
    ),
    totalDistanceKm = TOTAL_DISTANCE_KM,
    totalWalkingMinutes = TOTAL_WALKING_MINUTES,
)
