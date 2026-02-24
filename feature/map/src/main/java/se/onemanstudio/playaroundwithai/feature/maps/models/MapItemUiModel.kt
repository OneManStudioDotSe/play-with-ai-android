package se.onemanstudio.playaroundwithai.feature.maps.models

import com.google.android.gms.maps.model.LatLng
import se.onemanstudio.playaroundwithai.data.maps.domain.model.MapItem as DomainMapItem

/**
 * Represents a map item that is displayed on the UI.
 *
 * @property mapItem The core domain model for the map item.
 * @property isSelected Whether this item is currently selected in the UI.
 */
data class MapItemUiModel(
    val mapItem: DomainMapItem,
    val isSelected: Boolean = false,
) {
    val id: String get() = mapItem.id
    val position: LatLng get() = LatLng(mapItem.lat, mapItem.lng)
    val type get() = mapItem.type
    val name get() = mapItem.name
}

/**
 * Converts a domain [DomainMapItem] to a [MapItemUiModel] for the UI layer.
 */
fun DomainMapItem.toUiModel() = MapItemUiModel(
    mapItem = this,
    isSelected = false
)
