package se.onemanstudio.playaroundwithai.data.explore.data.api

import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.data.explore.data.dto.ExploreItemDto
import se.onemanstudio.playaroundwithai.data.explore.data.settings.ExploreSettingsHolder
import se.onemanstudio.playaroundwithai.data.explore.data.util.ExploreDataGenerator
import javax.inject.Inject

private const val FAKE_DELAY = 1_500L
private const val DEGREES_PER_KM = 0.02

@Suppress("MagicNumber")
class FakeExploreItemsService @Inject constructor(
    private val exploreSettingsHolder: ExploreSettingsHolder,
) : ExploreApiService {
    override suspend fun getExploreItems(count: Int, centerLat: Double, centerLng: Double): List<ExploreItemDto> {
        delay(timeMillis = FAKE_DELAY)

        val spreadDegrees = exploreSettingsHolder.searchRadiusKm.value * DEGREES_PER_KM

        return List(count) { i ->
            ExploreItemDto(
                id = "loc_$i",
                lat = ExploreDataGenerator.generateRandomLat(centerLat, spreadDegrees),
                lng = ExploreDataGenerator.generateRandomLng(centerLng, spreadDegrees),
                name = "Vehicle #$i",
                type = ExploreDataGenerator.generateRandomVehicleType(),
                batteryLevel = ExploreDataGenerator.generateRandomBatteryLevel(),
                vehicleCode = ExploreDataGenerator.generateRandomVehicleCode(),
                nickname = ExploreDataGenerator.generateRandomNickname()
            )
        }
    }
}
