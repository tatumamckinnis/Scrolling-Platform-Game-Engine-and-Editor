package oogasalad.engine.model.object;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;

/**
 * The player class represents a player object, which is typically controlled using key inputs (but
 * not required to be) Player's are typically how the user represents their own actions within the
 * game
 *
 * @author Alana Zinkin
 */
public class DefaultGameObject extends GameObject {


  /**
   * Constructs a new GameObject with the provided attributes.
   *
   * @param uuid         unique identifier for the object
   * @param blueprintID  ID associated with the object's blueprint
   * @param type         the type/category of the object
   * @param hitBoxX      the x-position of the hitbox
   * @param hitBoxY      the y-position of the hitbox
   * @param hitBoxWidth  width of the hitbox
   * @param hitBoxHeight height of the hitbox
   * @param layer        the rendering layer of the object
   * @param name         the display name of the object
   * @param group        the group or category the object belongs to (e.g., "Background",
   *                     "Enemies")
   * @param spriteData
   * @param currentFrame
   * @param frameMap
   * @param animationMap
   * @param params       map of dynamic runtime parameters
   * @param events       list of events associated with this object
   * @param hitBoxData
   */
  public DefaultGameObject(UUID uuid, int blueprintID, String type, int hitBoxX, int hitBoxY,
      int hitBoxWidth, int hitBoxHeight, int layer, String name, String group,
      SpriteData spriteData,
      FrameData currentFrame, Map<String, FrameData> frameMap,
      Map<String, AnimationData> animationMap, Map<String, String> params, List<Event> events,
      HitBoxData hitBoxData) {
    super(uuid, blueprintID, type, hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight, layer, name, group,
        spriteData, currentFrame, frameMap, animationMap, params, events, hitBoxData);
  }
}
