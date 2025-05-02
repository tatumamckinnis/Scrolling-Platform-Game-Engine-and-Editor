## Description

In XmlBlueprintsWriter.saveSpriteIfNeeded(), the cache key that determines whether a sprite-sheet
XML is written only considers the game name, so when multiple sprites (with different base‐frame
names) belonged to the same game, only the first sprite file was ever generated and reused for all
later sprites. This made future files use the wrong sprite files.

## Expected Behavior

When two SpriteData objects share the same gameName but have different baseFrame().name(),
calling the saving method should write two distinct file names and cache them separately.

## Current Behavior

Because the cache key was only the gameName, the first call to saveSpriteSheetsIfNeeded writes
"sprite_game_one.xml" and stores it under "MyGame" in savedSprites. The next call sees that
"MyGame" already exists in savedSprites and returns "sprite_game_one.xml", never exporting
or returning the correct file for spriteTwo.

## Steps to Reproduce

1. Create two FrameData records with distinct names:
2. Wrap each in a SpriteData for the same game:
3. Invoke writer.saveSpriteIfNeeded(); on both sprite data
   String file2 = writer.saveSpriteIfNeeded("MyGame", s2);

## Hypothesis for Fixing the Bug

**Code Fix**
Change the cache key to include both gameName and the sprite’s base‐frame name:
String key = gameName + "#" + sprite.baseFrame().name();

This ensures each unique combo gets its
own entry in savedSprites, and each sprite is written exactly once under the correct file name.
