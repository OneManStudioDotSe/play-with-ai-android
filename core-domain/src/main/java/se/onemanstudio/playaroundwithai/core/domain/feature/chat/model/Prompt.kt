package se.onemanstudio.playaroundwithai.core.domain.feature.chat.model

import java.util.Date

data class Prompt(
    val id: Long = 0,
    val text: String,
    val timestamp: Date = Date(),
    val syncStatus: SyncStatus = SyncStatus.Pending
)
