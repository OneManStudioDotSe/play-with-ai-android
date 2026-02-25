package se.onemanstudio.playaroundwithai.data.agents.domain.model

sealed interface AgentEvent {
    data class Thinking(val message: String) : AgentEvent
    data class ToolCalling(val toolName: String, val summary: String) : AgentEvent
    data class ToolResult(val toolName: String, val summary: String) : AgentEvent
    data class Complete(val plan: TripPlan) : AgentEvent
    data class Error(val message: String) : AgentEvent
}
