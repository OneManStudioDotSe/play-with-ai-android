package se.onemanstudio.playaroundwithai.data.explore.data.mapper

import se.onemanstudio.playaroundwithai.data.explore.data.dto.ExploreItemDto
import se.onemanstudio.playaroundwithai.data.explore.data.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType

fun ExploreItemDto.toDomain(): ExploreItem {
    return ExploreItem(
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
