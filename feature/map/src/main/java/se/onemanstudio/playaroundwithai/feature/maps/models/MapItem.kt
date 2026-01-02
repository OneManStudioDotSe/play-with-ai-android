package se.onemanstudio.playaroundwithai.feature.maps.models

import com.google.android.gms.maps.model.LatLng
import se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto.VehicleType
import se.onemanstudio.playaroundwithai.core.data.model.MapItem as DomainMapItem

data class MapItem(
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

fun MapItemDto.toUiModel() = MapItem(
    id = id,
    lat = lat,
    lng = lng,
    name = name,
    type = type,
    batteryLevel = batteryLevel,
    vehicleCode = vehicleCode,
    nickname = nickname,
)

fun DomainMapItem.toUiModel() = MapItem(
    id = id,
    lat = lat,
    lng = lng,
    name = name,
    type = type,
    batteryLevel = batteryLevel,
    vehicleCode = vehicleCode,
    nickname = nickname,
)
