package oogasalad.engine.model.object;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.SpriteData;

/**
 * Abstract representation of a game object within the engine.
 *
 * <p>Each {@code GameObject} has a unique identifier (UUID), blueprint metadata,
 * hitbox dimensions, sprite rendering data, and runtime parameters.
 * It can also contain a list of {@link Event} objects that define
 * how the object behaves during gameplay.
 *
 * <p>This class serves as a base for specific object types such as players, enemies, or terrain.
 * Subclasses should implement behavior and game-specific logic as needed.
 *
 * @author Alana Zinkin
 */
public abstract class GameObject {

  private final UUID uuid;
  private int myBlueprintID;
  private String myType;
  private int myHitBoxX;
  private int myHitBoxY;
  private int myHitBoxWidth;
  private int myHitBoxHeight;
  private int myLayer;
  private String myName;
  private String myGroup;
  private SpriteData mySpriteData;
  private Map<String, String> myParams;
  private List<Event> myEvents;

  /**
   * Constructs a new GameObject with the provided attributes.
   *
   * @param uuid unique identifier for the object
   * @param blueprintID ID associated with the object's blueprint
   * @param type the type/category of the object
   * @param hitBoxX the x-position of the hitbox
   * @param hitBoxY the y-position of the hitbox
   * @param hitBoxWidth width of the hitbox
   * @param hitBoxHeight height of the hitbox
   * @param layer the rendering layer of the object
   * @param name the display name of the object
   * @param group the group or category the object belongs to (e.g., "Background", "Enemies")
   * @param spriteData sprite rendering metadata
   * @param params map of dynamic runtime parameters
   * @param events list of events associated with this object
   */
  public GameObject(UUID uuid, int blueprintID, String type, int hitBoxX, int hitBoxY,
      int hitBoxWidth, int hitBoxHeight, int layer, String name, String group,
      SpriteData spriteData, Map<String, String> params, List<Event> events) {
    this.uuid = uuid;
    this.myBlueprintID = blueprintID;
    this.myType = type;
    this.myHitBoxX = hitBoxX;
    this.myHitBoxY = hitBoxY;
    this.myHitBoxWidth = hitBoxWidth;
    this.myHitBoxHeight = hitBoxHeight;
    this.myLayer = layer;
    this.myName = name;
    this.myGroup = group;
    this.mySpriteData = spriteData;
    this.myParams = params;
    this.myEvents = events;
  }

  /**
   * Returns the unique UUID of the object as a string.
   *
   * @return object's UUID
   */
  public String getUuid() {
    return uuid.toString();
  }

  /**
   * Returns the blueprint ID for the object.
   *
   * @return blueprint ID
   */
  public int getBlueprintID() {
    return myBlueprintID;
  }

  /**
   * Returns the type/category of the object.
   *
   * @return object type
   */
  public String getType() {
    return myType;
  }

  /**
   * Returns the x-position of the object's hitbox.
   *
   * @return x-position
   */
  public int getX() {
    return myHitBoxX;
  }

  /**
   * Returns the y-position of the object's hitbox.
   *
   * @return y-position
   */
  public int getY() {
    return myHitBoxY;
  }

  /**
   * Returns the width of the object's hitbox.
   *
   * @return hitbox width
   */
  public int getWidth() {
    return myHitBoxWidth;
  }

  /**
   * Returns the height of the object's hitbox.
   *
   * @return hitbox height
   */
  public int getHeight() {
    return myHitBoxHeight;
  }

  /**
   * Returns the layer on which the object should be rendered.
   *
   * @return render layer
   */
  public int getLayer() {
    return myLayer;
  }

  /**
   * Returns the name of the object.
   *
   * @return object name
   */
  public String getName() {
    return myName;
  }

  /**
   * Returns the group this object belongs to.
   *
   * @return object group
   */
  public String getGroup() {
    return myGroup;
  }

  /**
   * Returns the sprite data associated with this object.
   *
   * @return sprite data
   */
  public SpriteData getSpriteData() {
    return mySpriteData;
  }

  /**
   * Returns a map of dynamic parameters associated with this object.
   *
   * @return parameter map
   */
  public Map<String, String> getParams() {
    return myParams;
  }

  /**
   * Returns the list of events assigned to this object.
   *
   * @return list of events
   */
  public List<Event> getEvents() {
    return myEvents;
  }

  /**
   * Updates the x-position of the object.
   *
   * @param xPos new x-position
   */
  public void setX(int xPos) {
    this.myHitBoxX = xPos;
  }

  /**
   * Updates the y-position of the object.
   *
   * @param yPos new y-position
   */
  public void setY(int yPos) {
    this.myHitBoxY = yPos;
  }

  /**
   * Updates the width of the object's hitbox.
   *
   * @param width new hitbox width
   */
  public void setWidth(int width) {
    this.myHitBoxWidth = width;
  }

  /**
   * Updates the height of the object's hitbox.
   *
   * @param height new hitbox height
   */
  public void setHeight(int height) {
    this.myHitBoxHeight = height;
  }

  /**
   * Updates the group/category of the object.
   *
   * @param group new group name
   */
  public void setGroup(String group) {
    this.myGroup = group;
  }

  /**
   * Updates the sprite data used to render this object.
   *
   * @param spriteData new sprite data
   */
  public void setSpriteData(SpriteData spriteData) {
    this.mySpriteData = spriteData;
  }

  /**
   * Sets the list of events associated with this object.
   *
   * @param events list of events
   */
  public void setEvents(List<Event> events) {
    myEvents = events;
  }
}
