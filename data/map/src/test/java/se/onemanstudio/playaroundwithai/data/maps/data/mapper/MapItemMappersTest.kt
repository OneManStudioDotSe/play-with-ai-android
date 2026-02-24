package se.onemanstudio.playaroundwithai.data.maps.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.maps.data.dto.MapItemDto
import se.onemanstudio.playaroundwithai.data.maps.data.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.data.maps.domain.model.VehicleType

class MapItemMappersTest {

    @Test
    fun `toDomain maps scooter DTO to domain model correctly`() {
        val dto = MapItemDto(
            id = "scooter-1", lat = 59.3293, lng = 18.0686, name = "Scooter Alpha",
            type = VehicleTypeDto.Scooter, batteryLevel = 85, vehicleCode = "SC-001", nickname = "Zippy"
        )

        val domain = dto.toDomain()

        assertThat(domain.id).isEqualTo("scooter-1")
        assertThat(domain.lat).isEqualTo(59.3293)
        assertThat(domain.lng).isEqualTo(18.0686)
        assertThat(domain.name).isEqualTo("Scooter Alpha")
        assertThat(domain.type).isEqualTo(VehicleType.Scooter)
        assertThat(domain.batteryLevel).isEqualTo(85)
        assertThat(domain.vehicleCode).isEqualTo("SC-001")
        assertThat(domain.nickname).isEqualTo("Zippy")
    }

    @Test
    fun `toDomain maps bicycle DTO to domain model correctly`() {
        val dto = MapItemDto(
            id = "bike-1", lat = 59.34, lng = 18.07, name = "Bike Beta",
            type = VehicleTypeDto.Bicycle, batteryLevel = 42, vehicleCode = "BK-002", nickname = "Pedals"
        )

        val domain = dto.toDomain()

        assertThat(domain.type).isEqualTo(VehicleType.Bicycle)
        assertThat(domain.batteryLevel).isEqualTo(42)
        assertThat(domain.nickname).isEqualTo("Pedals")
    }

    @Test
    fun `toDomain preserves coordinate precision`() {
        val dto = MapItemDto(
            id = "id", lat = 59.329323456789, lng = 18.068612345678, name = "Precise",
            type = VehicleTypeDto.Scooter, batteryLevel = 100, vehicleCode = "P1", nickname = "Dot"
        )

        val domain = dto.toDomain()

        assertThat(domain.lat).isEqualTo(59.329323456789)
        assertThat(domain.lng).isEqualTo(18.068612345678)
    }
}
