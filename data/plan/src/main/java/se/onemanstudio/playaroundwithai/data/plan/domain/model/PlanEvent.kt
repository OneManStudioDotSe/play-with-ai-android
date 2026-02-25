package se.onemanstudio.playaroundwithai.data.plan.domain.model

sealed interface PlanEvent {
    data class Thinking(val message: String) : PlanEvent
    data class ToolCalling(val toolName: String, val summary: String) : PlanEvent
    data class ToolResult(val toolName: String, val summary: String) : PlanEvent
    data class Complete(val plan: TripPlan) : PlanEvent
    data class Error(val message: String) : PlanEvent
}
