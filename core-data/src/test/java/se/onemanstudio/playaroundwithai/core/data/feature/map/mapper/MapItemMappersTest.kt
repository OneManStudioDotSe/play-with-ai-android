package se.onemanstudio.playaroundwithai.core.data.feature.map.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType

class MapItemMappersTest {

    @Test
    fun `toDomain maps scooter DTO to domain model correctly`() {
        // GIVEN: A scooter DTO
        val dto = MapItemDto(
            id = "scooter-1",
            lat = 59.3293,
            lng = 18.0686,
            name = "Scooter Alpha",
            type = VehicleTypeDto.SCOOTER,
            batteryLevel = 85,
            vehicleCode = "SC-001",
            nickname = "Zippy"
        )

        // WHEN
        val domain = dto.toDomain()

        // THEN
        assertThat(domain.id).isEqualTo("scooter-1")
        assertThat(domain.lat).isEqualTo(59.3293)
        assertThat(domain.lng).isEqualTo(18.0686)
        assertThat(domain.name).isEqualTo("Scooter Alpha")
        assertThat(domain.type).isEqualTo(VehicleType.SCOOTER)
        assertThat(domain.batteryLevel).isEqualTo(85)
        assertThat(domain.vehicleCode).isEqualTo("SC-001")
        assertThat(domain.nickname).isEqualTo("Zippy")
    }

    @Test
    fun `toDomain maps bicycle DTO to domain model correctly`() {
        // GIVEN: A bicycle DTO
        val dto = MapItemDto(
            id = "bike-1",
            lat = 59.34,
            lng = 18.07,
            name = "Bike Beta",
            type = VehicleTypeDto.BICYCLE,
            batteryLevel = 42,
            vehicleCode = "BK-002",
            nickname = "Pedals"
        )

        // WHEN
        val domain = dto.toDomain()

        // THEN
        assertThat(domain.type).isEqualTo(VehicleType.BICYCLE)
        assertThat(domain.batteryLevel).isEqualTo(42)
        assertThat(domain.nickname).isEqualTo("Pedals")
    }

    @Test
    fun `toDomain preserves coordinate precision`() {
        // GIVEN: A DTO with high-precision coordinates
        val dto = MapItemDto(
            id = "id",
            lat = 59.329323456789,
            lng = 18.068612345678,
            name = "Precise",
            type = VehicleTypeDto.SCOOTER,
            batteryLevel = 100,
            vehicleCode = "P1",
            nickname = "Dot"
        )

        // WHEN
        val domain = dto.toDomain()

        // THEN: Coordinates should not lose precision
        assertThat(domain.lat).isEqualTo(59.329323456789)
        assertThat(domain.lng).isEqualTo(18.068612345678)
    }
}
