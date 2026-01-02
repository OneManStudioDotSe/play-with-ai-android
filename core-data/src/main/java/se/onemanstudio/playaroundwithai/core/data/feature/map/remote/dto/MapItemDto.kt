package se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto

enum class VehicleType {
    SCOOTER, BICYCLE
}

data class MapItemDto(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val type: VehicleType,
    val batteryLevel: Int = 100,
    val vehicleCode: String = "0000",
    val nickname: String = "Unnamed"
)
