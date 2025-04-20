package oogasalad.editor.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.object.sprite.FrameData;

/**
 * Maintains a collection of sprite-sheet atlases and provides lookup of frames
 * by atlas identifier and frame name.
 * <p>
 * Each atlas is registered under a unique name, and its frames are indexed internally
 * for quick retrieval. Clients can retrieve an entire atlas, list all frames, or
 * fetch a specific frame data by atlas and frame name.
 * </p>
 *
 * @author Jacob You
 */
public class SpriteSheetLibrary {

  private final Map<String, SpriteSheetAtlas> atlases;
  private final Map<String, FrameData> frameIndex;

  /**
   * Constructs an empty SpriteSheetLibrary.
   */
  public SpriteSheetLibrary() {
    atlases = new HashMap<>();
    frameIndex = new HashMap<>();
  }

  /**
   * Registers the given atlas under the specified name and indexes all of its frames.
   * <p>
   * Frame entries are internally stored under keys formed as
   * "<atlasName>:<frameName>" for subsequent lookup.
   * </p>
   *
   * @param atlasName the unique name for this atlas
   * @param atlas     the SpriteSheetAtlas whose frames should be indexed
   */
  public void addAtlas(String atlasName, SpriteSheetAtlas atlas) {
    atlases.put(atlasName, atlas);
    for (FrameData frameData : atlas.frames()) {
      frameIndex.put(atlasName + ':' + frameData.name(), frameData);
    }
  }

  /**
   * Retrieves the atlas registered under the given identifier.
   *
   * @param atlasId the name of the atlas to retrieve
   * @return the corresponding SpriteSheetAtlas, or null if no atlas is found
   */
  public SpriteSheetAtlas getAtlas(String atlasId) {
    return atlases.get(atlasId);
  }

  /**
   * Returns a list of all FrameData objects indexed across all registered atlases.
   *
   * @return a new List containing every FrameData in the library
   */
  public List<FrameData> getAllFrames() {
    return new ArrayList<>(frameIndex.values());
  }

  /**
   * Retrieves a single frame by its atlas identifier and frame name.
   *
   * @param atlasId   the name of the atlas containing the frame
   * @param frameName the name of the frame to retrieve
   * @return the matching FrameData, or null if not found
   */
  public FrameData getFrame(String atlasId, String frameName) {
    return frameIndex.get(atlasId + ':' + frameName);
  }
}
