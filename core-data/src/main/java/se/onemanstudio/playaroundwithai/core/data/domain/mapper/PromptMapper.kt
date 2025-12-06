package se.onemanstudio.playaroundwithai.core.data.domain.mapper

import se.onemanstudio.playaroundwithai.core.data.domain.model.Prompt
import se.onemanstudio.playaroundwithai.core.data.local.PromptEntity

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
