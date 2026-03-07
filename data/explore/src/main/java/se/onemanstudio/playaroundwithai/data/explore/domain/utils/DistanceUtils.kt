package se.onemanstudio.playaroundwithai.data.explore.domain.utils

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371

fun calculatePathDistance(startLat: Double, startLng: Double, path: List<Pair<Double, Double>>): Double {
    var distance = 0.0
    var currentLat = startLat
    var currentLng = startLng
    path.forEach { (nextLat, nextLng) ->
        distance += distanceBetween(currentLat, currentLng, nextLat, nextLng)
        currentLat = nextLat
        currentLng = nextLng
    }
    return distance
}

fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
}

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
