package se.onemanstudio.playaroundwithai.feature.chat.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import se.onemanstudio.playaroundwithai.feature.chat.data.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.Prompt
import se.onemanstudio.playaroundwithai.feature.chat.domain.model.SyncStatus
import java.time.Instant

class PromptMappersTest {

    @Test
    fun `toDomain maps entity to domain model correctly`() {
        val entity = PromptEntity(
            id = 42,
            text = "Hello AI",
            timestamp = 1_700_000_000_000L,
            syncStatus = SyncStatus.Synced
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(42L)
        assertThat(domain.text).isEqualTo("Hello AI")
        assertThat(domain.timestamp).isEqualTo(Instant.ofEpochMilli(1_700_000_000_000L))
        assertThat(domain.syncStatus).isEqualTo(SyncStatus.Synced)
    }

    @Test
    fun `toEntity maps domain model to entity correctly`() {
        val prompt = Prompt(
            id = 7L,
            text = "World domination plan",
            timestamp = Instant.ofEpochMilli(1_700_000_060_000L),
            syncStatus = SyncStatus.Pending
        )

        val entity = prompt.toEntity()

        assertThat(entity.id).isEqualTo(7)
        assertThat(entity.text).isEqualTo("World domination plan")
        assertThat(entity.timestamp).isEqualTo(1_700_000_060_000L)
        assertThat(entity.syncStatus).isEqualTo(SyncStatus.Pending)
    }

    @Test
    fun `toDomain and toEntity are inverse operations`() {
        val original = Prompt(
            id = 10L,
            text = "Round trip test",
            timestamp = Instant.ofEpochMilli(1_700_000_000_000L),
            syncStatus = SyncStatus.Pending
        )

        val roundTripped = original.toEntity().toDomain()

        assertThat(roundTripped.id).isEqualTo(original.id)
        assertThat(roundTripped.text).isEqualTo(original.text)
        assertThat(roundTripped.timestamp).isEqualTo(original.timestamp)
        assertThat(roundTripped.syncStatus).isEqualTo(original.syncStatus)
    }

    @Test
    fun `toDomain preserves all SyncStatus values`() {
        val statuses = listOf(SyncStatus.Pending, SyncStatus.Synced, SyncStatus.Failed)

        statuses.forEach { status ->
            val entity = PromptEntity(id = 1, text = "test", timestamp = 0L, syncStatus = status)
            assertThat(entity.toDomain().syncStatus).isEqualTo(status)
        }
    }
}
