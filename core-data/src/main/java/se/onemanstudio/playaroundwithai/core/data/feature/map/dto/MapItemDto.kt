package se.onemanstudio.playaroundwithai.core.data.feature.map.dto

import se.onemanstudio.playaroundwithai.core.domain.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.model.VehicleType

enum class VehicleTypeDto {
    SCOOTER, BICYCLE
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

fun MapItemDto.toDomain(): MapItem {
    return MapItem(
        id = this.id,
        lat = this.lat,
        lng = this.lng,
        name = this.name,
        type = when (this.type) {
            VehicleTypeDto.SCOOTER -> VehicleType.SCOOTER
            VehicleTypeDto.BICYCLE -> VehicleType.BICYCLE
        },
        batteryLevel = 100,
        vehicleCode = "0000",
        nickname = this.nickname
    )
}
