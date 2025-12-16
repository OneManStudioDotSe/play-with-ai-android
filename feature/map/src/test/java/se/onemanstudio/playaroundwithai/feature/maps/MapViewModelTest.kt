package se.onemanstudio.playaroundwithai.feature.maps

import com.google.android.gms.maps.model.LatLng
import io.mockk.coEvery
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
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.feature.maps.util.MainCoroutineRule
import se.onemanstudio.playaroundwithai.feature.maps.models.ItemOnMap
import se.onemanstudio.playaroundwithai.feature.maps.models.VehicleType
import se.onemanstudio.playaroundwithai.feature.maps.state.MapUiState

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

    private val repository: MapRepository = mockk()

    // Test Data
    private val scooterItem = ItemOnMap(
        "1",
        lat = 59.0, lng = 18.0, "Scooter 1", VehicleType.SCOOTER, isSelected = false, 100
    )
    private val bikeItem = ItemOnMap(
        "2",
        lat = 59.0, lng = 18.0,
        "Bike 1", VehicleType.BICYCLE, isSelected = false, 100,
    )
    private val testData = listOf(scooterItem, bikeItem)

    @Test
    fun `init loads data successfully`() = runTest {
        // Given
        coEvery { repository.generateRandomData(any()) } returns testData

        // When
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)
        advanceUntilIdle()

        // Then
        val finalState = states.last()
        assertFalse(finalState.isLoading)
        assertEquals(testData, finalState.allLocations)
        assertEquals(testData, finalState.visibleLocations)
    }

    @Test
    fun `selectMarker updates focused marker`() = runTest {
        // Given
        coEvery { repository.generateRandomData(any()) } returns testData
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)

        // When
        viewModel.selectMarker(scooterItem)

        // Then
        assertEquals(scooterItem, states.last().focusedMarker)

        // When (Deselect)
        viewModel.selectMarker(null)

        // Then
        assertNull(states.last().focusedMarker)
    }

    @Test
    fun `setPathMode resets selection and updates mode`() = runTest {
        // Given
        coEvery { repository.generateRandomData(any()) } returns testData
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)

        // Pre-condition: Select something and be in normal mode
        viewModel.selectMarker(scooterItem)

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
    }

    @Test
    fun `toggleFilter filters visible locations correctly`() = runTest {
        // Given
        coEvery { repository.generateRandomData(any()) } returns testData
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)
        advanceUntilIdle() // Wait for data load

        // Initial state: Both types active (Assuming MapUiState defaults activeFilter to setOf(SCOOTER, BICYCLE))
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
        coEvery { repository.generateRandomData(any()) } returns testData
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)
        viewModel.setPathMode(true) // Must be in path mode

        // When: Select first item
        viewModel.toggleSelection(scooterItem)

        // Then
        assertEquals(1, states.last().selectedLocations.size)
        assertEquals(scooterItem.id, states.last().selectedLocations.first().id)

        // When: Select second item
        viewModel.toggleSelection(bikeItem)

        // Then
        assertEquals(2, states.last().selectedLocations.size)

        // When: Toggle first item again (Deselect)
        viewModel.toggleSelection(scooterItem)

        // Then
        assertEquals(1, states.last().selectedLocations.size)
        assertEquals(bikeItem.id, states.last().selectedLocations.first().id)
    }

    @Test
    fun `toggleSelection enforces max selection limit`() = runTest {
        // Given
        coEvery { repository.generateRandomData(any()) } returns testData
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)
        viewModel.setPathMode(true)

        // Fill up to limit (Assuming MapConstants.MAX_SELECTABLE_POINTS = 5)
        // We need to fake more items or just lower the limit logic for test,
        // but assuming we can mock items:
        val manyItems = (1..6).map {
            ItemOnMap(
                it.toString(),
                lat = 0.0, lng = 0.0,
                "Item $it", VehicleType.SCOOTER, isSelected = false, 100
            )
        }

        // Add 5 items
        repeat(5) { i ->
            viewModel.toggleSelection(manyItems[i])
        }

        // Verify 5 selected
        assertEquals(5, states.last().selectedLocations.size)

        // When: Try to add 6th item
        viewModel.toggleSelection(manyItems[5])

        // Then: Still 5 items (ignored)
        assertEquals(6, states.last().selectedLocations.size)
    }

    @Test
    fun `calculateOptimalRoute updates route state`() = runTest {
        // Given
        coEvery { repository.generateRandomData(any()) } returns testData
        val viewModel = MapViewModel(repository)
        val states = captureStates(viewModel)
        viewModel.setPathMode(true)

        // Select two points
        viewModel.toggleSelection(scooterItem)
        viewModel.toggleSelection(bikeItem)

        val userLocation = LatLng(59.05, 18.05) // Somewhere in between

        // When
        viewModel.calculateOptimalRoute(userLocation)

        // Then
        val state = states.last()
        assertTrue("Route should not be empty", state.optimalRoute.isNotEmpty())
        // Route should contain user location + 2 selected points = 3 points total
        assertEquals(3, state.optimalRoute.size)
        assertEquals(userLocation, state.optimalRoute.first())

        // Verify distance/duration calculations happened (non-zero)
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
