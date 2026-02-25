package se.onemanstudio.playaroundwithai.data.explore.domain.model

data class ExploreItem(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val type: VehicleType,
    val batteryLevel: Int,
    val vehicleCode: String,
    val nickname: String
)
