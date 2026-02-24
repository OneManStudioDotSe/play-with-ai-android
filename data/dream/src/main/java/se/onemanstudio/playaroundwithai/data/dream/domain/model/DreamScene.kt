package se.onemanstudio.playaroundwithai.data.dream.domain.model

data class DreamScene(
    val palette: DreamPalette,
    val layers: List<DreamLayer>,
    val particles: List<DreamParticle>,
)

data class DreamPalette(
    val sky: Long,
    val horizon: Long,
    val accent: Long,
)

data class DreamLayer(
    val depth: Float,
    val elements: List<DreamElement>,
)

data class DreamElement(
    val shape: ElementShape,
    val x: Float,
    val y: Float,
    val scale: Float,
    val color: Long,
    val alpha: Float,
)

data class DreamParticle(
    val shape: ParticleShape,
    val count: Int,
    val color: Long,
    val speed: Float,
    val size: Float,
)

enum class ElementShape { CIRCLE, TRIANGLE, MOUNTAIN, WAVE, TREE, CLOUD, STAR }

enum class ParticleShape { DOT, SPARKLE, RING }
