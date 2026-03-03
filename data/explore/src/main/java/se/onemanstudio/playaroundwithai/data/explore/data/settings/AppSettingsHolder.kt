package se.onemanstudio.playaroundwithai.data.explore.data.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsHolder @Inject constructor() {

    private val _showTokenUsage = MutableStateFlow(false)
    val showTokenUsage: StateFlow<Boolean> = _showTokenUsage.asStateFlow()

    fun updateShowTokenUsage(enabled: Boolean) {
        _showTokenUsage.value = enabled
    }
}
