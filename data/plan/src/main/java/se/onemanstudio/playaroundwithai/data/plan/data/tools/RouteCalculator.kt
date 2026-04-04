package se.onemanstudio.playaroundwithai.data.plan.data.tools

import se.onemanstudio.playaroundwithai.core.network.utils.haversineKm
import se.onemanstudio.playaroundwithai.core.network.utils.permutations

private const val BRUTE_FORCE_THRESHOLD = 8
internal const val MINUTES_PER_HOUR = 60

data class RouteResult(
    val orderedIndices: List<Int>,
    val totalDistanceKm: Double,
    val totalWalkingMinutes: Int,
)

object RouteCalculator {
    fun findOptimalRoute(places: List<Pair<Double, Double>>, walkingSpeedKmh: Double): RouteResult {
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
        val walkingMinutes = (totalDistance / walkingSpeedKmh * MINUTES_PER_HOUR).toInt()

        return RouteResult(
            orderedIndices = orderedIndices,
            totalDistanceKm = totalDistance,
            totalWalkingMinutes = walkingMinutes,
        )
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
}
