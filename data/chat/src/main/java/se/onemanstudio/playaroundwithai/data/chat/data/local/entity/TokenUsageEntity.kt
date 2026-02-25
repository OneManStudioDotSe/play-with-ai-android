package se.onemanstudio.playaroundwithai.data.chat.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "token_usage",
    indices = [
        Index("feature"),
        Index("dateMillis"),
    ]
)
data class TokenUsageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val feature: String,
    val promptTokens: Int,
    val candidateTokens: Int,
    val totalTokens: Int,
    val timestamp: Long,
    val dateMillis: Long,
)
