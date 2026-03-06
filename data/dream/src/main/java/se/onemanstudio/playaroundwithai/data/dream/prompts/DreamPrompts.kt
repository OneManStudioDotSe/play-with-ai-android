package se.onemanstudio.playaroundwithai.data.dream.prompts

internal object DreamPrompts {

    private const val INTERPRETATION_PROMPT = """
You are a dream interpreter and visual artist. Given the user's dream description below, return a JSON object with exactly this structure:
{
  "interpretation": "A 2-3 sentence analysis of symbolism, emotional meaning, and themes",
  "mood": "one of: JOYFUL, MYSTERIOUS, ANXIOUS, PEACEFUL, DARK, SURREAL, NOSTALGIC, HOPEFUL, MELANCHOLIC, ADVENTUROUS, ROMANTIC",
  "scene": {
    "palette": { "sky": <ARGB long>, "horizon": <ARGB long>, "accent": <ARGB long> },
    "layers": [
      {
        "depth": <0.0-1.0>,
        "elements": [{ "shape": "<CIRCLE|TRIANGLE|MOUNTAIN|WAVE|TREE|CLOUD|STAR|CRESCENT|DIAMOND|SPIRAL|LOTUS|AURORA|CRYSTAL>", "x": <0.0-1.0>, "y": <0.0-1.0>, "scale": <0.5-3.0>, "color": <ARGB long>, "alpha": <0.0-1.0> }]
      }
    ],
    "particles": [{ "shape": "<DOT|SPARKLE|RING|TEARDROP|DIAMOND_MOTE|DASH|STARBURST>", "count": <5-30>, "color": <ARGB long>, "speed": <0.5-2.0>, "size": <2.0-8.0> }]
  }
}
Generate 3-5 layers with 2-4 elements each. Use colors that match the dream mood. ARGB long values should be like 4278190335 (0xFF0000FF for blue).
Shape guidance:
- Nature: TREE, MOUNTAIN, LOTUS, AURORA, WAVE, CLOUD. Particles: TEARDROP, DOT
- Night/space: STAR, CRESCENT, CRYSTAL, CIRCLE. Particles: SPARKLE, STARBURST, DIAMOND_MOTE
- Abstract/surreal: SPIRAL, DIAMOND, AURORA, WAVE. Particles: RING, DASH, DIAMOND_MOTE
- Water/ocean: WAVE, CIRCLE, CRESCENT. Particles: TEARDROP, DOT, RING
Vertical placement guide (y values):
- Sky elements (STAR, CRESCENT, AURORA, CIRCLE as sun/moon): y between 0.05-0.3
- Upper elements (CLOUD, DIAMOND, SPIRAL, CRYSTAL): y between 0.15-0.4
- Mid elements (MOUNTAIN, TRIANGLE): y between 0.4-0.7, they grow downward from their peak
- Ground elements (TREE, WAVE, LOTUS): y between 0.7-0.9
Use diverse shapes across layers. Mix 3-5 different element shapes and 2-3 particle types per scene.
Keep particle count between 5-15 per type to avoid cluttering the scene.
Return ONLY valid JSON, no markdown, no backticks, no extra text.

Dream: "%s"
    """

    private const val IMAGE_PROMPT = """
Create an image of a painting inspired by the following dream. Paint it in the distinctive style of a famous artist whose work resonates with the dream's mood and imagery. Choose from artists like Dalí, Monet, Van Gogh, Klimt, Kahlo, Hokusai, Magritte, Munch, Rothko, or any other renowned painter.

The painting should be vivid, atmospheric, and capture the emotional essence of the dream.

Along with the generated image, include a single line of text with the artist's name in this exact format:
Artist: <Full Name>

Dream: "%s"
    """

    fun interpretationPrompt(description: String): String =
        INTERPRETATION_PROMPT.trimIndent().format(description)

    fun imagePrompt(description: String): String = IMAGE_PROMPT.trimIndent().format(description)
}
