package se.onemanstudio.playaroundwithai.core.data.feature.map.dto

import se.onemanstudio.playaroundwithai.core.data.model.MapItem

enum class VehicleType {
    SCOOTER, BICYCLE
}

data class MapItemDto(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val type: VehicleType,
    val batteryLevel: Int,
    val vehicleCode: String,
    val nickname: String
)

fun MapItemDto.toDomain(): MapItem {
    return MapItem(
        id = this.id,
        lat = this.lat,
        lng = this.lng,
        name = this.name,
        type = this.type,
        batteryLevel = 100,
        vehicleCode = "0000",
        nickname = this.nickname
    )
}
