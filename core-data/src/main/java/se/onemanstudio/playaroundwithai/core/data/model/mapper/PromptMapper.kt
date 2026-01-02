package se.onemanstudio.playaroundwithai.core.data.model.mapper

import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.data.model.Prompt

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
