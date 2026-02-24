package se.onemanstudio.playaroundwithai.data.chat.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import se.onemanstudio.playaroundwithai.data.chat.domain.model.SyncStatus

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
