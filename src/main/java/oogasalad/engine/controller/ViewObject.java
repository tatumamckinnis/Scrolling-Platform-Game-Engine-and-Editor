package oogasalad.engine.controller;

import oogasalad.fileparser.records.FrameData;

/**
 * Immutable data record representing a visual object to be rendered in the view layer.
 *
 * <p>This record contains positional and sizing information for both the hitbox and
 * sprite image, along with a reference to the current frame of the sprite.
 * It serves as a clean data transfer object between the engine and the UI.
 *
 * @param uuid unique identifier of the object
 * @param hitBoxXPosition x-position of the hitbox (typically matches the game object logic position)
 * @param hitBoxYPosition y-position of the hitbox
 * @param spriteDx horizontal offset of the sprite image relative to the hitbox
 * @param spriteDy vertical offset of the sprite image relative to the hitbox
 * @param hitBoxWidth width of the hitbox
 * @param hitBoxHeight height of the hitbox
 * @param currentFrame the current {@link FrameData} representing the sprite image to render
 */
public record ViewObject(
    String uuid,
    int hitBoxXPosition,
    int hitBoxYPosition,
    int spriteDx,
    int spriteDy,
    int hitBoxWidth,
    int hitBoxHeight,
    FrameData currentFrame
) {}
