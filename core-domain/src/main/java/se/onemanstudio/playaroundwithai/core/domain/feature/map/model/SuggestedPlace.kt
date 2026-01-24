package se.onemanstudio.playaroundwithai.core.domain.feature.map.model

data class SuggestedPlace(
    val name: String,
    val lat: Double,
    val lng: Double,
    val description: String,
    val category: String
)
