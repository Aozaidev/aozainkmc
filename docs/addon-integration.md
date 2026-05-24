# Aozai Ink MC Addon Integration

This is the supported addon surface for Aozai Ink MC.

The base mod is intentionally small: it recognizes handwritten glyphs, creates `InkMark` records, exposes stave metadata, and sends events. Your addon decides what each glyph does.

Current runtime scope: integrated singleplayer worlds. The API shape is designed so the same addon logic can move to networked/server handling later.

## Packages

Use these packages in new addons:

```java
com.aozainkmc.api.*
com.aozainkmc.api.event.*
```

Legacy event imports under `com.aozainkmc.core.event.*` still work for existing addons, but new code should use `api.event`.

## Minimal Working Addon

Register your glyphs during mod construction:

```java
public ExampleAddon(IEventBus modBus) {
    InkGlyphRegistry.register(InkGlyphDefinition.builder("火")
        .castOnly()
        .minimumStaffTier(InkStaffTier.WOOD)
        .closeAfterRecognize()
        .build());
}
```

Then listen on the NeoForge event bus:

```java
@SubscribeEvent
public static void onInkMarkAttached(InkMarkAttachedEvent event) {
    InkMark mark = event.mark();
    if (!mark.word().equals("火")) {
        return;
    }
    if (mark.target().type() != InkTargetType.PLAYER) {
        return;
    }

    InkStaffTier tier = InkStaffMetadata.tier(mark);
    float power = InkStaffMetadata.powerMultiplier(mark);
    // Apply your effect here.
}
```

That is the default path: register a glyph, receive an attached mark, read the target and stave tier, then apply your effect.

## Registering Glyphs

Old style still works:

```java
InkGlyphRegistry.register("火");
InkGlyphRegistry.registerAll(List.of("火", "水", "山"));
InkGlyphClientBehaviorRegistry.registerCloseAfterRecognize("山");
```

Preferred style:

```java
InkGlyphRegistry.register(InkGlyphDefinition.builder("山")
    .anchorOnly()
    .minimumStaffTier(InkStaffTier.STONE)
    .closeAfterRecognize()
    .build());
```

Definitions let the base mod reject invalid casts before your addon has to reason about them.

Fields:

```text
word                  the recognized Chinese glyph
modes                 CAST, ANCHOR, or both
minimumStaffTier      lowest stave tier allowed
closeAfterRecognize   close the circle after a successful recognition
```

## Targets

Self-cast glyphs use:

```java
mark.target().type() == InkTargetType.PLAYER
mark.target().entityUuid()
```

World-anchored glyphs use:

```java
mark.target().type() == InkTargetType.MARKER
BlockPos.of(mark.target().packedBlockPos())
```

Other target types exist for addon use:

```text
PLAYER, ENTITY, ITEM, BLOCK, CHUNK, LEVEL, MARKER
```

The base mod currently creates `PLAYER` and `MARKER` marks from the two built-in circles.

## Before Attach: Cancel, Consume, or Continue

Use `InkMarkBeforeAttachEvent` when your glyph should not become a normal stored mark, or when you need a second step.

Cancel without consuming durability or progress:

```java
@SubscribeEvent
public static void onBeforeAttach(InkMarkBeforeAttachEvent event) {
    if (!event.mark().word().equals("门")) {
        return;
    }
    event.setCanceled(true);
    event.requestCloseInput("This glyph is not ready yet");
}
```

Cancel but still count as a successful cast:

```java
event.setCanceled(true);
event.setConsumeOnCancel(true);
event.addExtraDurabilityCost(2);
event.requestCloseInput("Cast consumed");
```

If not canceled, the base mod attaches the mark, adds 1 stave momentum, and damages the current stave by `1 + extraDurabilityCost`.

## Two-step Block Selection

Use this for glyphs like a seal or a remote target selector.

Step 1: ask the client to pick a block.

```java
@SubscribeEvent
public static void onBeforeAttach(InkMarkBeforeAttachEvent event) {
    if (!event.mark().word().equals("印")) {
        return;
    }

    event.setCanceled(true);
    event.setConsumeOnCancel(true);
    event.requestBlockTarget("example:seal:" + event.player().getUUID(), "Choose a block", 32);
}
```

Step 2: receive the selected block and reopen a circle if needed.

```java
@SubscribeEvent
public static void onBlockSelected(InkBlockTargetSelectedEvent event) {
    if (!event.token().startsWith("example:seal:")) {
        return;
    }

    BlockPos selected = event.pos();
    // Store pending addon state here.
    event.requestOpenCastInput("Block selected. Write the glyph to seal.");
}
```

The token is opaque to the base mod. Use a namespaced token so addons do not collide.

## Cast Context and Recognition Candidates

`InkMarkBeforeAttachEvent` exposes a context:

```java
InkCastContext context = event.context();
context.player();
context.mode();        // CAST or ANCHOR
context.staffTier();
context.mark();
context.recognition(); // top recognition candidates
```

Use this if your addon wants OCR details:

```java
for (RecognizedGlyph candidate : event.context().recognition().candidates()) {
    String word = candidate.word();
    float confidence = candidate.confidence();
}
```

`InkMark.confidence()` is still the best candidate confidence for simple addons.

## Staff Helpers

Use `InkStaffs` instead of depending on core item classes:

```java
boolean isStaff = InkStaffs.isStaff(stack);
Optional<InkStaffTier> tier = InkStaffs.tier(stack);
Item diamondStaff = InkStaffs.itemForTier(InkStaffTier.DIAMOND);
```

Use `InkStaffProgress` when your addon intentionally participates in the built-in stave progression:

```java
InkStaffProgress.addProgress(stack, tier, 5);
InkStaffProgress.setBreakthroughReady(stack, true);
```

## Mark Store

The mark store is available through:

```java
AozaiInkApi.marks()
```

Common operations:

```java
AozaiInkApi.marks().marksOn(target);
AozaiInkApi.marks().allMarks();
AozaiInkApi.marks().clear(target);
```

Most addons should prefer events for immediate effects and query the store for persistent or ticking effects.

## Compatibility Rules

Do:

- Register every glyph your addon handles.
- Use `api.event` in new code.
- Use namespaced block-selection tokens.
- Read stave tier through `InkStaffMetadata` or `InkStaffs`.
- Keep gameplay meanings in your addon.

Avoid:

- Importing `com.aozainkmc.client.*`.
- Parsing `InkMark.source` by hand.
- Depending on `com.aozainkmc.core.*` unless a legacy addon already does so.
- Assuming multiplayer support exists today.
