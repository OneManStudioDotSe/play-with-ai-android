package se.onemanstudio.playaroundwithai.data.plan.data.tools

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RouteCalculatorTest {

    // region findOptimalRoute - empty and single place

    @Test
    fun `findOptimalRoute with empty list returns empty indices and zero distance`() {
        // GIVEN: No places

        // WHEN
        val result = RouteCalculator.findOptimalRoute(emptyList())

        // THEN
        assertThat(result.orderedIndices).isEmpty()
        assertThat(result.totalDistanceKm).isEqualTo(0.0)
        assertThat(result.totalWalkingMinutes).isEqualTo(0)
    }

    @Test
    fun `findOptimalRoute with single place returns index 0 and zero distance`() {
        // GIVEN: A single place
        val places = listOf(59.3293 to 18.0686) // Stockholm

        // WHEN
        val result = RouteCalculator.findOptimalRoute(places)

        // THEN
        assertThat(result.orderedIndices).containsExactly(0)
        assertThat(result.totalDistanceKm).isEqualTo(0.0)
        assertThat(result.totalWalkingMinutes).isEqualTo(0)
    }

    // endregion

    // region findOptimalRoute - two places

    @Test
    fun `findOptimalRoute with two places returns correct distance`() {
        // GIVEN: Two places (Stockholm Central, Stockholm Sodermalm)
        val places = listOf(
            59.3293 to 18.0686, // Central Stockholm
            59.3170 to 18.0716, // Sodermalm
        )

        // WHEN
        val result = RouteCalculator.findOptimalRoute(places)

        // THEN
        assertThat(result.orderedIndices).hasSize(2)
        assertThat(result.orderedIndices).containsExactly(0, 1).inOrder()
        assertThat(result.totalDistanceKm).isGreaterThan(0.0)
        assertThat(result.totalDistanceKm).isLessThan(5.0) // Should be about 1.4 km
    }

    // endregion

    // region findOptimalRoute - multiple places (brute force)

    @Test
    fun `findOptimalRoute with multiple places returns optimal route via brute force`() {
        // GIVEN: Four places arranged in a line from south to north
        // Optimal route should visit them in geographic order to minimize distance
        val places = listOf(
            59.30 to 18.07, // Southernmost
            59.35 to 18.07, // Northernmost
            59.32 to 18.07, // Middle south
            59.33 to 18.07, // Middle north
        )

        // WHEN
        val result = RouteCalculator.findOptimalRoute(places)

        // THEN: The optimal route visits south-to-north or north-to-south
        assertThat(result.orderedIndices).hasSize(4)
        assertThat(result.totalDistanceKm).isGreaterThan(0.0)

        // The brute force should find the linear ordering (0, 2, 3, 1) or its reverse (1, 3, 2, 0)
        val ordered = result.orderedIndices
        val orderedLats = ordered.map { places[it].first }
        val isAscending = orderedLats == orderedLats.sorted()
        val isDescending = orderedLats == orderedLats.sortedDescending()
        assertThat(isAscending || isDescending).isTrue()
    }

    @Test
    fun `findOptimalRoute with brute force is better than arbitrary order`() {
        // GIVEN: Places where the naive index order is suboptimal
        val places = listOf(
            59.30 to 18.07, // South
            59.35 to 18.07, // North (far from south)
            59.31 to 18.07, // Near south
            59.34 to 18.07, // Near north
        )
        val naiveOrder = listOf(
            places[0], places[1], places[2], places[3],
        )
        val naiveDistance = RouteCalculator.pathDistanceKm(naiveOrder)

        // WHEN
        val result = RouteCalculator.findOptimalRoute(places)

        // THEN: Optimal route distance should be less than or equal to naive order
        assertThat(result.totalDistanceKm).isAtMost(naiveDistance)
    }

    // endregion

    // region findOptimalRoute - walking minutes

    @Test
    fun `findOptimalRoute calculates walking minutes from distance`() {
        // GIVEN: Two places roughly 5 km apart
        // Walking speed is 5 km/h, so 5 km should take about 60 minutes
        val places = listOf(
            59.30 to 18.07,
            59.3449 to 18.07, // ~5 km north
        )

        // WHEN
        val result = RouteCalculator.findOptimalRoute(places)

        // THEN: Walking minutes should be proportional to distance at 5 km/h
        val expectedMinutes = (result.totalDistanceKm / 5.0 * 60).toInt()
        assertThat(result.totalWalkingMinutes).isEqualTo(expectedMinutes)
    }

    // endregion

    // region haversineKm

    @Test
    fun `haversineKm returns known distance between Stockholm and Gothenburg`() {
        // GIVEN: Stockholm (59.3293, 18.0686) and Gothenburg (57.7089, 11.9746)
        // Known distance is approximately 398 km

        // WHEN
        val distance = RouteCalculator.haversineKm(59.3293, 18.0686, 57.7089, 11.9746)

        // THEN
        assertThat(distance).isWithin(10.0).of(398.0)
    }

    @Test
    fun `haversineKm returns zero for same point`() {
        // GIVEN: The same coordinates

        // WHEN
        val distance = RouteCalculator.haversineKm(59.3293, 18.0686, 59.3293, 18.0686)

        // THEN
        assertThat(distance).isEqualTo(0.0)
    }

    @Test
    fun `haversineKm returns known distance between London and Paris`() {
        // GIVEN: London (51.5074, -0.1278) and Paris (48.8566, 2.3522)
        // Known distance is approximately 343 km

        // WHEN
        val distance = RouteCalculator.haversineKm(51.5074, -0.1278, 48.8566, 2.3522)

        // THEN
        assertThat(distance).isWithin(10.0).of(343.0)
    }

    @Test
    fun `haversineKm is symmetric`() {
        // GIVEN: Two points

        // WHEN
        val distanceAtoB = RouteCalculator.haversineKm(59.3293, 18.0686, 57.7089, 11.9746)
        val distanceBtoA = RouteCalculator.haversineKm(57.7089, 11.9746, 59.3293, 18.0686)

        // THEN
        assertThat(distanceAtoB).isWithin(0.001).of(distanceBtoA)
    }

    // endregion

    // region pathDistanceKm

    @Test
    fun `pathDistanceKm returns zero for single point`() {
        // GIVEN: A single point

        // WHEN
        val distance = RouteCalculator.pathDistanceKm(listOf(59.3293 to 18.0686))

        // THEN
        assertThat(distance).isEqualTo(0.0)
    }

    @Test
    fun `pathDistanceKm returns zero for empty list`() {
        // GIVEN: An empty list

        // WHEN
        val distance = RouteCalculator.pathDistanceKm(emptyList())

        // THEN
        assertThat(distance).isEqualTo(0.0)
    }

    @Test
    fun `pathDistanceKm sums distances along path`() {
        // GIVEN: Three collinear points along the same longitude
        val pointA = 59.30 to 18.07
        val pointB = 59.32 to 18.07
        val pointC = 59.34 to 18.07

        // WHEN
        val pathDistance = RouteCalculator.pathDistanceKm(listOf(pointA, pointB, pointC))
        val directDistance = RouteCalculator.haversineKm(pointA.first, pointA.second, pointC.first, pointC.second)

        // THEN: For collinear points, path distance should equal direct distance
        assertThat(pathDistance).isWithin(0.01).of(directDistance)
    }

    @Test
    fun `pathDistanceKm for two points equals haversine between them`() {
        // GIVEN: Two points
        val pointA = 59.3293 to 18.0686
        val pointB = 57.7089 to 11.9746

        // WHEN
        val pathDistance = RouteCalculator.pathDistanceKm(listOf(pointA, pointB))
        val haversineDistance = RouteCalculator.haversineKm(pointA.first, pointA.second, pointB.first, pointB.second)

        // THEN
        assertThat(pathDistance).isWithin(0.001).of(haversineDistance)
    }

    // endregion
}
