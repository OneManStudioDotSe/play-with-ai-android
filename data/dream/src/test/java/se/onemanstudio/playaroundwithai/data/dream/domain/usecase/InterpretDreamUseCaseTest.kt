package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamInterpretation
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository

class InterpretDreamUseCaseTest {

    private lateinit var repository: DreamGeminiRepository
    private lateinit var useCase: InterpretDreamUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = InterpretDreamUseCase(repository)
    }

    @Test
    fun `invoke with valid description delegates to repository and returns success`() = runTest {
        val description = "I was flying over a purple ocean"
        val expectedResult = createTestInterpretation()
        coEvery { repository.interpretDream(description) } returns Result.success(expectedResult)

        val result = useCase(description)

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).isEqualTo(expectedResult)
        coVerify(exactly = 1) { repository.interpretDream(description) }
    }

    @Test
    fun `invoke with blank description returns failure`() = runTest {
        val result = useCase("   ")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message).isEqualTo("Dream description cannot be blank")
        coVerify(exactly = 0) { repository.interpretDream(any()) }
    }

    @Test
    fun `invoke with description exceeding max length returns failure`() = runTest {
        val longDescription = "a".repeat(MAX_DREAM_LENGTH + 1)

        val result = useCase(longDescription)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(result.exceptionOrNull()?.message)
            .isEqualTo("Dream description exceeds maximum length of $MAX_DREAM_LENGTH characters")
        coVerify(exactly = 0) { repository.interpretDream(any()) }
    }

    @Test
    fun `invoke when repository fails propagates failure`() = runTest {
        val description = "A strange dream"
        val expectedException = RuntimeException("API error")
        coEvery { repository.interpretDream(description) } returns Result.failure(expectedException)

        val result = useCase(description)

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(expectedException)
    }

    private fun createTestInterpretation() = DreamInterpretation(
        textAnalysis = "This dream symbolizes freedom and exploration.",
        scene = DreamScene(
            palette = DreamPalette(sky = 0xFF1A1A2E, horizon = 0xFF16213E, accent = 0xFF0F3460),
            layers = emptyList(),
            particles = listOf(DreamParticle(shape = ParticleShape.DOT, count = 10, color = 0xFFFFFFFF, speed = 1f, size = 4f)),
        ),
        mood = DreamMood.MYSTERIOUS,
    )
}
