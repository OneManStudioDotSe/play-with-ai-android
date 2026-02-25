package se.onemanstudio.playaroundwithai.feature.explore.models

import com.google.android.gms.maps.model.LatLng
import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem as DomainExploreItem

/**
 * Represents an explore item that is displayed on the UI.
 *
 * @property mapItem The core domain model for the explore item.
 * @property isSelected Whether this item is currently selected in the UI.
 */
data class ExploreItemUiModel(
    val mapItem: DomainExploreItem,
    val isSelected: Boolean = false,
) {
    val id: String get() = mapItem.id
    val position: LatLng get() = LatLng(mapItem.lat, mapItem.lng)
    val type get() = mapItem.type
    val name get() = mapItem.name
}

/**
 * Converts a domain [DomainExploreItem] to a [ExploreItemUiModel] for the UI layer.
 */
fun DomainExploreItem.toUiModel() = ExploreItemUiModel(
    mapItem = this,
    isSelected = false
)
