package se.onemanstudio.playaroundwithai.data.dream.domain.usecase

import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.repository.DreamRepository

class SaveDreamUseCaseTest {

    private lateinit var repository: DreamRepository
    private lateinit var useCase: SaveDreamUseCase

    @Before
    fun setUp() {
        repository = mockk()
        useCase = SaveDreamUseCase(repository)
    }

    @Test
    fun `invoke with valid dream saves and returns id`() = runTest {
        val dream = Dream(description = "Flying over mountains")
        coEvery { repository.saveDream(dream) } returns 1L

        val result = useCase(dream)

        assertThat(result).isEqualTo(1L)
        coVerify(exactly = 1) { repository.saveDream(dream) }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke with blank description throws`() = runTest {
        val dream = Dream(description = "   ")

        useCase(dream)
    }
}
