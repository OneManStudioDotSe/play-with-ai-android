package se.onemanstudio.playaroundwithai.core.data.feature.chat.remote.dto

import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace

data class SuggestedPlaceDto(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val category: String
)

data class SuggestedPlacesResponseDto(
    val places: List<SuggestedPlaceDto>
)

fun SuggestedPlaceDto.toDomain(): SuggestedPlace {
    return SuggestedPlace(
        name = this.name,
        lat = this.latitude,
        lng = this.longitude,
        description = this.description,
        category = this.category
    )
}
