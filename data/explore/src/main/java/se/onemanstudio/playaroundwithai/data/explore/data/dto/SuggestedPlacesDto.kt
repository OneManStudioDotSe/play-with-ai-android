package se.onemanstudio.playaroundwithai.data.explore.data.dto

import com.google.gson.annotations.SerializedName
import se.onemanstudio.playaroundwithai.data.explore.domain.model.SuggestedPlace

data class SuggestedPlacesResponseDto(
    @SerializedName("places") val places: List<SuggestedPlaceDto>
)

data class SuggestedPlaceDto(
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val lat: Double,
    @SerializedName("longitude") val lng: Double,
    @SerializedName("description") val description: String,
    @SerializedName("category") val category: String
)

fun SuggestedPlaceDto.toSuggestedPlaceDomain(): SuggestedPlace {
    return SuggestedPlace(
        name = name,
        lat = lat,
        lng = lng,
        description = description,
        category = category
    )
}
