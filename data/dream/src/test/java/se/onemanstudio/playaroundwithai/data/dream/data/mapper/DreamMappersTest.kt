package se.onemanstudio.playaroundwithai.data.dream.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import se.onemanstudio.playaroundwithai.data.dream.data.local.entity.DreamEntity
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import java.time.Instant

class DreamMappersTest {

    @Test
    fun `entity toDomain maps correctly`() {
        val timestamp = 1700000000000L
        val entity = DreamEntity(
            id = 1,
            description = "Flying dream",
            interpretation = "Freedom symbolism",
            sceneJson = null,
            mood = "JOYFUL",
            timestamp = timestamp,
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo(1L)
        assertThat(domain.description).isEqualTo("Flying dream")
        assertThat(domain.interpretation).isEqualTo("Freedom symbolism")
        assertThat(domain.scene).isNull()
        assertThat(domain.mood).isEqualTo(DreamMood.JOYFUL)
        assertThat(domain.timestamp).isEqualTo(Instant.ofEpochMilli(timestamp))
    }

    @Test
    fun `domain toEntity maps correctly`() {
        val timestamp = Instant.ofEpochMilli(1700000000000L)
        val domain = Dream(
            id = 2,
            description = "Ocean dream",
            interpretation = "Emotional depth",
            scene = null,
            mood = DreamMood.PEACEFUL,
            timestamp = timestamp,
        )

        val entity = domain.toEntity()

        assertThat(entity.id).isEqualTo(2L)
        assertThat(entity.description).isEqualTo("Ocean dream")
        assertThat(entity.interpretation).isEqualTo("Emotional depth")
        assertThat(entity.sceneJson).isNull()
        assertThat(entity.mood).isEqualTo("PEACEFUL")
        assertThat(entity.timestamp).isEqualTo(1700000000000L)
    }

    @Test
    fun `entity with unknown mood defaults to MYSTERIOUS`() {
        val entity = DreamEntity(
            id = 3,
            description = "Test",
            interpretation = "Test",
            sceneJson = null,
            mood = "UNKNOWN_MOOD",
            timestamp = 0L,
        )

        val domain = entity.toDomain()

        assertThat(domain.mood).isEqualTo(DreamMood.MYSTERIOUS)
    }

    @Test
    fun `roundtrip preserves data`() {
        val original = Dream(
            id = 5,
            description = "Chase dream",
            interpretation = "Avoidance pattern",
            scene = null,
            mood = DreamMood.ANXIOUS,
            timestamp = Instant.ofEpochMilli(1700000000000L),
        )

        val roundTripped = original.toEntity().toDomain()

        assertThat(roundTripped.id).isEqualTo(original.id)
        assertThat(roundTripped.description).isEqualTo(original.description)
        assertThat(roundTripped.interpretation).isEqualTo(original.interpretation)
        assertThat(roundTripped.mood).isEqualTo(original.mood)
        assertThat(roundTripped.timestamp).isEqualTo(original.timestamp)
    }
}
