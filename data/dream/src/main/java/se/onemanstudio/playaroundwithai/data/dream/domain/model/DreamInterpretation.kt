package se.onemanstudio.playaroundwithai.data.dream.domain.model

data class DreamInterpretation(
    val textAnalysis: String,
    val scene: DreamScene,
    val mood: DreamMood,
)
