package se.onemanstudio.playaroundwithai.feature.agents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.agents.domain.model.AgentEvent
import se.onemanstudio.playaroundwithai.data.agents.domain.usecase.PlanTripUseCase
import se.onemanstudio.playaroundwithai.feature.agents.states.AgentError
import se.onemanstudio.playaroundwithai.feature.agents.states.AgentStepUi
import se.onemanstudio.playaroundwithai.feature.agents.states.AgentUiState
import se.onemanstudio.playaroundwithai.feature.agents.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.agents.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.agents.states.TripStopUi
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

private const val STOCKHOLM_LAT = 59.3293
private const val STOCKHOLM_LNG = 18.0686

@HiltViewModel
class AgentViewModel @Inject constructor(
    private val planTripUseCase: PlanTripUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AgentUiState>(AgentUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _uiState.value = AgentUiState.Error(AgentError.ApiKeyMissing)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun planTrip(goal: String) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _uiState.value = AgentUiState.Error(AgentError.ApiKeyMissing)
            return
        }

        val steps = mutableListOf<AgentStepUi>()

        _uiState.value = AgentUiState.Running(
            steps = persistentListOf(),
            currentAction = "Starting...",
        )

        viewModelScope.launch {
            try {
                planTripUseCase(goal, STOCKHOLM_LAT, STOCKHOLM_LNG).collect { event ->
                    when (event) {
                        is AgentEvent.Thinking -> {
                            steps.add(AgentStepUi(icon = StepIcon.THINKING, label = event.message))
                            _uiState.update {
                                AgentUiState.Running(steps = steps.toPersistentList(), currentAction = event.message)
                            }
                        }

                        is AgentEvent.ToolCalling -> {
                            steps.add(AgentStepUi(icon = StepIcon.TOOL_CALL, label = event.summary))
                            _uiState.update {
                                AgentUiState.Running(steps = steps.toPersistentList(), currentAction = event.summary)
                            }
                        }

                        is AgentEvent.ToolResult -> {
                            steps.add(AgentStepUi(icon = StepIcon.TOOL_RESULT, label = event.summary))
                            _uiState.update {
                                AgentUiState.Running(steps = steps.toPersistentList(), currentAction = event.summary)
                            }
                        }

                        is AgentEvent.Complete -> {
                            val planUi = event.plan.toUi()
                            _uiState.value = AgentUiState.Result(steps = steps.toPersistentList(), plan = planUi)
                        }

                        is AgentEvent.Error -> {
                            _uiState.value = AgentUiState.Error(AgentError.Unknown(event.message))
                        }
                    }
                }
            } catch (e: IOException) {
                Timber.e(e, "AgentVM - Network error")
                _uiState.value = AgentUiState.Error(AgentError.NetworkMissing)
            } catch (e: Exception) {
                Timber.e(e, "AgentVM - Unexpected error")
                _uiState.value = AgentUiState.Error(AgentError.Unknown(e.message))
            }
        }
    }

    fun resetToInitial() {
        _uiState.value = AgentUiState.Initial
    }

    private fun se.onemanstudio.playaroundwithai.data.agents.domain.model.TripPlan.toUi(): TripPlanUi {
        return TripPlanUi(
            summary = summary,
            stops = stops.map { stop ->
                TripStopUi(
                    name = stop.name,
                    latitude = stop.latitude,
                    longitude = stop.longitude,
                    description = stop.description,
                    category = stop.category,
                    orderIndex = stop.orderIndex,
                )
            }.toPersistentList(),
            totalDistanceKm = totalDistanceKm,
            totalWalkingMinutes = totalWalkingMinutes,
        )
    }
}
