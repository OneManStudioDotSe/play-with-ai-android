package se.onemanstudio.playaroundwithai.core.network.utils

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
