package se.onemanstudio.playaroundwithai.feature.plan

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
import se.onemanstudio.playaroundwithai.data.plan.domain.model.PlanEvent
import se.onemanstudio.playaroundwithai.data.plan.domain.usecase.PlanTripUseCase
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanError
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanStepUi
import se.onemanstudio.playaroundwithai.feature.plan.states.PlanUiState
import se.onemanstudio.playaroundwithai.feature.plan.states.StepIcon
import se.onemanstudio.playaroundwithai.feature.plan.states.TripPlanUi
import se.onemanstudio.playaroundwithai.feature.plan.states.TripStopUi
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val planTripUseCase: PlanTripUseCase,
    private val apiKeyAvailability: ApiKeyAvailability,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlanUiState>(PlanUiState.Initial)
    val uiState = _uiState.asStateFlow()

    init {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _uiState.value = PlanUiState.Error(PlanError.ApiKeyMissing)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun planTrip(goal: String, latitude: Double, longitude: Double) {
        if (!apiKeyAvailability.isGeminiKeyAvailable) {
            _uiState.value = PlanUiState.Error(PlanError.ApiKeyMissing)
            return
        }

        val steps = mutableListOf<PlanStepUi>()

        _uiState.value = PlanUiState.Running(
            steps = persistentListOf(),
            currentAction = "Starting...",
        )

        viewModelScope.launch {
            try {
                planTripUseCase(goal, latitude, longitude).collect { event ->
                    when (event) {
                        is PlanEvent.Thinking -> {
                            steps.add(PlanStepUi(icon = StepIcon.THINKING, label = event.message))
                            _uiState.update {
                                PlanUiState.Running(steps = steps.toPersistentList(), currentAction = event.message)
                            }
                        }

                        is PlanEvent.ToolCalling -> {
                            steps.add(PlanStepUi(icon = StepIcon.TOOL_CALL, label = event.summary))
                            _uiState.update {
                                PlanUiState.Running(steps = steps.toPersistentList(), currentAction = event.summary)
                            }
                        }

                        is PlanEvent.ToolResult -> {
                            steps.add(PlanStepUi(icon = StepIcon.TOOL_RESULT, label = event.summary))
                            _uiState.update {
                                PlanUiState.Running(steps = steps.toPersistentList(), currentAction = event.summary)
                            }
                        }

                        is PlanEvent.Complete -> {
                            val planUi = event.plan.toUi()
                            _uiState.value = PlanUiState.Result(steps = steps.toPersistentList(), plan = planUi)
                        }

                        is PlanEvent.Error -> {
                            _uiState.value = PlanUiState.Error(PlanError.Unknown(event.message))
                        }
                    }
                }
            } catch (e: IOException) {
                Timber.e(e, "PlanVM - Network error")
                _uiState.value = PlanUiState.Error(PlanError.NetworkMissing)
            } catch (e: Exception) {
                Timber.e(e, "PlanVM - Unexpected error")
                _uiState.value = PlanUiState.Error(PlanError.Unknown(e.message))
            }
        }
    }

    fun resetToInitial() {
        _uiState.value = PlanUiState.Initial
    }

    private fun se.onemanstudio.playaroundwithai.data.plan.domain.model.TripPlan.toUi(): TripPlanUi {
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
