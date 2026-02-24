package se.onemanstudio.playaroundwithai.feature.maps.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.feature.maps.data.api.MapApiService
import se.onemanstudio.playaroundwithai.feature.maps.data.dto.MapItemDto
import se.onemanstudio.playaroundwithai.feature.maps.data.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.feature.maps.domain.model.VehicleType

class MapRepositoryImplTest {

    private lateinit var mapApiService: MapApiService
    private lateinit var repository: MapRepositoryImpl

    @Before
    fun setUp() {
        mapApiService = mockk()
        repository = MapRepositoryImpl(mapApiService)
    }

    @Test
    fun `getMapItems when service returns data then repository returns mapped domain models`() = runTest {
        val testDtos = listOf(
            MapItemDto(
                id = "id1", lat = 59.32, lng = 18.06, name = "Scooter 1",
                type = VehicleTypeDto.SCOOTER, batteryLevel = 95, vehicleCode = "S1", nickname = "Scooty"
            ),
            MapItemDto(
                id = "id2", lat = 59.33, lng = 18.07, name = "Bike 1",
                type = VehicleTypeDto.BICYCLE, batteryLevel = 70, vehicleCode = "B1", nickname = "Bikey"
            )
        )
        coEvery { mapApiService.getMapItems(any(), any(), any()) } returns testDtos

        val result = repository.getMapItems(2, 59.3293, 18.0686)

        assertThat(result).hasSize(2)
        assertThat(result.first().id).isEqualTo("id1")
        assertThat(result.first().name).isEqualTo("Scooter 1")
        assertThat(result.first().type).isEqualTo(VehicleType.SCOOTER)
        assertThat(result.first().nickname).isEqualTo("Scooty")
        assertThat(result.last().id).isEqualTo("id2")
        assertThat(result.last().name).isEqualTo("Bike 1")
    }

    @Test
    fun `getMapItems when service returns empty list then repository returns empty list`() = runTest {
        coEvery { mapApiService.getMapItems(any(), any(), any()) } returns emptyList()

        val result = repository.getMapItems(0, 59.3293, 18.0686)

        assertThat(result).isEmpty()
    }
}
