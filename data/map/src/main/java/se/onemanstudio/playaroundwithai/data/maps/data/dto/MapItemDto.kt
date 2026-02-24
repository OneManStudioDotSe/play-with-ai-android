package se.onemanstudio.playaroundwithai.data.maps.data.dto

enum class VehicleTypeDto {
    Scooter, Bicycle
}

data class MapItemDto(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val type: VehicleTypeDto,
    val batteryLevel: Int,
    val vehicleCode: String,
    val nickname: String
)
