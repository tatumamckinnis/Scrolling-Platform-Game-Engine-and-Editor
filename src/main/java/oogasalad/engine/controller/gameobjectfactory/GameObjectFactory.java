package oogasalad.engine.controller.gameobjectfactory;

import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.engine.model.object.DynamicVariableCollection;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.SpriteData;

/**
 * Interface for creating instances of {@link GameObject}s based on configuration data.
 *
 * <p>Implementations of this factory encapsulate the logic needed to instantiate specific
 * types of game objects (e.g., Player, Enemy, Block), typically using reflection or type-based
 * mapping.
 *
 * <p>This is used to decouple the object creation logic from the engine's controller logic.
 */
public interface GameObjectFactory {

  /**
   * Creates a new {@link GameObject} instance using the specified parameters.
   *
   * <p>This method dynamically instantiates a game object based on the object's type and
   * attributes,
   * which are typically provided from a level configuration file.
   *
   * @param uuid         unique identifier for the object
   * @param blueprintID  ID of the object's blueprint (used for grouping or reference)
   * @param hitBoxX      x-position of the object's hitbox
   * @param hitBoxY      y-position of the object's hitbox
   * @param hitBoxWidth  width of the object's hitbox
   * @param hitBoxHeight height of the object's hitbox
   * @param layer        the rendering layer of the object
   * @param name         the name or type of the object (e.g., "Player", "Enemy")
   * @param group        the group this object belongs to (e.g., "Blocks", "Background")
   * @param spriteData   visual data used to render the object
   * @param params       dynamic runtime variables for the object
   * @param events       list of events associated with this object
   * @return a newly created {@link GameObject} with the provided configuration
   */
  GameObject createGameObject(
      UUID uuid,
      int blueprintID,
      int hitBoxX,
      int hitBoxY,
      int hitBoxWidth,
      int hitBoxHeight,
      int layer,
      String name,
      String group,
      SpriteData spriteData,
      DynamicVariableCollection params,
      List<Event> events
  );
}
