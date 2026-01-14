package se.onemanstudio.playaroundwithai.core.domain.feature.map.model

enum class VehicleType {
    SCOOTER, BICYCLE
}

data class MapItem(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val type: VehicleType,
    val batteryLevel: Int,
    val vehicleCode: String,
    val nickname: String
)
