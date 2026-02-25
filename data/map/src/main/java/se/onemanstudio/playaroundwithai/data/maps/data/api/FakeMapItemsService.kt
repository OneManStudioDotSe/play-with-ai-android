package se.onemanstudio.playaroundwithai.data.maps.data.api

import kotlinx.coroutines.delay
import se.onemanstudio.playaroundwithai.data.maps.data.dto.MapItemDto
import se.onemanstudio.playaroundwithai.data.maps.data.settings.MapSettingsHolder
import se.onemanstudio.playaroundwithai.data.maps.data.util.MapDataGenerator
import javax.inject.Inject

private const val FAKE_DELAY = 1_500L
private const val DEGREES_PER_KM = 0.02

@Suppress("MagicNumber")
class FakeMapApiService @Inject constructor(
    private val mapSettingsHolder: MapSettingsHolder,
) : MapApiService {
    override suspend fun getMapItems(count: Int, centerLat: Double, centerLng: Double): List<MapItemDto> {
        delay(timeMillis = FAKE_DELAY)

        val spreadDegrees = mapSettingsHolder.searchRadiusKm.value * DEGREES_PER_KM

        return List(count) { i ->
            MapItemDto(
                id = "loc_$i",
                lat = MapDataGenerator.generateRandomLat(centerLat, spreadDegrees),
                lng = MapDataGenerator.generateRandomLng(centerLng, spreadDegrees),
                name = "Vehicle #$i",
                type = MapDataGenerator.generateRandomVehicleType(),
                batteryLevel = MapDataGenerator.generateRandomBatteryLevel(),
                vehicleCode = MapDataGenerator.generateRandomVehicleCode(),
                nickname = MapDataGenerator.generateRandomNickname()
            )
        }
    }
}
