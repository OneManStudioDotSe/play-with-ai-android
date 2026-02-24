package se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository

class GetMapItemsUseCaseTest {

    private lateinit var mapRepository: MapRepository
    private lateinit var useCase: GetMapItemsUseCase

    @Before
    fun setUp() {
        mapRepository = mockk()
        useCase = GetMapItemsUseCase(mapRepository)
    }

    @Test
    fun `invoke with valid params delegates to repository`() = runTest {
        // GIVEN: Repository returns a list of map items
        val expectedItems = listOf(
            MapItem(
                id = "id1",
                lat = 59.32,
                lng = 18.06,
                name = "Scooter 1",
                type = VehicleType.SCOOTER,
                batteryLevel = 95,
                vehicleCode = "S1",
                nickname = "Scooty"
            ),
            MapItem(
                id = "id2",
                lat = 59.33,
                lng = 18.07,
                name = "Bike 1",
                type = VehicleType.BICYCLE,
                batteryLevel = 70,
                vehicleCode = "B1",
                nickname = "Bikey"
            )
        )
        coEvery { mapRepository.getMapItems(2, 59.3293, 18.0686) } returns expectedItems

        // WHEN
        val result = useCase(count = 2, centerLat = 59.3293, centerLng = 18.0686)

        // THEN
        assertThat(result).hasSize(2)
        assertThat(result.first().id).isEqualTo("id1")
        assertThat(result.first().type).isEqualTo(VehicleType.SCOOTER)
        assertThat(result.last().id).isEqualTo("id2")
        assertThat(result.last().type).isEqualTo(VehicleType.BICYCLE)
        coVerify(exactly = 1) { mapRepository.getMapItems(2, 59.3293, 18.0686) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with count of zero throws IllegalArgumentException`() = runTest {
        // GIVEN: Count is zero, which is below the minimum of 1

        // WHEN
        useCase(count = 0, centerLat = 59.3293, centerLng = 18.0686)

        // THEN: IllegalArgumentException is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with count exceeding max throws IllegalArgumentException`() = runTest {
        // GIVEN: Count exceeds the maximum allowed value
        val countOverMax = MAX_ITEM_COUNT + 1

        // WHEN
        useCase(count = countOverMax, centerLat = 59.3293, centerLng = 18.0686)

        // THEN: IllegalArgumentException is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with invalid latitude throws IllegalArgumentException`() = runTest {
        // GIVEN: Latitude is outside the valid range of -90 to 90

        // WHEN
        useCase(count = 10, centerLat = 91.0, centerLng = 18.0686)

        // THEN: IllegalArgumentException is thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with invalid longitude throws IllegalArgumentException`() = runTest {
        // GIVEN: Longitude is outside the valid range of -180 to 180

        // WHEN
        useCase(count = 10, centerLat = 59.3293, centerLng = 181.0)

        // THEN: IllegalArgumentException is thrown
    }

    @Test
    fun `invoke when repository returns empty list returns empty list`() = runTest {
        // GIVEN: Repository returns an empty list
        coEvery { mapRepository.getMapItems(any(), any(), any()) } returns emptyList()

        // WHEN
        val result = useCase(count = 5, centerLat = 59.3293, centerLng = 18.0686)

        // THEN
        assertThat(result).isEmpty()
    }
}
