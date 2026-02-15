package se.onemanstudio.playaroundwithai.core.data.feature.chat.mapper

import se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity.PromptEntity
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.Prompt
import java.time.Instant

fun PromptEntity.toDomain(): Prompt {
    return Prompt(
        id = this.id.toLong(),
        text = this.text,
        timestamp = Instant.ofEpochMilli(this.timestamp),
        syncStatus = this.syncStatus,
        firestoreDocId = this.firestoreDocId
    )
}

fun Prompt.toEntity(): PromptEntity {
    return PromptEntity(
        id = this.id.toInt(),
        text = this.text,
        timestamp = this.timestamp.toEpochMilli(),
        syncStatus = this.syncStatus,
        firestoreDocId = this.firestoreDocId
    )
}
