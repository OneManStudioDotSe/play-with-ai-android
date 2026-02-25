package se.onemanstudio.playaroundwithai.data.maps.data.util

import se.onemanstudio.playaroundwithai.data.maps.data.dto.VehicleTypeDto
import kotlin.random.Random

@Suppress("MagicNumber")
object MapDataGenerator {
    private const val CENTER_LAT = 59.3293
    private const val CENTER_LNG = 18.0686
    private const val DEFAULT_SPREAD = 0.08
    private val nicknames = listOf("Lucas", "Björn", "Anton", "Sotiris", "Simon", "Mark", "Stefan", "Niklas", "Jonas", "Benjamin", "Aris")

    fun generateRandomLat(centerLat: Double = CENTER_LAT, spreadDegrees: Double = DEFAULT_SPREAD): Double {
        return centerLat + (Random.nextDouble() - 0.5) * spreadDegrees
    }

    fun generateRandomLng(centerLng: Double = CENTER_LNG, spreadDegrees: Double = DEFAULT_SPREAD): Double {
        return centerLng + (Random.nextDouble() - 0.5) * spreadDegrees
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
