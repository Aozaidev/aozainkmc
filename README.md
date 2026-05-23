# Aozai Ink MC

Aozai Ink MC is a NeoForge 1.21.1 foundation mod that turns handwritten Chinese glyphs into semantic world states for Minecraft addons.

It is not a spell-content mod. The core only provides the handwriting ritual, ONNX glyph recognition, `InkMark` state model, API access, and attachment events. Gameplay effects are meant to live in official example mods or third-party addons.

## Core Idea

```text
Handwriting -> ONNX OCR -> semantic tags -> InkMark -> API/event bus
```

The base mod does not create fireballs, healing, walls, dimensions, buffs, skill JSON, databases, or server services. It exposes readable semantic state so other mods can decide what a glyph means in the current world context.

## Current Controls

Take an ink wand from the creative inventory or craft the wooden wand, hold it, then use:

```text
G       Toggle the casting circle. Recognized glyphs attach an InkMark to the player.
V       Toggle the inscription circle. Recognized glyphs anchor an InkMark to a world marker.
Left    Write strokes on the circle.
Enter   Recognize the glyph.
R       Clear strokes.
X       Cancel the active circle.
```

The casting circle and inscription circle are mutually exclusive.

## Ink Wands

Aozai Ink MC provides the handwriting tools, durability, recipes, and metadata only. It does not decide what stronger wands do to gameplay effects.

Current wand tiers:

```text
wood       power 1.00
stone      power 1.10
copper     power 1.15
iron       power 1.25
gold       power 1.35
diamond    power 1.60
netherite  power 1.80
```

Successful recognition writes the wand tier into `InkMark.source` as metadata such as:

```text
onnx.handwriting.cast;staff=diamond;power=1.6
```

Addons should read this through:

```text
com.aozainkmc.api.InkStaffMetadata
com.aozainkmc.api.InkStaffTier
```

While a writing circle is active, attack and pick-block input are cancelled so left-click strokes do not attack or mine through the circle.

The wooden wand recipe is intentionally short:

```text
  plank
stick
```

Upgrade recipes use the same short shape:

```text
  material
previous wand
```

## API Surface

Useful entry points:

```text
com.aozainkmc.api.AozaiInkApi
com.aozainkmc.api.InkMark
com.aozainkmc.api.InkTarget
com.aozainkmc.core.event.InkMarkAttachedEvent
```

Addons can listen for `InkMarkAttachedEvent`, query `AozaiInkApi.marks()`, and react to marks on players, entities, blocks, chunks, levels, items, or world markers.

## Build

Requirements:

```text
Java 21
Gradle 8.14.3 or newer
Minecraft 1.21.1
NeoForge 21.1.230
```

Build:

```powershell
gradle build
```

Run the client dev environment:

```powershell
gradle runClient
```

## License

MIT. See `LICENSE`.
