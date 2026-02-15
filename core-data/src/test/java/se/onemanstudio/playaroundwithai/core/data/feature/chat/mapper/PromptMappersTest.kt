package se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus
import java.time.Instant

class PromptMappersTest {

    @Test
    fun `toDomain maps entity to domain model correctly`() {
        // GIVEN: A PromptEntity with known values
        val entity = PromptEntity(
            id = 42,
            text = "Hello AI",
            timestamp = 1_700_000_000_000L,
            syncStatus = SyncStatus.Synced
        )

        // WHEN
        val domain = entity.toDomain()

        // THEN: Int id is converted to Long, Long timestamp to Date
        assertThat(domain.id).isEqualTo(42L)
        assertThat(domain.text).isEqualTo("Hello AI")
        assertThat(domain.timestamp).isEqualTo(Instant.ofEpochMilli(1_700_000_000_000L))
        assertThat(domain.syncStatus).isEqualTo(SyncStatus.Synced)
    }

    @Test
    fun `toEntity maps domain model to entity correctly`() {
        // GIVEN: A domain Prompt
        val prompt = Prompt(
            id = 7L,
            text = "World domination plan",
            timestamp = Instant.ofEpochMilli(1_700_000_060_000L),
            syncStatus = SyncStatus.Pending
        )

        // WHEN
        val entity = prompt.toEntity()

        // THEN: Long id is converted to Int, Date timestamp to Long
        assertThat(entity.id).isEqualTo(7)
        assertThat(entity.text).isEqualTo("World domination plan")
        assertThat(entity.timestamp).isEqualTo(1_700_000_060_000L)
        assertThat(entity.syncStatus).isEqualTo(SyncStatus.Pending)
    }

    @Test
    fun `toDomain and toEntity are inverse operations`() {
        // GIVEN: A domain Prompt
        val original = Prompt(
            id = 10L,
            text = "Round trip test",
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            syncStatus = SyncStatus.Pending
        )

        // WHEN: We convert to entity and back
        val roundTripped = original.toEntity().toDomain()

        // THEN: The result matches the original
        assertThat(roundTripped.id).isEqualTo(original.id)
        assertThat(roundTripped.text).isEqualTo(original.text)
        assertThat(roundTripped.timestamp).isEqualTo(original.timestamp)
        assertThat(roundTripped.syncStatus).isEqualTo(original.syncStatus)
    }

    @Test
    fun `toDomain preserves all SyncStatus values`() {
        // GIVEN/WHEN/THEN: Each SyncStatus maps correctly
        val statuses = listOf(SyncStatus.Pending, SyncStatus.Synced, SyncStatus.Failed)

        statuses.forEach { status ->
            val entity = PromptEntity(id = 1, text = "test", timestamp = 0L, syncStatus = status)
            assertThat(entity.toDomain().syncStatus).isEqualTo(status)
        }
    }
}
