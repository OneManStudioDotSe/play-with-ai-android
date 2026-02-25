package se.onemanstudio.playaroundwithai.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import se.onemanstudio.playaroundwithai.core.network.tracking.DailyTokenUsage
import se.onemanstudio.playaroundwithai.data.chat.domain.usecase.GetWeeklyTokenUsageUseCase
import se.onemanstudio.playaroundwithai.data.explore.data.settings.ExploreSettingsHolder
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    getWeeklyTokenUsageUseCase: GetWeeklyTokenUsageUseCase,
    private val exploreSettingsHolder: ExploreSettingsHolder,
) : ViewModel() {

    val weeklyUsage: StateFlow<List<DailyTokenUsage>> = getWeeklyTokenUsageUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val vehicleCount: StateFlow<Int> = exploreSettingsHolder.vehicleCount
    val searchRadiusKm: StateFlow<Float> = exploreSettingsHolder.searchRadiusKm

    private val _selectedDayIndex = MutableStateFlow<Int?>(null)
    val selectedDayIndex: StateFlow<Int?> = _selectedDayIndex

    fun onBarTapped(index: Int) {
        _selectedDayIndex.value = if (_selectedDayIndex.value == index) null else index
    }

    fun onVehicleCountChange(count: Int) {
        exploreSettingsHolder.updateVehicleCount(count)
    }

    fun onSearchRadiusChange(radius: Float) {
        exploreSettingsHolder.updateSearchRadiusKm(radius)
    }
}
