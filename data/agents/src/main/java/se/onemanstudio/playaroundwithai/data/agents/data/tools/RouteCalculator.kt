package se.onemanstudio.playaroundwithai.data.agents.data.tools

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_KM = 6371.0
private const val BRUTE_FORCE_THRESHOLD = 8
private const val WALKING_SPEED_KMH = 5.0
private const val MINUTES_PER_HOUR = 60

data class RouteResult(
    val orderedIndices: List<Int>,
    val totalDistanceKm: Double,
    val totalWalkingMinutes: Int,
)

object RouteCalculator {

    fun findOptimalRoute(places: List<Pair<Double, Double>>): RouteResult {
        if (places.size <= 1) {
            return RouteResult(
                orderedIndices = places.indices.toList(),
                totalDistanceKm = 0.0,
                totalWalkingMinutes = 0,
            )
        }

        val orderedIndices = if (places.size <= BRUTE_FORCE_THRESHOLD) {
            bruteForceOptimal(places)
        } else {
            nearestNeighbor(places)
        }

        val orderedPlaces = orderedIndices.map { places[it] }
        val totalDistance = pathDistanceKm(orderedPlaces)
        val walkingMinutes = (totalDistance / WALKING_SPEED_KMH * MINUTES_PER_HOUR).toInt()

        return RouteResult(
            orderedIndices = orderedIndices,
            totalDistanceKm = totalDistance,
            totalWalkingMinutes = walkingMinutes,
        )
    }

    fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_KM * c
    }

    fun pathDistanceKm(ordered: List<Pair<Double, Double>>): Double {
        var distance = 0.0
        for (i in 0 until ordered.size - 1) {
            distance += haversineKm(ordered[i].first, ordered[i].second, ordered[i + 1].first, ordered[i + 1].second)
        }
        return distance
    }

    private fun bruteForceOptimal(places: List<Pair<Double, Double>>): List<Int> {
        val indices = places.indices.toList()
        var bestOrder = indices
        var bestDistance = Double.MAX_VALUE

        for (perm in permutations(indices)) {
            val orderedPlaces = perm.map { places[it] }
            val dist = pathDistanceKm(orderedPlaces)
            if (dist < bestDistance) {
                bestDistance = dist
                bestOrder = perm
            }
        }
        return bestOrder
    }

    private fun nearestNeighbor(places: List<Pair<Double, Double>>): List<Int> {
        val visited = mutableListOf(0)
        val remaining = (1 until places.size).toMutableList()

        while (remaining.isNotEmpty()) {
            val current = places[visited.last()]
            var nearestIdx = remaining.first()
            var nearestDist = Double.MAX_VALUE

            for (idx in remaining) {
                val dist = haversineKm(current.first, current.second, places[idx].first, places[idx].second)
                if (dist < nearestDist) {
                    nearestDist = dist
                    nearestIdx = idx
                }
            }

            visited.add(nearestIdx)
            remaining.remove(nearestIdx)
        }
        return visited
    }

    private fun <T> permutations(list: List<T>): List<List<T>> {
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
}
