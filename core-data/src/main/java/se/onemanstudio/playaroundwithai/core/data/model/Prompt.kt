package se.onemanstudio.playaroundwithai.core.data.model

/**
 * Represents a prompt in the domain layer, clean of any framework-specific annotations.
 */
data class Prompt(
    val id: Int,
    val text: String,
    val timestamp: Long,
)
