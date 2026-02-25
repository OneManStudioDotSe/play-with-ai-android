package se.onemanstudio.playaroundwithai.data.explore.domain.model

data class SuggestedPlace(
    val name: String,
    val lat: Double,
    val lng: Double,
    val description: String,
    val category: String
)
