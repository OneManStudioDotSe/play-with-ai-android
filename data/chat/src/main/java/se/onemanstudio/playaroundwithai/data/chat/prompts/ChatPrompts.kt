package se.onemanstudio.playaroundwithai.data.chat.prompts

internal object ChatPrompts {

    const val CONVERSATION_STARTERS_PROMPT = """
        Generate 3 short, menacing conversation starters that a person might ask their AI ruler.
        Keep them under 7 words each.
        Format the output strictly as: "Topic 1|Topic 2|Topic 3"
        Do not add any numbering, bullet points, or extra text.
    """

    val ANALYSIS_INSTRUCTIONS: Map<String, String> = mapOf(
        "LOCATION" to "Identify the location depicted. Include a description of the location",

        "RECIPE" to "Analyze the food or dish depicted and explain how to create it.",

        "MOVIE" to "Identify this movie/show. Give the title, year and a description of it.",

        "SONG" to "Identify the song. Give the title, artist and description of the album that contains it.",

        "PERSONALITY" to "Identify this high-value personality. Give the name, date of birth and significance.",

        "PRODUCT" to "Identify the product that is shown. Provide the name, brand and most common use.",

        "TREND" to "Analyze the trend that is shown in the image.",
    )
}
