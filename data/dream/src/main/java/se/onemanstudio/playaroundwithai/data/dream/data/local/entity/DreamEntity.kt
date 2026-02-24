package se.onemanstudio.playaroundwithai.data.dream.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dreams")
data class DreamEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val interpretation: String,
    val sceneJson: String?,
    val mood: String,
    val timestamp: Long,
)
