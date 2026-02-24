package se.onemanstudio.playaroundwithai.data.dream.data.mapper

import com.google.gson.Gson
import se.onemanstudio.playaroundwithai.data.dream.data.local.entity.DreamEntity
import se.onemanstudio.playaroundwithai.data.dream.domain.model.Dream
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamMood
import se.onemanstudio.playaroundwithai.data.dream.domain.model.DreamScene
import java.time.Instant

private val gson = Gson()

fun DreamEntity.toDomain(): Dream {
    return Dream(
        id = this.id,
        description = this.description,
        interpretation = this.interpretation,
        scene = this.sceneJson?.let { runCatching { gson.fromJson(it, DreamScene::class.java) }.getOrNull() },
        mood = runCatching { DreamMood.valueOf(this.mood) }.getOrDefault(DreamMood.MYSTERIOUS),
        timestamp = Instant.ofEpochMilli(this.timestamp),
    )
}

fun Dream.toEntity(): DreamEntity {
    return DreamEntity(
        id = this.id,
        description = this.description,
        interpretation = this.interpretation,
        sceneJson = this.scene?.let { gson.toJson(it) },
        mood = this.mood.name,
        timestamp = this.timestamp.toEpochMilli(),
    )
}
