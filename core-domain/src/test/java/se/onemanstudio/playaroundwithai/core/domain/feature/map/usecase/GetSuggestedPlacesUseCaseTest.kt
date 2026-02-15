package se.onemanstudio.playaroundwithai.core.domain.feature.map.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.GeminiModel
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.repository.GeminiRepository
import se.onemanstudio.playaroundwithai.core.domain.feature.map.model.SuggestedPlace

class GetSuggestedPlacesUseCaseTest {

    private lateinit var geminiRepository: GeminiRepository
    private lateinit var useCase: GetSuggestedPlacesUseCase

    @Before
    fun setUp() {
        geminiRepository = mockk()
        useCase = GetSuggestedPlacesUseCase(geminiRepository)
    }

    @Test
    fun `invoke with valid coordinates delegates to repository`() = runTest {
        // GIVEN: Repository returns a list of suggested places
        val expectedPlaces = listOf(
            SuggestedPlace(
                name = "Stockholm Palace",
                lat = 59.3268,
                lng = 18.0717,
                description = "The official residence of the Swedish monarch",
                category = "Landmark"
            ),
            SuggestedPlace(
                name = "Vasa Museum",
                lat = 59.3280,
                lng = 18.0914,
                description = "Maritime museum with a 17th-century warship",
                category = "Museum"
            )
        )
        coEvery { geminiRepository.getSuggestedPlaces(59.3293, 18.0686, GeminiModel.FLASH_PREVIEW) } returns Result.success(expectedPlaces)

        // WHEN
        val result = useCase(latitude = 59.3293, longitude = 18.0686)

        // THEN
        assertThat(result.isSuccess).isTrue()
        val places = result.getOrThrow()
        assertThat(places).hasSize(2)
        assertThat(places[0].name).isEqualTo("Stockholm Palace")
        assertThat(places[1].name).isEqualTo("Vasa Museum")
    }

    @Test
    fun `invoke with latitude above 90 returns failure`() = runTest {
        // GIVEN: Latitude exceeds maximum allowed value

        // WHEN
        val result = useCase(latitude = 90.1, longitude = 18.0686)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Latitude must be between -90.0 and 90.0")
    }

    @Test
    fun `invoke with latitude below -90 returns failure`() = runTest {
        // GIVEN: Latitude is below minimum allowed value

        // WHEN
        val result = useCase(latitude = -90.1, longitude = 18.0686)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Latitude must be between -90.0 and 90.0")
    }

    @Test
    fun `invoke with longitude above 180 returns failure`() = runTest {
        // GIVEN: Longitude exceeds maximum allowed value

        // WHEN
        val result = useCase(latitude = 59.3293, longitude = 180.1)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Longitude must be between -180.0 and 180.0")
    }

    @Test
    fun `invoke with longitude below -180 returns failure`() = runTest {
        // GIVEN: Longitude is below minimum allowed value

        // WHEN
        val result = useCase(latitude = 59.3293, longitude = -180.1)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).contains("Longitude must be between -180.0 and 180.0")
    }

    @Test
    fun `invoke when repository fails propagates failure`() = runTest {
        // GIVEN: Repository returns a failure
        val expectedException = RuntimeException("API request failed")
        coEvery { geminiRepository.getSuggestedPlaces(59.3293, 18.0686, GeminiModel.FLASH_PREVIEW) } returns Result.failure(expectedException)

        // WHEN
        val result = useCase(latitude = 59.3293, longitude = 18.0686)

        // THEN
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(expectedException)
    }
}
