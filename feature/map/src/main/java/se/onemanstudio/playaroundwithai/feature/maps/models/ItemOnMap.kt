package se.onemanstudio.playaroundwithai.feature.maps.models

import com.google.android.gms.maps.model.LatLng

enum class VehicleType {
    SCOOTER, BICYCLE
}

data class ItemOnMap(
    val id: String,
    val lat: Double,
    val lng: Double,
    val name: String,
    val type: VehicleType,
    val isSelected: Boolean = false,
    val batteryLevel: Int = 100,
    val vehicleCode: String = "0000",
    val nickname: String = "Unnamed"
) {
    val position: LatLng
        get() = LatLng(lat, lng)
}
