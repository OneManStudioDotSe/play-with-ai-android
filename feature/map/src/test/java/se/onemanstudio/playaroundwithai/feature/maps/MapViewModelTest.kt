package se.onemanstudio.playaroundwithai.feature.maps

import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.maps.domain.model.MapItem
import se.onemanstudio.playaroundwithai.data.maps.domain.model.VehicleType
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapSuggestionsRepository
import se.onemanstudio.playaroundwithai.data.maps.domain.repository.MapPointsRepository
import se.onemanstudio.playaroundwithai.data.maps.domain.usecase.GetMapItemsUseCase
import se.onemanstudio.playaroundwithai.data.maps.domain.usecase.GetSuggestedPlacesUseCase
import se.onemanstudio.playaroundwithai.feature.maps.states.MapError
import se.onemanstudio.playaroundwithai.feature.maps.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.util.MainCoroutineRule
import se.onemanstudio.playaroundwithai.data.maps.data.settings.MapSettingsHolder
import se.onemanstudio.playaroundwithai.data.maps.util.NetworkMonitor

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private val repository: MapPointsRepository = mockk()
    private val mapSuggestionsRepository: MapSuggestionsRepository = mockk()
    private val getSuggestedPlacesUseCase = GetSuggestedPlacesUseCase(mapSuggestionsRepository)
    private val networkMonitor: NetworkMonitor = mockk()
    private val mapSettingsHolder = MapSettingsHolder()

    @Before
    fun setup() {
        every { networkMonitor.isNetworkAvailable() } returns true
    }

    // Test Data
    private val scooterItemDomain = MapItem(
        id = "1", lat = 59.0, lng = 18.0, name = "Scooter 1", type = VehicleType.Scooter,
        batteryLevel = 88, vehicleCode = "1234", nickname = "Scooty"
    )
    private val bikeItemDomain = MapItem(
        id = "2", lat = 59.0, lng = 18.0, name = "Bike 1", type = VehicleType.Bicycle,
        batteryLevel = 55, vehicleCode = "5678", nickname = "Bikey"
    )
    private val testDataDomain = listOf(scooterItemDomain, bikeItemDomain)

    @Test
    fun `loadMapData loads data successfully`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val expectedUiModels = testDataDomain.map { it.toUiModel() }

        // When
        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)

        // Then
        val finalState = states.last()
        assertFalse(finalState.isLoading)
        assertEquals(expectedUiModels, finalState.allLocations)
        assertEquals(expectedUiModels, finalState.visibleLocations)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `selectMarker updates focused marker`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)
        val scooterItemUi = scooterItemDomain.toUiModel()

        // When
        viewModel.selectMarker(scooterItemUi)

        // Then
        assertEquals(scooterItemUi, states.last().focusedMarker)

        // When (Deselect)
        viewModel.selectMarker(null)

        // Then
        assertNull(states.last().focusedMarker)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `setPathMode resets selection and updates mode`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)
        val scooterItemUi = scooterItemDomain.toUiModel()

        // Pre-condition: Select something and be in normal mode
        viewModel.selectMarker(scooterItemUi)

        // When
        viewModel.setPathMode(true)

        // Then
        val state = states.last()
        assertTrue(state.isPathMode)
        assertNull(state.focusedMarker)
        assertTrue(state.selectedLocations.isEmpty())
        assertTrue(state.optimalRoute.isEmpty())

        // When (Turn off)
        viewModel.setPathMode(false)

        // Then
        assertFalse(states.last().isPathMode)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `toggleFilter filters visible locations correctly`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)

        // Initial state
        assertEquals(2, states.last().visibleLocations.size)

        // When: Toggle OFF Scooters
        viewModel.toggleFilter(VehicleType.Scooter)

        // Then: Only Bikes visible
        val bikeState = states.last()
        assertFalse(bikeState.activeFilter.contains(VehicleType.Scooter))
        assertEquals(1, bikeState.visibleLocations.size)
        assertEquals(VehicleType.Bicycle, bikeState.visibleLocations.first().type)

        // When: Toggle ON Scooters
        viewModel.toggleFilter(VehicleType.Scooter)

        // Then: Both visible again
        assertEquals(2, states.last().visibleLocations.size)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `toggleSelection adds and removes items in Path Mode`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)
        viewModel.setPathMode(true)
        val scooterItemUi = scooterItemDomain.toUiModel()
        val bikeItemUi = bikeItemDomain.toUiModel()

        // When: Select first item
        viewModel.toggleSelection(scooterItemUi)

        // Then
        assertEquals(1, states.last().selectedLocations.size)
        assertEquals(scooterItemUi.id, states.last().selectedLocations.first().id)

        // When: Select second item
        viewModel.toggleSelection(bikeItemUi)

        // Then
        assertEquals(2, states.last().selectedLocations.size)

        // When: Toggle first item again (Deselect)
        viewModel.toggleSelection(scooterItemUi)

        // Then
        assertEquals(1, states.last().selectedLocations.size)
        assertEquals(bikeItemUi.id, states.last().selectedLocations.first().id)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `toggleSelection enforces max selection limit`() = runTest {
        // Given: MAX_SELECTABLE_POINTS is 8, so we need 9 items to test the limit
        val itemCount = MapConstants.MAX_SELECTABLE_POINTS + 1
        val manyItemsDomain = (1..itemCount).map {
            MapItem(
                id = it.toString(), lat = 0.0, lng = 0.0, name = "Item $it", type = VehicleType.Scooter,
                batteryLevel = 100, vehicleCode = "$it", nickname = "Item $it"
            )
        }
        coEvery { repository.getMapItems(any(), any(), any()) } returns manyItemsDomain
        val manyItemsUi = manyItemsDomain.map { it.toUiModel() }

        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)
        viewModel.setPathMode(true)

        // When: Add items up to the max
        repeat(MapConstants.MAX_SELECTABLE_POINTS) { i ->
            viewModel.toggleSelection(manyItemsUi[i])
        }

        // Then: Verify max selected
        assertEquals(MapConstants.MAX_SELECTABLE_POINTS, states.last().selectedLocations.size)

        // When: Try to add one more item beyond the limit
        viewModel.toggleSelection(manyItemsUi[MapConstants.MAX_SELECTABLE_POINTS])

        // Then: Still at max (limit is enforced)
        assertEquals(MapConstants.MAX_SELECTABLE_POINTS, states.last().selectedLocations.size)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `calculateOptimalRoute updates route state`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val viewModel = createViewModel()
        val states = captureStates(viewModel)
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)
        viewModel.setPathMode(true)
        val scooterItemUi = scooterItemDomain.toUiModel()
        val bikeItemUi = bikeItemDomain.toUiModel()

        // When: Select two points
        viewModel.toggleSelection(scooterItemUi)
        viewModel.toggleSelection(bikeItemUi)
        val userLocation = LatLng(59.05, 18.05)
        viewModel.calculateOptimalRoute(userLocation)

        // Then
        val state = states.last()
        assertTrue("Route should not be empty", state.optimalRoute.isNotEmpty())
        assertEquals(3, state.optimalRoute.size) // user location + 2 selected points
        assertEquals(userLocation, state.optimalRoute.first())
        assertTrue(state.routeDistanceMeters > 0)
        assertTrue(state.routeDurationMinutes > 0)

        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `loadMapData sets ApiKeyMissing when Maps key is missing`() = runTest {
        // Given
        coEvery { repository.getMapItems(any(), any(), any()) } returns testDataDomain
        val viewModel = createViewModel(
            apiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = true, isMapsKeyAvailable = false)
        )
        val states = captureStates(viewModel)

        // When
        viewModel.loadMapData(TEST_CENTER_LAT, TEST_CENTER_LNG)

        // Then
        val finalState = states.last()
        assertFalse(finalState.isLoading)
        assertEquals(MapError.ApiKeyMissing, finalState.error)

        viewModel.viewModelScope.cancel()
    }

    // --- Helpers ---
    private fun createViewModel(
        apiKeyAvailability: ApiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = true, isMapsKeyAvailable = true)
    ): MapViewModel {
        return MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, apiKeyAvailability, networkMonitor, mapSettingsHolder)
    }

    private fun captureStates(viewModel: MapViewModel): List<MapUiState> {
        val list = mutableListOf<MapUiState>()
        viewModel.uiState
            .onEach { list.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher()))
        return list
    }

    companion object {
        private const val TEST_CENTER_LAT = 59.3293
        private const val TEST_CENTER_LNG = 18.0686
    }
}
