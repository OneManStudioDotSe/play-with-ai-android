package se.onemanstudio.playaroundwithai.core.network.utils

object JsonExtractor {
    private val CODE_FENCE = Regex("""```(?:json)?\s*([\s\S]*?)```""")

    fun extract(text: String): String =
        CODE_FENCE.find(text)?.groupValues?.getOrNull(1)?.trim() ?: text.trim()
}
