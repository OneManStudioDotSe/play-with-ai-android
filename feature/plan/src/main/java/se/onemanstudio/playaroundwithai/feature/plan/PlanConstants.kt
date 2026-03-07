package se.onemanstudio.playaroundwithai.feature.plan

import androidx.compose.ui.unit.dp

internal object PlanConstants {
    const val DEFAULT_LAT = 59.3293
    const val DEFAULT_LNG = 18.0686
    const val MAP_HEIGHT_MIN = 180
    const val MAP_HEIGHT_MAX = 280
    const val MAP_ZOOM = 13f
    const val POLYLINE_WIDTH = 5f
    const val PULSE_ALPHA_MIN = 0.3f
    const val PULSE_ALPHA_MAX = 1.0f
    const val PULSE_DURATION_MS = 800
    const val BOUNCE_DURATION_MS = 400
    const val BOUNCE_STAGGER_MS = 100
    const val BOUNCE_DOT_COUNT = 3
    val BOUNCE_AMPLITUDE = 4.dp

    // Agent pulse animation
    const val AGENT_PULSE_SIZE = 64
    const val AGENT_PULSE_DOT_COUNT = 3
    const val AGENT_PULSE_DOT_RADIUS = 12f
    const val AGENT_PULSE_LINE_WIDTH = 4f
    const val AGENT_PULSE_DURATION_MS = 600
    const val AGENT_PULSE_STAGGER_MS = 200

    // Step stagger animation
    const val STEP_STAGGER_DELAY_MS = 80

    // Step color border
    val STEP_BORDER_WIDTH = 4.dp

    // Result phased entry animation
    const val RESULT_SUMMARY_DELAY_MS = 200
    const val RESULT_ITINERARY_DELAY_MS = 400
    const val RESULT_STOP_STAGGER_MS = 100
    const val RESULT_METRICS_DELAY_MS = 600
    const val RESULT_BUTTON_DELAY_MS = 800

    // Stop card order badge
    const val ORDER_BADGE_SIZE = 32
    const val ACCENT_COLOR_COUNT = 5
}