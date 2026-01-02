package se.onemanstudio.playaroundwithai.core.data.model.mapper

import se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.model.MapItem

fun MapItemDto.toDomain(): MapItem {
    return MapItem(
        id = this.id,
        lat = this.lat,
        lng = this.lng,
        name = this.name,
        type = this.type,
        batteryLevel = this.batteryLevel,
        vehicleCode = this.vehicleCode,
        nickname = this.nickname
    )
}
