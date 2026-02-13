package se.onemanstudio.playaroundwithai.core.data.feature.map.mapper

import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType

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
