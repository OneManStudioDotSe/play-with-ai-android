package se.onemanstudio.playaroundwithai.data.maps.data.util

import se.onemanstudio.playaroundwithai.data.maps.data.dto.VehicleTypeDto
import kotlin.random.Random

@Suppress("MagicNumber")
object MapDataGenerator {
    private const val CENTER_LAT = 59.3293
    private const val CENTER_LNG = 18.0686
    private val nicknames = listOf("Lucas", "Björn", "Anton", "Sotiris", "Simon", "Mark", "Stefan", "Niklas", "Jonas", "Benjamin", "Aris")

    fun generateRandomLat(centerLat: Double = CENTER_LAT): Double {
        return centerLat + (Random.nextDouble() - 0.5) * 0.08
    }

    fun generateRandomLng(centerLng: Double = CENTER_LNG): Double {
        return centerLng + (Random.nextDouble() - 0.5) * 0.08
    }

    fun generateRandomVehicleType(): VehicleTypeDto {
        return if (Random.nextBoolean()) VehicleTypeDto.Scooter else VehicleTypeDto.Bicycle
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
