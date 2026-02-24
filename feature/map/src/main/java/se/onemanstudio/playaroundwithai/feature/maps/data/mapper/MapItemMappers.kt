package se.onemanstudio.playaroundwithai.feature.maps.data.mapper

import se.onemanstudio.playaroundwithai.feature.maps.data.dto.MapItemDto
import se.onemanstudio.playaroundwithai.feature.maps.data.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.feature.maps.domain.model.MapItem
import se.onemanstudio.playaroundwithai.feature.maps.domain.model.VehicleType

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
        batteryLevel = this.batteryLevel,
        vehicleCode = this.vehicleCode,
        nickname = this.nickname
    )
}
