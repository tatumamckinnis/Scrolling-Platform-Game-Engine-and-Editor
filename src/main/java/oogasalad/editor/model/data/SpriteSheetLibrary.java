package oogasalad.editor.model.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.object.sprite.FrameData;

public class SpriteSheetLibrary {
  private final Map<String, SpriteSheetAtlas> atlases;
  private final Map<String, FrameData> frameIndex;

  public SpriteSheetLibrary() {
    atlases = new HashMap<>();
    frameIndex = new HashMap<>();
  }

  public void addAtlas(String atlasName, SpriteSheetAtlas atlas){
    atlases.put(atlasName, atlas);
    for (FrameData frameData : atlas.frames()) {
      frameIndex.put(atlasName + ':' + frameData, frameData);
    }
  }

  public SpriteSheetAtlas getAtlas(String atlasId) {
    return atlases.get(atlasId);
  }

  public List<FrameData> getAllFrames() {
    return new ArrayList<>(frameIndex.values());
  }

  public FrameData getFrame(String atlasId, String frameName){
    return frameIndex.get(atlasId + ':' + frameName);
  }
}
