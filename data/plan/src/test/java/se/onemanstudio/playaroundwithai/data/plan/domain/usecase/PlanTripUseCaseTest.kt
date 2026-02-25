package se.onemanstudio.playaroundwithai.data.plan.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.plan.domain.model.PlanEvent
import se.onemanstudio.playaroundwithai.data.plan.domain.model.TripPlan
import se.onemanstudio.playaroundwithai.data.plan.domain.repository.TripPlannerRepository

class PlanTripUseCaseTest {

    private lateinit var repository: TripPlannerRepository
    private lateinit var useCase: PlanTripUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = PlanTripUseCase(repository)
    }

    // region Blank goal validation

    @Test
    fun `invoke with blank goal emits error event`() = runTest {
        // GIVEN: A blank goal

        // WHEN
        val events = useCase(goal = "   ", latitude = 59.33, longitude = 18.07).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(PlanEvent.Error::class.java)
        assertThat((events.first() as PlanEvent.Error).message).isEqualTo("Please describe your trip idea")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    @Test
    fun `invoke with empty goal emits error event`() = runTest {
        // GIVEN: An empty goal

        // WHEN
        val events = useCase(goal = "", latitude = 59.33, longitude = 18.07).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat((events.first() as PlanEvent.Error).message).isEqualTo("Please describe your trip idea")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    // endregion

    // region Goal length validation

    @Test
    fun `invoke with goal exceeding max length emits error event`() = runTest {
        // GIVEN: A goal that exceeds 1000 characters
        val longGoal = "a".repeat(1_001)

        // WHEN
        val events = useCase(goal = longGoal, latitude = 59.33, longitude = 18.07).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isInstanceOf(PlanEvent.Error::class.java)
        assertThat((events.first() as PlanEvent.Error).message)
            .isEqualTo("Trip description is too long (max 1000 characters)")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    @Test
    fun `invoke with goal at exactly max length delegates to repository`() = runTest {
        // GIVEN: A goal that is exactly 1000 characters
        val exactGoal = "a".repeat(1_000)
        val expectedEvent = PlanEvent.Thinking("Planning...")
        every { repository.planTrip(exactGoal, 59.33, 18.07) } returns flowOf(expectedEvent)

        // WHEN
        val events = useCase(goal = exactGoal, latitude = 59.33, longitude = 18.07).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isEqualTo(expectedEvent)
        verify(exactly = 1) { repository.planTrip(exactGoal, 59.33, 18.07) }
    }

    // endregion

    // region Latitude validation

    @Test
    fun `invoke with latitude below negative 90 emits error event`() = runTest {
        // GIVEN: An invalid latitude below -90

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = -90.1, longitude = 18.07).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat((events.first() as PlanEvent.Error).message).isEqualTo("Invalid location coordinates")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    @Test
    fun `invoke with latitude above 90 emits error event`() = runTest {
        // GIVEN: An invalid latitude above 90

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = 90.1, longitude = 18.07).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat((events.first() as PlanEvent.Error).message).isEqualTo("Invalid location coordinates")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    // endregion

    // region Longitude validation

    @Test
    fun `invoke with longitude below negative 180 emits error event`() = runTest {
        // GIVEN: An invalid longitude below -180

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = 59.33, longitude = -180.1).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat((events.first() as PlanEvent.Error).message).isEqualTo("Invalid location coordinates")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    @Test
    fun `invoke with longitude above 180 emits error event`() = runTest {
        // GIVEN: An invalid longitude above 180

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = 59.33, longitude = 180.1).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat((events.first() as PlanEvent.Error).message).isEqualTo("Invalid location coordinates")
        verify(exactly = 0) { repository.planTrip(any(), any(), any()) }
    }

    // endregion

    // region Boundary values

    @Test
    fun `invoke with latitude exactly negative 90 delegates to repository`() = runTest {
        // GIVEN: Latitude at the minimum boundary
        val expectedEvent = PlanEvent.Thinking("Planning...")
        every { repository.planTrip("Coffee tour", -90.0, 0.0) } returns flowOf(expectedEvent)

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = -90.0, longitude = 0.0).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isEqualTo(expectedEvent)
        verify(exactly = 1) { repository.planTrip("Coffee tour", -90.0, 0.0) }
    }

    @Test
    fun `invoke with latitude exactly 90 delegates to repository`() = runTest {
        // GIVEN: Latitude at the maximum boundary
        val expectedEvent = PlanEvent.Thinking("Planning...")
        every { repository.planTrip("Coffee tour", 90.0, 0.0) } returns flowOf(expectedEvent)

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = 90.0, longitude = 0.0).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isEqualTo(expectedEvent)
        verify(exactly = 1) { repository.planTrip("Coffee tour", 90.0, 0.0) }
    }

    @Test
    fun `invoke with longitude exactly negative 180 delegates to repository`() = runTest {
        // GIVEN: Longitude at the minimum boundary
        val expectedEvent = PlanEvent.Thinking("Planning...")
        every { repository.planTrip("Coffee tour", 0.0, -180.0) } returns flowOf(expectedEvent)

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = 0.0, longitude = -180.0).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isEqualTo(expectedEvent)
        verify(exactly = 1) { repository.planTrip("Coffee tour", 0.0, -180.0) }
    }

    @Test
    fun `invoke with longitude exactly 180 delegates to repository`() = runTest {
        // GIVEN: Longitude at the maximum boundary
        val expectedEvent = PlanEvent.Thinking("Planning...")
        every { repository.planTrip("Coffee tour", 0.0, 180.0) } returns flowOf(expectedEvent)

        // WHEN
        val events = useCase(goal = "Coffee tour", latitude = 0.0, longitude = 180.0).toList()

        // THEN
        assertThat(events).hasSize(1)
        assertThat(events.first()).isEqualTo(expectedEvent)
        verify(exactly = 1) { repository.planTrip("Coffee tour", 0.0, 180.0) }
    }

    // endregion

    // region Valid input delegation

    @Test
    fun `invoke with valid input delegates to repository and emits all events`() = runTest {
        // GIVEN: A valid goal and coordinates, with the repository emitting multiple events
        val goal = "Coffee tour in Stockholm"
        val latitude = 59.33
        val longitude = 18.07
        val tripPlan = TripPlan(
            summary = "Great coffee tour",
            stops = emptyList(),
            totalDistanceKm = 2.5,
            totalWalkingMinutes = 30,
        )
        val expectedEvents = listOf(
            PlanEvent.Thinking("Planning your trip..."),
            PlanEvent.ToolCalling("search_places", "Searching for coffee shops"),
            PlanEvent.ToolResult("search_places", "Found 3 coffee shops"),
            PlanEvent.Complete(tripPlan),
        )
        every { repository.planTrip(goal, latitude, longitude) } returns flowOf(*expectedEvents.toTypedArray())

        // WHEN
        val events = useCase(goal = goal, latitude = latitude, longitude = longitude).toList()

        // THEN
        assertThat(events).hasSize(4)
        assertThat(events).isEqualTo(expectedEvents)
        verify(exactly = 1) { repository.planTrip(goal, latitude, longitude) }
    }

    @Test
    fun `invoke with valid input does not call repository for validation errors`() = runTest {
        // GIVEN: Multiple validation error scenarios are tested above
        // This test verifies that valid input results in exactly one repository call
        val goal = "Visit museums"
        val expectedEvent = PlanEvent.Complete(
            TripPlan(summary = "Museum tour", stops = emptyList(), totalDistanceKm = 1.0, totalWalkingMinutes = 12)
        )
        every { repository.planTrip(goal, 59.33, 18.07) } returns flowOf(expectedEvent)

        // WHEN
        val events = useCase(goal = goal, latitude = 59.33, longitude = 18.07).toList()

        // THEN
        assertThat(events).containsExactly(expectedEvent)
        verify(exactly = 1) { repository.planTrip(goal, 59.33, 18.07) }
    }

    // endregion
}
