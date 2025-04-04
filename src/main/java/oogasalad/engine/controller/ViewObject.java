package oogasalad.engine.controller;


import oogasalad.fileparser.records.FrameData;

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
