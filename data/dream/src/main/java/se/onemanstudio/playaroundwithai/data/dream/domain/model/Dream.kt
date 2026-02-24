package se.onemanstudio.playaroundwithai.data.dream.domain.model

import java.time.Instant

data class Dream(
    val id: Long = 0,
    val description: String,
    val interpretation: String = "",
    val scene: DreamScene? = null,
    val mood: DreamMood = DreamMood.MYSTERIOUS,
    val timestamp: Instant = Instant.now(),
)
