# Aozai Ink MC

Aozai Ink MC is a NeoForge 1.21.1 foundation mod that turns handwritten Chinese glyphs into semantic world states for Minecraft addons.

It is not a spell-content mod. The core only provides the handwriting ritual, ONNX glyph recognition, `InkMark` state model, API access, and attachment events. Gameplay effects are meant to live in official example mods or third-party addons.

## Core Idea

```text
Handwriting -> ONNX OCR -> semantic tags -> InkMark -> API/event bus
```

The base mod does not create fireballs, healing, walls, dimensions, buffs, skill JSON, databases, or server services. It exposes readable semantic state so other mods can decide what a glyph means in the current world context.

## Current Controls

Take the `Ink Brush` from the creative inventory, hold it, then use:

```text
G       Toggle the casting circle. Recognized glyphs attach an InkMark to the player.
V       Toggle the inscription circle. Recognized glyphs anchor an InkMark to a world marker.
Left    Write strokes on the circle.
Enter   Recognize the glyph.
R       Clear strokes.
X       Cancel the active circle.
```

The casting circle and inscription circle are mutually exclusive.

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
