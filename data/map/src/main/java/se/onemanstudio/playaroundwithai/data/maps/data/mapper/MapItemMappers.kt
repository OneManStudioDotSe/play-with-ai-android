package se.onemanstudio.playaroundwithai.data.maps.data.mapper

import se.onemanstudio.playaroundwithai.data.maps.data.dto.MapItemDto
import se.onemanstudio.playaroundwithai.data.maps.data.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.data.maps.domain.model.MapItem
import se.onemanstudio.playaroundwithai.data.maps.domain.model.VehicleType

fun MapItemDto.toDomain(): MapItem {
    return MapItem(
        id = this.id,
        lat = this.lat,
        lng = this.lng,
        name = this.name,
        type = when (this.type) {
            VehicleTypeDto.Scooter -> VehicleType.Scooter
            VehicleTypeDto.Bicycle -> VehicleType.Bicycle
        },
        batteryLevel = this.batteryLevel,
        vehicleCode = this.vehicleCode,
        nickname = this.nickname
    )
}
