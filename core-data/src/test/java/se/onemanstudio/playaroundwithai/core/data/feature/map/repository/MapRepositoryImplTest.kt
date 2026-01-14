package se.onemanstudio.playaroundwithai.core.data.feature.map.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.data.feature.map.api.MapApiService
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType

class MapRepositoryImplTest {

    private lateinit var mapApiService: MapApiService
    private lateinit var repository: MapRepositoryImpl

    @Before
    fun setUp() {
        // 1. Mock the ApiService, as we don't want to test its real implementation
        mapApiService = mockk()
        // 2. Create the repository instance with the mock service
        repository = MapRepositoryImpl(mapApiService)
    }

    @Test
    fun `getMapItems when service returns data then repository returns mapped domain models`() = runTest {
        // GIVEN: A list of DTOs that our mock service will return
        val testDtos = listOf(
            MapItemDto("id1", 59.32, 18.06, "Scooter 1", VehicleTypeDto.SCOOTER, 95, "S1", "Scooty"),
            MapItemDto("id2", 59.33, 18.07, "Bike 1", VehicleTypeDto.BICYCLE, 70, "B1", "Bikey")
        )
        // Instruct the mock to return our test DTOs when getMapItems is called
        coEvery { mapApiService.getMapItems(any()) } returns testDtos

        // WHEN: We call the repository method
        val result = repository.getMapItems(2)

        // THEN: The result should be a list of correctly mapped domain models
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
        // GIVEN: The mock service will return an empty list
        coEvery { mapApiService.getMapItems(any()) } returns emptyList()

        // WHEN: We call the repository method
        val result = repository.getMapItems(0)

        // THEN: The result should be an empty list
        assertThat(result).isEmpty()
    }
}
