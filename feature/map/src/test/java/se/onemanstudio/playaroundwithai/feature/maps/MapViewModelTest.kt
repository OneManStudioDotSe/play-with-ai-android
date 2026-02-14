package se.onemanstudio.playaroundwithai.feature.maps

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.VehicleType
import se.onemanstudio.playaroundwithai.core.domain.feature.map.repository.MapRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.MapItem
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetMapItemsUseCase
import se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase.GetSuggestedPlacesUseCase
import se.onemanstudio.playaroundwithai.feature.maps.models.toUiModel
import se.onemanstudio.playaroundwithai.feature.maps.states.MapUiState
import se.onemanstudio.playaroundwithai.feature.maps.util.MainCoroutineRule

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private val repository: MapRepository = mockk()
    private val geminiRepository: GeminiRepository = mockk()
    private val getSuggestedPlacesUseCase = GetSuggestedPlacesUseCase(geminiRepository)
    private val application: Application = mockk() // Mock the Application object
    private val connectivityManager: ConnectivityManager = mockk()
    private val network: Network = mockk()
    private val networkCapabilities: NetworkCapabilities = mockk()

    @Before
    fun setup() {
        every { application.getSystemService(ConnectivityManager::class.java) } returns connectivityManager
        every { connectivityManager.activeNetwork } returns network
        every { connectivityManager.getNetworkCapabilities(network) } returns networkCapabilities
        every { networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) } returns true
        every { application.getString(any()) } returns "Mocked String"
    }

    // Test Data
    private val scooterItemDomain = MapItem(
        id = "1", lat = 59.0, lng = 18.0, name = "Scooter 1", type = VehicleType.SCOOTER,
        batteryLevel = 88, vehicleCode = "1234", nickname = "Scooty"
    )
    private val bikeItemDomain = MapItem(
        id = "2", lat = 59.0, lng = 18.0, name = "Bike 1", type = VehicleType.BICYCLE,
        batteryLevel = 55, vehicleCode = "5678", nickname = "Bikey"
    )
    private val testDataDomain = listOf(scooterItemDomain, bikeItemDomain)

    @Test
    fun `init loads data successfully`() = runTest {
        // Given
        coEvery { repository.getMapItems(any()) } returns testDataDomain
        val expectedUiModels = testDataDomain.map { it.toUiModel() }

        // When
        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
        advanceUntilIdle()

        // Then
        val finalState = states.last()
        assertFalse(finalState.isLoading)
        assertEquals(expectedUiModels, finalState.allLocations)
        assertEquals(expectedUiModels, finalState.visibleLocations)
    }

    @Test
    fun `selectMarker updates focused marker`() = runTest {
        // Given
        coEvery { repository.getMapItems(any()) } returns testDataDomain
        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
        val scooterItemUi = scooterItemDomain.toUiModel()

        // When
        viewModel.selectMarker(scooterItemUi)

        // Then
        assertEquals(scooterItemUi, states.last().focusedMarker)

        // When (Deselect)
        viewModel.selectMarker(null)

        // Then
        assertNull(states.last().focusedMarker)
    }

    @Test
    fun `setPathMode resets selection and updates mode`() = runTest {
        // Given
        coEvery { repository.getMapItems(any()) } returns testDataDomain
        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
        val scooterItemUi = scooterItemDomain.toUiModel()

        // Pre-condition: Select something and be in normal mode
        viewModel.selectMarker(scooterItemUi)
        advanceUntilIdle()

        // When
        viewModel.setPathMode(true)
        advanceUntilIdle()

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
    }

    @Test
    fun `toggleFilter filters visible locations correctly`() = runTest {
        // Given
        coEvery { repository.getMapItems(any()) } returns testDataDomain
        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
        advanceUntilIdle()

        // Initial state
        assertEquals(2, states.last().visibleLocations.size)

        // When: Toggle OFF Scooters
        viewModel.toggleFilter(VehicleType.SCOOTER)

        // Then: Only Bikes visible
        val bikeState = states.last()
        assertFalse(bikeState.activeFilter.contains(VehicleType.SCOOTER))
        assertEquals(1, bikeState.visibleLocations.size)
        assertEquals(VehicleType.BICYCLE, bikeState.visibleLocations.first().type)

        // When: Toggle ON Scooters
        viewModel.toggleFilter(VehicleType.SCOOTER)

        // Then: Both visible again
        assertEquals(2, states.last().visibleLocations.size)
    }

    @Test
    fun `toggleSelection adds and removes items in Path Mode`() = runTest {
        // Given
        coEvery { repository.getMapItems(any()) } returns testDataDomain
        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
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
    }

    @Test
    fun `toggleSelection enforces max selection limit`() = runTest {
        // Given: MAX_SELECTABLE_POINTS is 8, so we need 9 items to test the limit
        val itemCount = MapConstants.MAX_SELECTABLE_POINTS + 1
        val manyItemsDomain = (1..itemCount).map {
            MapItem(
                id = it.toString(), lat = 0.0, lng = 0.0, name = "Item $it", type = VehicleType.SCOOTER,
                batteryLevel = 100, vehicleCode = "$it", nickname = "Item $it"
            )
        }
        coEvery { repository.getMapItems(any()) } returns manyItemsDomain
        val manyItemsUi = manyItemsDomain.map { it.toUiModel() }

        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
        viewModel.setPathMode(true)
        advanceUntilIdle()

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
    }

    @Test
    fun `calculateOptimalRoute updates route state`() = runTest {
        // Given
        coEvery { repository.getMapItems(any()) } returns testDataDomain
        val viewModel = MapViewModel(GetMapItemsUseCase(repository), getSuggestedPlacesUseCase, application) // Pass the mocked application
        val states = captureStates(viewModel)
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
    }

    // --- Helper ---
    private fun captureStates(viewModel: MapViewModel): List<MapUiState> {
        val list = mutableListOf<MapUiState>()
        viewModel.uiState
            .onEach { list.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher()))
        return list
    }
}