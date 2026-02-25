package se.onemanstudio.playaroundwithai.feature.plan

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.plan.domain.model.PlanEvent
import se.onemanstudio.playaroundwithai.data.plan.domain.model.TripPlan
import se.onemanstudio.playaroundwithai.data.plan.domain.model.TripStop
import se.onemanstudio.playaroundwithai.data.plan.domain.repository.TripPlannerRepository
import se.onemanstudio.playaroundwithai.data.plan.domain.usecase.PlanTripUseCase
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanError
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.plan.util.MainCoroutineRule
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

    // region Test 1: Initial state is PlanUiState.Initial when API key is available

    @Test
    fun `initial state is Initial when API key is available`() = runTest {
        // Given
        val viewModel = createViewModel()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(PlanUiState.Initial)
    }

    // endregion

    // region Test 2: Initial state is Error(ApiKeyMissing) when API key is NOT available

    @Test
    fun `initial state is Error ApiKeyMissing when API key is not available`() = runTest {
        // Given
        val viewModel = createViewModel(
            apiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = false, isMapsKeyAvailable = true)
        )

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(PlanUiState.Error(PlanError.ApiKeyMissing))
    }

    // endregion

    // region Test 3: planTrip with missing API key sets Error state

    @Test
    fun `planTrip with missing API key sets Error state without transitioning to Running`() = runTest {
        // Given
        val viewModel = createViewModel(
            apiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = false, isMapsKeyAvailable = true)
        )
        val states = captureStates(viewModel)

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then - should never go to Running, stays as ApiKeyMissing
        assertThat(states.none { it is PlanUiState.Running }).isTrue()
        assertThat(states.last()).isEqualTo(PlanUiState.Error(PlanError.ApiKeyMissing))
    }

    // endregion

    // region Test 4: planTrip transitions to Running state

    @Test
    fun `planTrip transitions to Running state`() = runTest {
        // Given
        val viewModel = createViewModel(
            planEvents = flowOf(PlanEvent.Thinking("Analyzing your request..."))
        )
        val states = captureStates(viewModel)

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then - should have transitioned through Running
        assertThat(states.any { it is PlanUiState.Running }).isTrue()
        val runningState = states.first { it is PlanUiState.Running } as PlanUiState.Running
        assertThat(runningState.currentAction).isNotEmpty()
    }

    // endregion

    // region Test 5: planTrip emitting Complete transitions to Result state

    @Test
    fun `planTrip emitting Complete transitions to Result state`() = runTest {
        // Given
        val tripPlan = createTestTripPlan()
        val viewModel = createViewModel(
            planEvents = flowOf(
                PlanEvent.Thinking("Planning..."),
                PlanEvent.Complete(tripPlan),
            )
        )
        val states = captureStates(viewModel)

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then
        val finalState = states.last()
        assertThat(finalState).isInstanceOf(PlanUiState.Result::class.java)
        val resultState = finalState as PlanUiState.Result
        assertThat(resultState.plan.summary).isEqualTo(tripPlan.summary)
        assertThat(resultState.plan.stops).hasSize(tripPlan.stops.size)
        assertThat(resultState.plan.totalDistanceKm).isEqualTo(tripPlan.totalDistanceKm)
        assertThat(resultState.plan.totalWalkingMinutes).isEqualTo(tripPlan.totalWalkingMinutes)
    }

    // endregion

    // region Test 6: planTrip emitting Error transitions to Error state

    @Test
    fun `planTrip emitting PlanEvent Error transitions to Error state`() = runTest {
        // Given
        val errorMessage = "Gemini API failed"
        val viewModel = createViewModel(
            planEvents = flowOf(PlanEvent.Error(errorMessage))
        )
        val states = captureStates(viewModel)

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then
        val finalState = states.last()
        assertThat(finalState).isEqualTo(PlanUiState.Error(PlanError.Unknown(errorMessage)))
    }

    // endregion

    // region Test 7: planTrip catching IOException transitions to NetworkMissing error

    @Test
    fun `planTrip catching IOException transitions to NetworkMissing error`() = runTest {
        // Given
        val viewModel = createViewModel(
            planEvents = flow { throw IOException("No internet connection") }
        )
        val states = captureStates(viewModel)

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then
        val finalState = states.last()
        assertThat(finalState).isEqualTo(PlanUiState.Error(PlanError.NetworkMissing))
    }

    // endregion

    // region Test 8: resetToInitial sets Initial state

    @Test
    fun `resetToInitial sets Initial state after being in Error`() = runTest {
        // Given
        val viewModel = createViewModel(
            planEvents = flowOf(PlanEvent.Error("Something went wrong"))
        )
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Verify we are in Error state first
        assertThat(viewModel.uiState.value).isInstanceOf(PlanUiState.Error::class.java)

        // When
        viewModel.resetToInitial()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(PlanUiState.Initial)
    }

    @Test
    fun `resetToInitial sets Initial state after being in Result`() = runTest {
        // Given
        val viewModel = createViewModel(
            planEvents = flowOf(PlanEvent.Complete(createTestTripPlan()))
        )
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Verify we are in Result state first
        assertThat(viewModel.uiState.value).isInstanceOf(PlanUiState.Result::class.java)

        // When
        viewModel.resetToInitial()

        // Then
        assertThat(viewModel.uiState.value).isEqualTo(PlanUiState.Initial)
    }

    // endregion

    // region Test 9: planTrip accumulates steps correctly through Thinking/ToolCalling/ToolResult events

    @Test
    fun `planTrip accumulates steps correctly through Thinking, ToolCalling, and ToolResult events`() = runTest {
        // Given
        val tripPlan = createTestTripPlan()
        val viewModel = createViewModel(
            planEvents = flowOf(
                PlanEvent.Thinking("Analyzing your request..."),
                PlanEvent.ToolCalling("search_places", "Searching for coffee shops"),
                PlanEvent.ToolResult("search_places", "Found 3 coffee shops"),
                PlanEvent.ToolCalling("calculate_route", "Calculating optimal route"),
                PlanEvent.ToolResult("calculate_route", "Route calculated: 2.5 km"),
                PlanEvent.Complete(tripPlan),
            )
        )

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then - the final Result state carries all accumulated steps from the flow.
        // Note: StateFlow conflates intermediate Running states when using UnconfinedTestDispatcher,
        // so we verify the full step list in the final Result state.
        val resultState = viewModel.uiState.value
        assertThat(resultState).isInstanceOf(PlanUiState.Result::class.java)

        val result = resultState as PlanUiState.Result
        assertThat(result.steps).hasSize(5)

        // Verify step order and icons
        assertThat(result.steps[0].icon).isEqualTo(StepIcon.THINKING)
        assertThat(result.steps[0].label).isEqualTo("Analyzing your request...")

        assertThat(result.steps[1].icon).isEqualTo(StepIcon.TOOL_CALL)
        assertThat(result.steps[1].label).isEqualTo("Searching for coffee shops")

        assertThat(result.steps[2].icon).isEqualTo(StepIcon.TOOL_RESULT)
        assertThat(result.steps[2].label).isEqualTo("Found 3 coffee shops")

        assertThat(result.steps[3].icon).isEqualTo(StepIcon.TOOL_CALL)
        assertThat(result.steps[3].label).isEqualTo("Calculating optimal route")

        assertThat(result.steps[4].icon).isEqualTo(StepIcon.TOOL_RESULT)
        assertThat(result.steps[4].label).isEqualTo("Route calculated: 2.5 km")

        // Verify the plan summary is correctly mapped
        assertThat(result.plan.summary).isEqualTo(tripPlan.summary)
    }

    @Test
    fun `planTrip catching generic exception transitions to Unknown error`() = runTest {
        // Given
        val viewModel = createViewModel(
            planEvents = flow { throw IllegalStateException("Unexpected failure") }
        )
        val states = captureStates(viewModel)

        // When
        viewModel.planTrip("Coffee tour in Stockholm")
        advanceUntilIdle()

        // Then
        val finalState = states.last()
        assertThat(finalState).isInstanceOf(PlanUiState.Error::class.java)
        val errorState = finalState as PlanUiState.Error
        assertThat(errorState.error).isEqualTo(PlanError.Unknown("Unexpected failure"))
    }

    // endregion

    // --- Helpers ---

    private fun createViewModel(
        planEvents: Flow<PlanEvent> = flowOf(),
        apiKeyAvailability: ApiKeyAvailability = ApiKeyAvailability(
            isGeminiKeyAvailable = true,
            isMapsKeyAvailable = true
        ),
    ): PlanViewModel {
        val repository = mockk<TripPlannerRepository> {
            every { planTrip(any(), any(), any()) } returns planEvents
        }
        val planTripUseCase = PlanTripUseCase(repository)

        return PlanViewModel(planTripUseCase, apiKeyAvailability)
    }

    private fun captureStates(viewModel: PlanViewModel): List<PlanUiState> {
        val list = mutableListOf<PlanUiState>()
        viewModel.uiState
            .onEach { list.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher()))
        return list
    }

    private fun createTestTripPlan(): TripPlan {
        return TripPlan(
            summary = "A delightful coffee tour through Stockholm's best cafes.",
            stops = listOf(
                TripStop(
                    name = "Drop Coffee",
                    latitude = 59.3372,
                    longitude = 18.0649,
                    description = "Award-winning specialty coffee roaster",
                    category = "Coffee",
                    orderIndex = 0,
                ),
                TripStop(
                    name = "Johan & Nystrom",
                    latitude = 59.3189,
                    longitude = 18.0726,
                    description = "Popular coffee house in Sodermalm",
                    category = "Coffee",
                    orderIndex = 1,
                ),
            ),
            totalDistanceKm = 2.5,
            totalWalkingMinutes = 30,
        )
    }
}
