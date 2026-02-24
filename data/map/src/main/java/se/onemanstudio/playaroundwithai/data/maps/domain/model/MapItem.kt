package se.onemanstudio.playaroundwithai.data.maps.domain.model

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
