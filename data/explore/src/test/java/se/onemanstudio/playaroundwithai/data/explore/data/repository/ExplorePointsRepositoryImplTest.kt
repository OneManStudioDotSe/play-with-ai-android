package se.onemanstudio.playaroundwithai.data.explore.data.repository

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.explore.data.api.ExploreApiService
import se.onemanstudio.playaroundwithai.data.explore.data.dto.ExploreItemDto
import se.onemanstudio.playaroundwithai.data.explore.data.dto.VehicleTypeDto
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType

class ExplorePointsRepositoryImplTest {

    private lateinit var exploreApiService: ExploreApiService
    private lateinit var repository: ExplorePointsRepositoryImpl

    @Before
    fun setUp() {
        exploreApiService = mockk()
        repository = ExplorePointsRepositoryImpl(exploreApiService)
    }

    @Test
    fun `getExploreItems when service returns data then repository returns mapped domain models`() = runTest {
        val testDtos = listOf(
            ExploreItemDto(
                id = "id1", lat = 59.32, lng = 18.06, name = "Scooter 1",
                type = VehicleTypeDto.Scooter, batteryLevel = 95, vehicleCode = "S1", nickname = "Scooty"
            ),
            ExploreItemDto(
                id = "id2", lat = 59.33, lng = 18.07, name = "Bike 1",
                type = VehicleTypeDto.Bicycle, batteryLevel = 70, vehicleCode = "B1", nickname = "Bikey"
            )
        )
        coEvery { exploreApiService.getExploreItems(any(), any(), any()) } returns testDtos

        val result = repository.getExploreItems(2, 59.3293, 18.0686)

        assertThat(result).hasSize(2)
        assertThat(result.first().id).isEqualTo("id1")
        assertThat(result.first().name).isEqualTo("Scooter 1")
        assertThat(result.first().type).isEqualTo(VehicleType.Scooter)
        assertThat(result.first().nickname).isEqualTo("Scooty")
        assertThat(result.last().id).isEqualTo("id2")
        assertThat(result.last().name).isEqualTo("Bike 1")
    }

    @Test
    fun `getExploreItems when service returns empty list then repository returns empty list`() = runTest {
        coEvery { exploreApiService.getExploreItems(any(), any(), any()) } returns emptyList()

        val result = repository.getExploreItems(0, 59.3293, 18.0686)

        assertThat(result).isEmpty()
    }
}
