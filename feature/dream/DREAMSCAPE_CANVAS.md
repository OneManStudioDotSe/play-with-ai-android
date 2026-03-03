# DreamscapeCanvas — Element Slots, Shapes & Colors

This document describes how `DreamscapeCanvas` positions, renders, and colors every visual element in the dream scene.

## Overview

A `DreamScene` is composed of:

- **Palette** — 3 ARGB colors (`sky`, `horizon`, `accent`) that define the background gradient
- **Layers** — 3–5 parallax layers, each containing 2–4 `DreamElement` instances
- **Particles** — 1–3 ambient particle emitters floating across the canvas

The AI (Gemini) generates the entire scene as JSON. The canvas **remaps** each element's Y position into a shape-specific vertical zone, so the AI controls relative placement within the slot but cannot break out of it.

---

## Vertical Slots (Y-Zones)

Each `ElementShape` is locked to a vertical band via `verticalZoneFor()` in `DreamscapeCanvas.kt`. The AI's raw `y` (0.0–1.0) is linearly interpolated within that band.

### Sky Slot

| Shape     | Y Range     | Purpose              |
|-----------|-------------|----------------------|
| `STAR`    | 0.05 – 0.25 | Celestial pinpoints  |
| `CRESCENT`| 0.08 – 0.30 | Moon-like arcs       |
| `AURORA`  | 0.10 – 0.35 | Northern-lights bands|
| `CIRCLE`  | 0.10 – 0.40 | Sun / moon / orb     |

### Upper Slot

| Shape     | Y Range     | Purpose               |
|-----------|-------------|------------------------|
| `CLOUD`   | 0.12 – 0.35 | Atmospheric puffs      |
| `DIAMOND` | 0.15 – 0.45 | Floating gems          |
| `SPIRAL`  | 0.20 – 0.50 | Abstract/mystical accent|
| `CRYSTAL` | 0.20 – 0.50 | Mystical accent        |

### Mid Slot

| Shape      | Y Range     | Purpose         |
|------------|-------------|-----------------|
| `TRIANGLE` | 0.40 – 0.65 | Abstract peaks  |
| `MOUNTAIN` | 0.45 – 0.70 | Terrain anchors |

### Ground Slot

| Shape   | Y Range     | Purpose           |
|---------|-------------|-------------------|
| `TREE`  | 0.65 – 0.85 | Vegetation        |
| `LOTUS` | 0.70 – 0.88 | Water/ground flowers |
| `WAVE`  | 0.70 – 0.90 | Water surface     |

### Horizontal Positioning (X)

`x` ranges from 0.0 to 1.0 (normalized to canvas width). There are no slot restrictions horizontally. Parallax wrapping at `width * 1.5` makes elements scroll in and out seamlessly.

---

## Shape Rendering Details

### Element Size Formula

```
pixelSize = element.scale × canvasWidth × 0.1
```

`scale` ranges from 0.5 to 3.0 (AI-guided). On a 400 dp canvas, that yields 20–120 dp elements.

### Sky Shapes

| Shape      | Geometry                                        | Animation              | Constants                              |
|------------|-------------------------------------------------|------------------------|----------------------------------------|
| `STAR`     | 5-pointed star (inner radius = 40% of outer)    | Full continuous rotation | `STAR_POINTS = 5`, `INNER_RATIO = 0.4` |
| `CRESCENT` | Arc path with inner-arc cutout                   | Rocking ±10°           | `INNER_OFFSET = 0.3`, `SWEEP = 300°`  |
| `AURORA`   | 4 parallel cubic Bezier curves                   | Wave undulation (slowTime) | `CURVES = 4`, `SPACING = 0.15`      |
| `CIRCLE`   | Filled circle                                    | Breathing (scale ±8%)  | `BREATHE_AMPLITUDE = 0.08`             |

### Upper Shapes

| Shape     | Geometry                                           | Animation              | Constants                                      |
|-----------|----------------------------------------------------|------------------------|-------------------------------------------------|
| `CLOUD`   | 3 overlapping ovals                                | Vertical bob (±5%)     | `OVAL_RATIO = 0.35`, `BOB_RATIO = 0.05`        |
| `DIAMOND` | Elongated 4-point rhombus                          | Full continuous rotation | `ELONGATION = 0.7`, `WIDTH_RATIO = 0.5`       |
| `SPIRAL`  | Logarithmic spiral (60 points, 3 rotations), stroked | Full continuous rotation | `POINTS = 60`, `ROTATIONS = 3`, `GROWTH = 0.15` |
| `CRYSTAL` | Hexagon with 3 internal facet lines                | Alpha shimmer (0.85 ± 0.15) | `SIDES = 6`, `FACET_ALPHA = 0.4`          |

### Mid Shapes

| Shape      | Geometry                               | Animation | Constants              |
|------------|----------------------------------------|-----------|------------------------|
| `TRIANGLE` | Filled upward-pointing triangle        | None      | —                      |
| `MOUNTAIN` | Triangle with peak at 60% of height    | None      | `PEAK_RATIO = 0.6`    |

### Ground Shapes

| Shape   | Geometry                                 | Animation                  | Constants                                          |
|---------|------------------------------------------|----------------------------|----------------------------------------------------|
| `TREE`  | Rectangular trunk + triangular canopy    | Horizontal sway (±3%)     | `TRUNK_W = 0.15`, `TRUNK_H = 0.4`, `CANOPY = 0.5` |
| `WAVE`  | Cubic Bezier curves, stroked             | Phase shift via `time`     | `CONTROL_OFFSET = 0.15`, `STROKE_RATIO = 0.1`     |
| `LOTUS` | 6 petals radiating from center           | Breathing (scale ±8%)     | `PETALS = 6`, `PETAL_LENGTH = 0.45`, `WIDTH = 0.15`|

---

## Particle Shapes

Particles float in a grid band (5% margin on all sides). Max 15 displayed regardless of `count`.

| Shape          | Geometry                           | Animation                  | Movement                        |
|----------------|------------------------------------|----------------------------|---------------------------------|
| `DOT`          | Filled circle                      | —                          | Lissajous drift (freq 0.5, amp 0.01) |
| `SPARKLE`      | Plus sign (2 crossed lines)        | Alpha twinkle (3× freq)   | Horizontal scroll               |
| `RING`         | Stroked circle (stroke = 0.3× size)| Breathing scale (±30%)     | Horizontal scroll               |
| `TEARDROP`     | Cubic Bezier drop shape            | Horizontal sway            | Downward fall                   |
| `DIAMOND_MOTE` | Small filled diamond               | Continuous rotation         | Horizontal scroll               |
| `DASH`         | Horizontal line                    | Rocking ±15°              | Horizontal scroll (1.5× speed) |
| `STARBURST`    | Plus + diagonal lines (8 rays)     | Alpha twinkle (5× freq)   | Horizontal scroll               |

### Particle Value Ranges

| Field   | Min  | Max  | Notes                              |
|---------|------|------|------------------------------------|
| `count` | 5    | 30   | Clamped to 15 at render time       |
| `speed` | 0.5  | 2.0  | Horizontal scroll multiplier       |
| `size`  | 2.0  | 8.0  | Visual diameter in pixels          |
| `color` | —    | —    | ARGB Long, alpha often baked in    |

---

## Color System

### Background Gradient

`DreamPalette` provides three colors:

| Field     | Usage                                    |
|-----------|------------------------------------------|
| `sky`     | Top of the vertical gradient             |
| `horizon` | Bottom of the vertical gradient          |
| `accent`  | **Currently unused by the canvas**       |

The canvas draws a full-height linear gradient from `sky` (top) to `horizon` (bottom).

### Element Colors

Colors are ARGB `Long` values generated by the AI. `element.alpha` is multiplied into the color before drawing. The AI prompt provides thematic guidance:

| Theme         | Suggested Shapes                          | Implied Palette                  |
|---------------|-------------------------------------------|----------------------------------|
| Nature        | TREE, MOUNTAIN, LOTUS, AURORA, WAVE, CLOUD | Greens, earth tones, sky blues   |
| Night / space | STAR, CRESCENT, CRYSTAL, CIRCLE           | Deep blues/purples, white/silver |
| Abstract      | SPIRAL, DIAMOND, AURORA, WAVE             | Vivid/unusual combinations       |
| Water / ocean | WAVE, CIRCLE, CRESCENT                    | Blues, teals, silvers            |

### Particle Colors

Also AI-chosen ARGB `Long` values. Alpha is typically baked into the color itself (e.g., `0xCCE0E1DD` = ~80% opacity silver). The prompt suggests particles by theme:

| Theme         | Suggested Particles                |
|---------------|------------------------------------|
| Nature        | TEARDROP, DOT                      |
| Night / space | SPARKLE, STARBURST, DIAMOND_MOTE   |
| Abstract      | RING, DASH, DIAMOND_MOTE           |
| Water / ocean | TEARDROP, DOT, RING                |

---

## Element Value Ranges

| Field   | Min  | Max  | Notes                                      |
|---------|------|------|--------------------------------------------|
| `x`     | 0.0  | 1.0  | Normalized horizontal position             |
| `y`     | 0.0  | 1.0  | Remapped into the shape's vertical zone    |
| `scale` | 0.5  | 3.0  | Size multiplier (see size formula above)   |
| `alpha` | 0.0  | 1.0  | Opacity, multiplied into color             |
| `color` | —    | —    | ARGB Long (e.g., `0xFF415A77`)             |

---

## Layer System

| Field   | Range    | Purpose                                          |
|---------|----------|--------------------------------------------------|
| `depth` | 0.0–1.0  | Parallax factor — higher = faster scroll         |

Layers are sorted by `depth` (back to front) before rendering. Each layer applies:

- **Parallax offset**: `time × depth × canvasWidth`
- **Vertical drift**: `sin(time × 2π + element.x × 2π) × canvasHeight × 0.01 × depth`
- **Horizontal wrapping**: `(baseX + offset) % (width × 1.5) - width × 0.25`

Scene structure guided by the AI prompt: 3–5 layers, 2–4 elements each.

---

## Animation Timing

| Timer      | Duration | Used By                                        |
|------------|----------|------------------------------------------------|
| `time`     | 20 s     | Most element animations, all particle movement |
| `slowTime` | 35 s     | Aurora undulation, spiral secondary motion      |

Both loop infinitely via `rememberInfiniteTransition`.

---

## Observations

1. **`palette.accent` is generated but never read** by the canvas. It could be used for element highlights or particle tints.
2. **Mid slot has only 2 shapes** (`TRIANGLE`, `MOUNTAIN`) and **neither is animated**, so this zone can feel static.
3. **No runtime validation** on AI values — `scale`, `alpha`, `x`, `y` are used as-is. Out-of-range values (e.g., `scale = 10.0`) produce oversized elements. Only particle `count` is clamped.
4. **Zone overlap** — `CIRCLE` (0.10–0.40) overlaps with `CLOUD` (0.12–0.35) and `DIAMOND` (0.15–0.45), so the upper area can get crowded if the AI places many shapes there.
