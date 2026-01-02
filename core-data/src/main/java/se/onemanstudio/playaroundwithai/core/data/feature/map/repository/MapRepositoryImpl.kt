package se.onemanstudio.playaroundwithai.core.data.feature.map.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import se.onemanstudio.playaroundwithai.core.data.feature.map.remote.dto.MapItemDto
import se.onemanstudio.playaroundwithai.core.data.model.MapItem
import se.onemanstudio.playaroundwithai.core.data.model.mapper.toDomain
import se.onemanstudio.playaroundwithai.core.data.util.MapDataGenerator
import javax.inject.Inject

@Suppress("MagicNumber")
class MapRepositoryImpl @Inject constructor() : MapRepository {
    override suspend fun getMapItems(count: Int): List<MapItem> = withContext(Dispatchers.IO) {
        delay(3_000) // simulate a backend request that takes some time to complete

        List(count) { i ->
            MapItemDto(
                id = "loc_$i",
                lat = MapDataGenerator.generateRandomLat(),
                lng = MapDataGenerator.generateRandomLng(),
                name = "Vehicle #$i",
                type = MapDataGenerator.generateRandomVehicleType(),
                batteryLevel = MapDataGenerator.generateRandomBatteryLevel(),
                vehicleCode = MapDataGenerator.generateRandomVehicleCode(),
                nickname = MapDataGenerator.generateRandomNickname()
            )
        }.map { it.toDomain() }
    }
}
