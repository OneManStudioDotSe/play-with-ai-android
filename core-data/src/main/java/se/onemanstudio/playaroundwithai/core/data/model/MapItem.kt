package se.onemanstudio.playaroundwithai.core.data.model

import se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto.VehicleType

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
