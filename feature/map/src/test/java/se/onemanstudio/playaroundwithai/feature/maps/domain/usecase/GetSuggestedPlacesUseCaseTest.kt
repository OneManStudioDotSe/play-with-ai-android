package se.onemanstudio.playaroundwithai.feature.maps.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.feature.maps.domain.model.SuggestedPlace
import se.onemanstudio.playaroundwithai.feature.maps.domain.repository.MapGeminiRepository

class GetSuggestedPlacesUseCaseTest {

    private lateinit var mapGeminiRepository: MapGeminiRepository
    private lateinit var useCase: GetSuggestedPlacesUseCase

    @Before
    fun setUp() {
        mapGeminiRepository = mockk()
        useCase = GetSuggestedPlacesUseCase(mapGeminiRepository)
    }

    @Test
    fun `invoke with valid coordinates delegates to repository`() = runTest {
        val expectedPlaces = listOf(
            SuggestedPlace(
                name = "Stockholm Palace", lat = 59.3268, lng = 18.0717,
                description = "The official residence of the Swedish monarch", category = "Landmark"
            ),
            SuggestedPlace(
                name = "Vasa Museum", lat = 59.3280, lng = 18.0914,
                description = "Maritime museum with a 17th-century warship", category = "Museum"
            )
        )
        coEvery { mapGeminiRepository.getSuggestedPlaces(59.3293, 18.0686) } returns Result.success(expectedPlaces)

        val result = useCase(latitude = 59.3293, longitude = 18.0686)

        assertThat(result.isSuccess).isTrue()
        val places = result.getOrThrow()
        assertThat(places).hasSize(2)
        assertThat(places[0].name).isEqualTo("Stockholm Palace")
        assertThat(places[1].name).isEqualTo("Vasa Museum")
    }

    @Test
    fun `invoke with latitude above 90 returns failure`() = runTest {
        val result = useCase(latitude = 90.1, longitude = 18.0686)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Latitude must be between -90.0 and 90.0")
    }

    @Test
    fun `invoke with latitude below -90 returns failure`() = runTest {
        val result = useCase(latitude = -90.1, longitude = 18.0686)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Latitude must be between -90.0 and 90.0")
    }

    @Test
    fun `invoke with longitude above 180 returns failure`() = runTest {
        val result = useCase(latitude = 59.3293, longitude = 180.1)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Longitude must be between -180.0 and 180.0")
    }

    @Test
    fun `invoke with longitude below -180 returns failure`() = runTest {
        val result = useCase(latitude = 59.3293, longitude = -180.1)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Longitude must be between -180.0 and 180.0")
    }

    @Test
    fun `invoke when repository fails propagates failure`() = runTest {
        val expectedException = RuntimeException("API request failed")
        coEvery { mapGeminiRepository.getSuggestedPlaces(59.3293, 18.0686) } returns Result.failure(expectedException)

        val result = useCase(latitude = 59.3293, longitude = 18.0686)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(expectedException)
    }
}
