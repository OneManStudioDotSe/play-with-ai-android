package se.onemanstudio.playaroundwithai.states

// Shared UI State
data class ScreenState(
    val isLoading: Boolean = false,
    val result: String? = null
)