package se.onemanstudio.playaroundwithai.data.explore.domain.utils

import se.onemanstudio.playaroundwithai.core.network.utils.haversineKm

fun calculatePathDistance(startLat: Double, startLng: Double, path: List<Pair<Double, Double>>): Double {
    var distance = 0.0
    var currentLat = startLat
    var currentLng = startLng
    path.forEach { (nextLat, nextLng) ->
        distance += haversineKm(currentLat, currentLng, nextLat, nextLng)
        currentLat = nextLat
        currentLng = nextLng
    }
    return distance
}
