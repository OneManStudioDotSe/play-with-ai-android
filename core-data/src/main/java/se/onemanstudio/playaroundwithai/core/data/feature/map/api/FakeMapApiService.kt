package se.onemanstudio.playaroundwithai.core.data.feature.map.api

import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.util.MapDataGenerator
import javax.inject.Inject

private const val FAKE_DELAY = 1_500L

/**
 * A fake implementation of [MapApiService] for testing and development.
 * It simulates a network request and returns randomly generated data.
 */
@Suppress("MagicNumber")
class FakeMapApiService @Inject constructor() : MapApiService {
    override suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItemDto> {
        delay(timeMillis = FAKE_DELAY) // simulate a backend request that takes some time to complete

        return List(count) { i ->
            MapItemDto(
                id = "loc_$i",
                lat = MapDataGenerator.generateRandomLat(centerLat),
                lng = MapDataGenerator.generateRandomLng(centerLng),
                name = "Vehicle #$i",
                type = MapDataGenerator.generateRandomVehicleType(),
                batteryLevel = MapDataGenerator.generateRandomBatteryLevel(),
                vehicleCode = MapDataGenerator.generateRandomVehicleCode(),
                nickname = MapDataGenerator.generateRandomNickname()
            )
        }
    }
}
