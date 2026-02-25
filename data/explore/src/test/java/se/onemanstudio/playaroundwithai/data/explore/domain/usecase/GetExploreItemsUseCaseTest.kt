package se.onemanstudio.playaroundwithai.data.explore.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.explore.domain.model.ExploreItem
import se.onemanstudio.playaroundwithai.data.explore.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.data.explore.domain.repository.ExplorePointsRepository
import kotlin.test.assertFailsWith

class GetExploreItemsUseCaseTest {

    private lateinit var explorePointsRepository: ExplorePointsRepository
    private lateinit var useCase: GetExploreItemsUseCase

    @Before
    fun setUp() {
        explorePointsRepository = mockk()
        useCase = GetExploreItemsUseCase(explorePointsRepository)
    }

    @Test
    fun `invoke with valid params delegates to repository`() = runTest {
        val expectedItems = listOf(
            ExploreItem(
                id = "id1", lat = 59.32, lng = 18.06, name = "Scooter 1",
                type = VehicleType.Scooter, batteryLevel = 95, vehicleCode = "S1", nickname = "Scooty"
            ),
            ExploreItem(
                id = "id2", lat = 59.33, lng = 18.07, name = "Bike 1",
                type = VehicleType.Bicycle, batteryLevel = 70, vehicleCode = "B1", nickname = "Bikey"
            )
        )
        coEvery { explorePointsRepository.getExploreItems(2, 59.3293, 18.0686) } returns expectedItems

        val result = useCase(count = 2, centerLat = 59.3293, centerLng = 18.0686)

        assertThat(result).hasSize(2)
        assertThat(result.first().id).isEqualTo("id1")
        assertThat(result.first().type).isEqualTo(VehicleType.Scooter)
        assertThat(result.last().id).isEqualTo("id2")
        assertThat(result.last().type).isEqualTo(VehicleType.Bicycle)
        coVerify(exactly = 1) { explorePointsRepository.getExploreItems(2, 59.3293, 18.0686) }
    }

    @Test
    fun `invoke with count of zero throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(count = 0, centerLat = 59.3293, centerLng = 18.0686)
        }
    }

    @Test
    fun `invoke with count exceeding max throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(count = MAX_ITEM_COUNT + 1, centerLat = 59.3293, centerLng = 18.0686)
        }
    }

    @Test
    fun `invoke with invalid latitude throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(count = 10, centerLat = 91.0, centerLng = 18.0686)
        }
    }

    @Test
    fun `invoke with invalid longitude throws IllegalArgumentException`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            useCase(count = 10, centerLat = 59.3293, centerLng = 181.0)
        }
    }

    @Test
    fun `invoke when repository returns empty list returns empty list`() = runTest {
        coEvery { explorePointsRepository.getExploreItems(any(), any(), any()) } returns emptyList()

        val result = useCase(count = 5, centerLat = 59.3293, centerLng = 18.0686)

        assertThat(result).isEmpty()
    }
}
