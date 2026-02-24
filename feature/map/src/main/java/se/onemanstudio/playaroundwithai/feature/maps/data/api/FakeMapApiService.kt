package se.onemanstudio.playaroundwithai.feature.maps.data.api

import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.feature.maps.data.dto.MapItemDto
import se.onemanstudio.playaroundwithai.feature.maps.data.util.MapDataGenerator
import javax.inject.Inject

private const val FAKE_DELAY = 1_500L

@Suppress("MagicNumber")
class FakeMapApiService @Inject constructor() : MapApiService {
    override suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItemDto> {
        delay(timeMillis = FAKE_DELAY)

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
