package se.onemanstudio.playaroundwithai.feature.maps.utils

import com.google.android.gms.maps.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371

fun calculatePathDistance(start: LatLng, path: List<LatLng>): Double {
    var distance = 0.0
    var current = start
    path.forEach { next ->
        distance += distanceBetween(current, next)
        current = next
    }
    return distance
}

fun distanceBetween(p1: LatLng, p2: LatLng): Double {
    val r = EARTH_RADIUS_KM
    val dLat = Math.toRadians(p2.latitude - p1.latitude)
    val dLon = Math.toRadians(p2.longitude - p1.longitude)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
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
