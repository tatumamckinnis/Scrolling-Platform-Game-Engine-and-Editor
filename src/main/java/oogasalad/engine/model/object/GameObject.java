package oogasalad.engine.model.object;

import oogasalad.fileparser.records.SpriteData;

import java.util.Map;

/**
 * Abstract representation of a game object within the engine. Stores identifying information,
 * grouping metadata, visual sprite data, and a collection of dynamic parameters for runtime
 * behavior.
 *
 * @author Alana Zinkin
 */
public abstract class GameObject {

  /**
   * Unique identifier for the game object
   */
  private final String uuid;
  /**
   * Name of the game object
   */
  private String myName;
  /**
   * Group or category to which the game object belongs
   */
  private String myGroup;
  /**
   * Visual sprite data associated with the game object
   */
  private SpriteData mySpriteData;
  /**
   * Dynamic parameters that describe the object's runtime state
   */
  private Map<String, String> params;

  private int xPos;

  private int yPos;

  private int hitboxLength;

  private ObjectType objectType;

  /**
   * Constructs a new GameObject with the specified properties.
   *
   * @param uuid       unique identifier for this object
   * @param name       the display name of the object
   * @param group      the group or category the object belongs to
   * @param spriteData visual data used to render the object
   * @param params     dynamic variables associated with the object's behavior
   */
  public GameObject(String uuid, String name, String group, SpriteData spriteData,
      Map<String, String> params, int xPos, int yPos, int hitboxLength, ObjectType objectType) {
    this.uuid = uuid;
    this.myName = name;
    this.myGroup = group;
    this.mySpriteData = spriteData;
    this.params = params;
    this.xPos = xPos;
    this.yPos = yPos;
    this.hitboxLength = hitboxLength;
    this.objectType = objectType;
  }

  public enum ObjectType {
    ENEMY,
    PLATFORM
  }

  /**
   * Returns the dynamic parameters associated with the object.
   *
   * @return the object's dynamic variable collection
   */
  public Map<String, String> getParams() {
    return params;
  }

  /**
   * Returns the unique identifier of the object.
   *
   * @return the object's UUID
   */
  public String getUuid() {
    return uuid;
  }

  /**
   * Returns the display name of the object.
   *
   * @return the object's name
   */
  public String getName() {
    return myName;
  }

  /**
   * Returns the group or category of the object.
   *
   * @return the object's group
   */
  public String getGroup() {
    return myGroup;
  }

  /**
   * Returns the sprite data associated with the object.
   *
   * @return the object's SpriteData
   */
  public SpriteData getSpriteData() {
    return mySpriteData;
  }

  public int getxPos() {
    return xPos;
  }

  public int getyPos() {
    return yPos;
  }

  public int getHitboxLength() {
    return hitboxLength;
  }

  public ObjectType getObjectType() {
    return objectType;
  }

  public void setX(int xPos) {
    this.xPos = xPos;
  }

}
