package se.onemanstudio.playaroundwithai.core.data.util

import se.onemanstudio.playaroundwithai.core.data.feature.map.dto.VehicleTypeDto
import kotlin.random.Random

@Suppress("MagicNumber")
object MapDataGenerator {
    private const val CENTER_LAT = 59.3293
    private const val CENTER_LNG = 18.0686
    private val nicknames =
        listOf("Lucas", "Björn", "Anton", "Sotiris", "Simon", "Leif", "Mark", "Stefan", "Niklas", "Niclas", "Jonas", "Benjamin", "Aris")

    fun generateRandomLat(): Double {
        return CENTER_LAT + (Random.nextDouble() - 0.5) * 0.08
    }

    fun generateRandomLng(): Double {
        return CENTER_LNG + (Random.nextDouble() - 0.5) * 0.08
    }

    fun generateRandomVehicleType(): VehicleTypeDto {
        return if (Random.nextBoolean()) VehicleTypeDto.SCOOTER else VehicleTypeDto.BICYCLE
    }

    fun generateRandomBatteryLevel(): Int {
        return Random.nextInt(15, 100)
    }

    fun generateRandomVehicleCode(): String {
        return "${Random.nextInt(1000, 9999)}"
    }

    fun generateRandomNickname(): String {
        return "${nicknames.random()} ${Random.nextInt(1, 99)}"
    }
}
