package se.onemanstudio.playaroundwithai.core.network.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

const val MIN_LATITUDE = -90.0
const val MAX_LATITUDE = 90.0
const val MIN_LONGITUDE = -180.0
const val MAX_LONGITUDE = 180.0

private const val EARTH_RADIUS_KM = 6371.0

fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
}

/**
 * Returns all permutations of the given list.
 * Complexity is O(n!), so only use for small inputs (n ≤ 8 or so).
 */
fun <T> permutations(list: List<T>): List<List<T>> {
    if (list.isEmpty()) return listOf(emptyList())
    val result = mutableListOf<List<T>>()
    for (i in list.indices) {
        val elem = list[i]
        val rest = list.take(i) + list.drop(i + 1)
        for (p in permutations(rest)) {
            result.add(listOf(elem) + p)
        }
    }
    return result
}
