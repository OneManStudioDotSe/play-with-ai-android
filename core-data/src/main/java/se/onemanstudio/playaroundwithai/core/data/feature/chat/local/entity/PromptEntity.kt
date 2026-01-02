package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import se.onemanstudio.playaroundwithai.core.data.model.Prompt

@Entity(tableName = "prompt_history")
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Maps a [PromptEntity] from the data layer to a [Prompt] in the domain layer.
 */
fun PromptEntity.toDomain(): Prompt {
    return Prompt(
        id = this.id,
        text = this.text,
        timestamp = this.timestamp
    )
}
