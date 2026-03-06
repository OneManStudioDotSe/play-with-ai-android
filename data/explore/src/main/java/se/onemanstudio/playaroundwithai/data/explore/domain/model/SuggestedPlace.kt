package se.onemanstudio.playaroundwithai.data.explore.domain.model

data class SuggestedPlace(
    val name: String,
    val lat: Double,
    val lng: Double,
    val description: String,
    val category: String
)

fun SuggestedPlace.toExploreItem(): ExploreItem = ExploreItem(
    id = "suggested_${name}_${lat}_${lng}",
    lat = lat,
    lng = lng,
    name = name,
    type = VehicleType.Scooter,
    batteryLevel = 0,
    vehicleCode = "",
    nickname = name,
)
