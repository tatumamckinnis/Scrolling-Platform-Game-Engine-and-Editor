## Description

When loading in level data, the camera data of the game is not updated. The editor's CameraData class is not called, and the camera uses the default camera.

## Expected Behavior

When loading level data, the editor should know what the camera data is. It should set all the values of the CameraData class to the data from the loaded level.

## Current Behavior

When loading level data, the camera is not updated according to the level file. It defaults to preset values instead.

## Steps to Reproduce

1. Save or obtain a level data with a specified camera
2. Open the game engine and open the editor on the specific level
3. Open level properties, and camera data will be the default

## Hypothesis for Fixing the Bug

We simply need to save all of the camera info in the file into the level data's camera.
Something like this would work:
CameraDataManager.replaceStringParams(LevelData.cameraData().stringParams())
CameraDataManager.replaceDoubleParams(LevelData.cameraData().doubleParams())