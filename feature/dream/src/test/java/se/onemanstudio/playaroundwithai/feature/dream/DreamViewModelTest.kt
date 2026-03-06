package se.onemanstudio.playaroundwithai.feature.dream

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.config.model.ApiKeyAvailability
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamImage
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamInterpretation
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamPalette
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamParticle
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import se.onemanstudio.playaroundwithai.data.dream.domain.model.ParticleShape
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamGeminiRepository
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.GenerateDreamImageUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.GetDreamHistoryUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.InterpretDreamUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.SaveDreamImageUseCase
import se.onemanstudio.playaroundwithai.data.dream.domain.usecase.SaveDreamUseCase
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamError
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamImageState
import se.onemanstudio.playaroundwithai.feature.dream.states.DreamUiState
import se.onemanstudio.playaroundwithai.core.testing.MainCoroutineRule
import java.io.IOException
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DreamViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainCoroutineRule(UnconfinedTestDispatcher())

    @Test
    fun `Initial state is Initial`() = runTest {
        val viewModel = createViewModel()
        val states = mutableListOf<DreamUiState>()

        viewModel.screenState
            .map { it.dreamState }
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        advanceUntilIdle()

        assertEquals(1, states.size)
        assertEquals(DreamUiState.Initial, states[0])
    }

    @Test
    fun `interpretDream success updates state to Result`() = runTest {
        val interpretation = createTestInterpretation()
        val viewModel = createViewModel(interpretResult = Result.success(interpretation))
        val states = mutableListOf<DreamUiState>()

        viewModel.screenState
            .map { it.dreamState }
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.interpretDream("I was flying")
        advanceUntilIdle()

        assertEquals(DreamUiState.Initial, states[0])
        assertEquals(DreamUiState.Interpreting, states[1])
        assert(states[2] is DreamUiState.Result)
        assertEquals(interpretation.textAnalysis, (states[2] as DreamUiState.Result).interpretation)
    }

    @Test
    fun `interpretDream success sets image state to Generated with artist name`() = runTest {
        val interpretation = createTestInterpretation()
        val dreamImage = DreamImage(imageBase64 = "AAAA", mimeType = "image/png", artistName = "Salvador Dali")
        val viewModel = createViewModel(
            interpretResult = Result.success(interpretation),
            imageResult = Result.success(dreamImage),
        )

        viewModel.interpretDream("I was flying")
        advanceUntilIdle()

        val imageState = viewModel.screenState.value.imageState
        assert(imageState is DreamImageState.Generated)
        assertEquals("Salvador Dali", (imageState as DreamImageState.Generated).artistName)
    }

    @Test
    fun `interpretDream failure updates state to Error`() = runTest {
        val exception = RuntimeException("Test error")
        val viewModel = createViewModel(interpretResult = Result.failure(exception))
        val states = mutableListOf<DreamUiState>()

        viewModel.screenState
            .map { it.dreamState }
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.interpretDream("A strange dream")
        advanceUntilIdle()

        assertEquals(DreamUiState.Initial, states[0])
        assertEquals(DreamUiState.Interpreting, states[1])
        assert(states[2] is DreamUiState.Error)
        assertEquals(DreamError.Unknown(exception.message), (states[2] as DreamUiState.Error).error)
    }

    @Test
    fun `interpretDream network failure updates state to NetworkMissing`() = runTest {
        val exception = IOException("No internet")
        val viewModel = createViewModel(interpretResult = Result.failure(exception))
        val states = mutableListOf<DreamUiState>()

        viewModel.screenState
            .map { it.dreamState }
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        viewModel.interpretDream("A dream")
        advanceUntilIdle()

        assertEquals(DreamError.NetworkMissing, (states[2] as DreamUiState.Error).error)
    }

    @Test
    fun `missing Gemini API key sets ApiKeyMissing error on init`() = runTest {
        val viewModel = createViewModel(
            apiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = false, isMapsKeyAvailable = true)
        )
        val states = mutableListOf<DreamUiState>()

        viewModel.screenState
            .map { it.dreamState }
            .onEach { states.add(it) }
            .launchIn(CoroutineScope(UnconfinedTestDispatcher(testScheduler)))
        advanceUntilIdle()

        assertEquals(DreamUiState.Error(DreamError.ApiKeyMissing), states.last())
    }

    @Test
    fun `clearResult resets to Initial and Idle`() = runTest {
        val interpretation = createTestInterpretation()
        val viewModel = createViewModel(interpretResult = Result.success(interpretation))

        viewModel.interpretDream("Dream text")
        advanceUntilIdle()

        viewModel.clearResult()
        advanceUntilIdle()

        assertEquals(DreamUiState.Initial, viewModel.screenState.value.dreamState)
        assertEquals(DreamImageState.Idle, viewModel.screenState.value.imageState)
    }

    @Test
    fun `interpretDream success auto-saves dream`() = runTest {
        val interpretation = createTestInterpretation()
        val dreamRepository = mockk<DreamRepository> {
            coEvery { saveDream(any()) } returns 1L
            coEvery { saveDreamImage(any(), any(), any(), any()) } returns "/path/image.png"
            every { getDreamHistory() } returns flowOf(emptyList())
        }
        val viewModel = createViewModel(
            interpretResult = Result.success(interpretation),
            dreamRepository = dreamRepository,
        )

        viewModel.interpretDream("Dream text")
        advanceUntilIdle()

        coVerify { dreamRepository.saveDream(match { it.description == "Dream text" }) }
    }

    @Test
    fun `restoreDream with imagePath sets Generated state with path`() = runTest {
        val viewModel = createViewModel()
        val scene = createTestInterpretation().scene

        viewModel.restoreDream(
            Dream(
                id = 42,
                description = "A dream",
                interpretation = "Meaning",
                scene = scene,
                mood = DreamMood.MYSTERIOUS,
                imagePath = "/some/path.png",
                artistName = "Van Gogh",
            )
        )
        advanceUntilIdle()

        val imageState = viewModel.screenState.value.imageState
        assert(imageState is DreamImageState.Generated)
        assertEquals("/some/path.png", (imageState as DreamImageState.Generated).imagePath)
        assertEquals("Van Gogh", imageState.artistName)
    }

    @Test
    fun `restoreDream without imagePath sets Idle image state`() = runTest {
        val viewModel = createViewModel()
        val scene = createTestInterpretation().scene

        viewModel.restoreDream(
            Dream(
                id = 42,
                description = "A dream",
                interpretation = "Meaning",
                scene = scene,
                mood = DreamMood.MYSTERIOUS,
            )
        )
        advanceUntilIdle()

        val imageState = viewModel.screenState.value.imageState
        assertEquals(DreamImageState.Idle, imageState)
    }

    @Test
    fun `image generation failure does not affect interpretation result`() = runTest {
        val interpretation = createTestInterpretation()
        val viewModel = createViewModel(
            interpretResult = Result.success(interpretation),
            imageResult = Result.failure(IOException("Image gen failed")),
        )

        viewModel.interpretDream("A dream")
        advanceUntilIdle()

        val dreamState = viewModel.screenState.value.dreamState
        assert(dreamState is DreamUiState.Result)
        assertEquals(interpretation.textAnalysis, (dreamState as DreamUiState.Result).interpretation)

        val imageState = viewModel.screenState.value.imageState
        assert(imageState is DreamImageState.Error)
    }

    @Test
    fun `clearResult resets both dream and image states`() = runTest {
        val interpretation = createTestInterpretation()
        val dreamImage = DreamImage(imageBase64 = "AAAA", mimeType = "image/png", artistName = "Monet")
        val viewModel = createViewModel(
            interpretResult = Result.success(interpretation),
            imageResult = Result.success(dreamImage),
        )

        viewModel.interpretDream("Dream text")
        advanceUntilIdle()

        assert(viewModel.screenState.value.dreamState is DreamUiState.Result)
        assert(viewModel.screenState.value.imageState is DreamImageState.Generated)

        viewModel.clearResult()
        advanceUntilIdle()

        assertEquals(DreamUiState.Initial, viewModel.screenState.value.dreamState)
        assertEquals(DreamImageState.Idle, viewModel.screenState.value.imageState)
        assertEquals("", viewModel.screenState.value.currentDescription)
    }

    private fun createViewModel(
        interpretResult: Result<DreamInterpretation>? = null,
        imageResult: Result<DreamImage>? = null,
        dreamHistory: List<Dream> = emptyList(),
        dreamRepository: DreamRepository? = null,
        apiKeyAvailability: ApiKeyAvailability = ApiKeyAvailability(isGeminiKeyAvailable = true, isMapsKeyAvailable = true),
    ): DreamViewModel {
        val geminiRepository = mockk<DreamGeminiRepository> {
            interpretResult?.let { coEvery { interpretDream(any()) } returns it }
            coEvery { generateDreamImage(any()) } returns (imageResult ?: Result.failure(RuntimeException("Not configured")))
        }

        val effectiveDreamRepository = dreamRepository ?: mockk<DreamRepository> {
            coEvery { saveDream(any()) } returns 1L
            coEvery { saveDreamImage(any(), any(), any(), any()) } returns "/path/image.png"
            coEvery { deleteDream(any()) } returns Unit
            every { getDreamHistory() } returns flowOf(dreamHistory)
        }

        return DreamViewModel(
            InterpretDreamUseCase(geminiRepository),
            SaveDreamUseCase(effectiveDreamRepository),
            GetDreamHistoryUseCase(effectiveDreamRepository),
            GenerateDreamImageUseCase(geminiRepository),
            SaveDreamImageUseCase(effectiveDreamRepository),
            apiKeyAvailability,
        )
    }

    private fun createTestInterpretation() = DreamInterpretation(
        textAnalysis = "This dream represents freedom.",
        scene = DreamScene(
            palette = DreamPalette(sky = 0xFF1A1A2E, horizon = 0xFF16213E, accent = 0xFF0F3460),
            layers = emptyList(),
            particles = listOf(DreamParticle(shape = ParticleShape.DOT, count = 10, color = 0xFFFFFFFF, speed = 1f, size = 4f)),
        ),
        mood = DreamMood.MYSTERIOUS,
    )
}
