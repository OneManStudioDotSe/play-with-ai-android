package se.onemanstudio.playaroundwithai.core.data.feature.chat.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import se.onemanstudio.playaroundwithai.core.domain.feature.chat.model.SyncStatus

@Entity(
    tableName = "prompt_history",
    indices = [
        Index("syncStatus"),
        Index("firestoreDocId")
    ]
)
data class PromptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus,
    val firestoreDocId: String? = null
)
