package se.onemanstudio.playaroundwithai.feature.dream.views

import kotlin.math.PI

// Animation timing
internal const val ANIMATION_DURATION_MS = 28_000
internal const val SLOW_ANIMATION_DURATION_MS = 42_000

// Background luminance clamping
internal const val MAX_BACKGROUND_LUMINANCE = 0.55f
internal const val LUMINANCE_RED = 0.299f
internal const val LUMINANCE_GREEN = 0.587f
internal const val LUMINANCE_BLUE = 0.114f

// Element sizing & parallax wrap
internal const val ELEMENT_SIZE_RATIO = 0.1f
internal const val PARALLAX_WRAP = 1.5f
internal const val PARALLAX_OFFSET = 0.25f
internal const val TWO_PI = (2.0 * PI).toFloat()
internal const val VERTICAL_DRIFT_AMPLITUDE = 0.008f

// Ground element variety
internal const val GROUND_Y_VARIETY = 0.15f

// Oscillation degrees for tamed animations
internal const val STAR_ROCK_DEGREES = 30f
internal const val DIAMOND_OSCILLATION_DEGREES = 45f
internal const val SPIRAL_OSCILLATION_DEGREES = 60f

// Wave constants
internal const val WAVE_CONTROL_OFFSET = 0.15f
internal const val WAVE_SECOND_CONTROL = 1.5f
internal const val WAVE_SECOND_END = 2f
internal const val WAVE_CREST_HEIGHT = 0.3f

// Tree constants
internal const val TREE_TRUNK_WIDTH_RATIO = 0.15f
internal const val TREE_TRUNK_HEIGHT_RATIO = 0.4f
internal const val TREE_TRUNK_ALPHA = 0.8f
internal const val TREE_CANOPY_WIDTH_RATIO = 0.5f

// Cloud constants
internal const val CLOUD_OVAL_RATIO = 0.35f
internal const val CLOUD_SIDE_SCALE = 1.5f
internal const val CLOUD_SIDE_OFFSET = 0.2f
internal const val CLOUD_SIDE_HEIGHT = 0.8f

// Star constants
internal const val STAR_INNER_RATIO = 0.4f
internal const val STAR_POINTS = 5
internal const val FULL_CIRCLE_DEGREES = 360.0
internal const val STAR_ROTATION_OFFSET = 90.0
internal const val HALF_ROTATION = 0.5f

// Sparkle / Ring constants
internal const val SPARKLE_LINE_RATIO = 0.7f
internal const val RING_STROKE_RATIO = 0.3f

// Misc element constants
internal const val PARTICLE_DRIFT_RATIO = 0.05f
internal const val DEGREES_TO_RADIANS = 180.0

// Crescent constants
internal const val CRESCENT_SWEEP_ANGLE = 300f
internal const val CRESCENT_START_ANGLE = -60f
internal const val CRESCENT_INNER_OFFSET = 0.3f

// Diamond (element) constants
internal const val DIAMOND_ELONGATION = 0.7f
internal const val DIAMOND_WIDTH_RATIO = 0.5f

// Spiral constants
internal const val SPIRAL_POINTS = 60
internal const val SPIRAL_ROTATIONS = 3
internal const val SPIRAL_GROWTH_RATE = 0.15f
internal const val SPIRAL_STROKE_RATIO = 0.06f

// Lotus constants
internal const val LOTUS_PETALS = 6
internal const val LOTUS_PETAL_LENGTH = 0.45f
internal const val LOTUS_PETAL_WIDTH = 0.15f
internal const val LOTUS_PETAL_CURVE = 0.4f
internal const val LOTUS_PETAL_TIP = 0.5f
internal const val LOTUS_CENTER_HEIGHT = 0.45f

// Aurora constants
internal const val AURORA_CURVES = 4
internal const val AURORA_BASE_ALPHA = 0.3f
internal const val AURORA_STROKE_MIN = 0.08f
internal const val AURORA_STROKE_STEP = 0.04f
internal const val AURORA_SPACING = 0.15f
internal const val AURORA_CTRL_X = 0.3f
internal const val AURORA_CTRL_Y = 0.2f
internal const val AURORA_UNDULATION = 0.1f
internal const val AURORA_ALPHA_STEP = 0.05f
internal const val AURORA_ALPHA_MIN = 0.1f
internal const val AURORA_PHASE_STEP = 1.2f

// Crystal constants
internal const val CRYSTAL_SIDES = 6
internal const val CRYSTAL_FACET_ALPHA = 0.4f

// Mountain anchored constants
internal const val MOUNTAIN_PEAK_FACTOR = 1.2f
internal const val MOUNTAIN_BASE_HALF_WIDTH = 1.0f

// Particle constants
internal const val TEARDROP_WIDTH = 0.6f
internal const val TEARDROP_CURVE = 0.8f
internal const val DASH_LENGTH_RATIO = 3f
internal const val DASH_STROKE_RATIO = 0.5f
internal const val STARBURST_DIAGONAL_RATIO = 0.6f
internal const val DIAMOND_MOTE_WIDTH = 0.6f
internal const val PARTICLE_MARGIN = 0.05f
internal const val MAX_PARTICLE_COUNT = 15

// Particle animation constants
internal const val DOT_X_DRIFT = 0.02f
internal const val DOT_Y_LISSAJOUS_FREQ = 0.5f
internal const val DOT_Y_LISSAJOUS_AMP = 0.01f
internal const val SPARKLE_FREQ = 3f
internal const val RING_FREQ = 2f
internal const val DASH_ROTATION_AMP = 15f

// Crystal shimmer
internal const val CRYSTAL_SHIMMER_FREQ = 3f

// Element animation constants
internal const val BREATHE_AMPLITUDE = 0.08f
internal const val CLOUD_BOB_RATIO = 0.05f
internal const val TREE_SWAY_RATIO = 0.03f
internal const val CRESCENT_ROCK_DEGREES = 10f
internal const val CRYSTAL_SHIMMER_BASE = 0.85f
internal const val CRYSTAL_SHIMMER_RANGE = 0.15f
internal const val WAVE_PHASE_RATIO = 0.1f
internal const val SPARKLE_ALPHA_BASE = 0.5f
internal const val RING_BREATHE_AMPLITUDE = 0.3f
internal const val TEARDROP_DOWN_BIAS = 0.03f
internal const val TEARDROP_SWAY_RATIO = 0.02f
internal const val DASH_SPEED_MULTIPLIER = 1.5f
internal const val STARBURST_TWINKLE_SPEED = 5f

// Preview constants
internal const val PREVIEW_HEIGHT = 280
