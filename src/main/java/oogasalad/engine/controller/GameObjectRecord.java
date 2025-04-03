package oogasalad.engine.controller;


import oogasalad.fileparser.records.FrameData;

public record GameObjectRecord (
    int spriteX,
    int spriteY,
    FrameData currentFrame
) {}
