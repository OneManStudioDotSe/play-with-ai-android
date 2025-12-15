package se.onemanstudio.playaroundwithai.feature.maps

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.feature.maps.models.ItemOnMap
import se.onemanstudio.playaroundwithai.feature.maps.utils.MapDataGenerator.generateRandomBatteryLevel
import se.onemanstudio.playaroundwithai.feature.maps.utils.MapDataGenerator.generateRandomLat
import se.onemanstudio.playaroundwithai.feature.maps.utils.MapDataGenerator.generateRandomLng
import se.onemanstudio.playaroundwithai.feature.maps.utils.MapDataGenerator.generateRandomNickname
import se.onemanstudio.playaroundwithai.feature.maps.utils.MapDataGenerator.generateRandomVehicleCode
import se.onemanstudio.playaroundwithai.feature.maps.utils.MapDataGenerator.generateRandomVehicleType
import javax.inject.Inject

@Suppress("MagicNumber")
class MapRepository @Inject constructor() {
    suspend fun generateRandomData(count: Int): List<ItemOnMap> = withContext(Dispatchers.IO) {
        delay(3_000) // simulate a backend request that takes some time to complete

        List(count) { i ->
            ItemOnMap(
                id = "loc_$i",
                lat = generateRandomLat(),
                lng = generateRandomLng(),
                name = "Vehicle #$i",
                type = generateRandomVehicleType(),
                batteryLevel = generateRandomBatteryLevel(),
                vehicleCode = generateRandomVehicleCode(),
                nickname = generateRandomNickname()
            )
        }
    }
}
