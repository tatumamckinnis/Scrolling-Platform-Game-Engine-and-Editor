package oogasalad.engine.model.object;

import java.util.List;
import java.util.UUID;
import oogasalad.editor.model.data.Layer;
import oogasalad.engine.event.Event;
import oogasalad.fileparser.records.SpriteData;

/**
 * Abstract representation of a game object within the engine. Stores identifying information,
 * grouping metadata, visual sprite data, and a collection of dynamic parameters for runtime
 * behavior.
 *
 * @author Alana Zinkin
 */
public abstract class GameObject {

  private final UUID uuid;
  private int myBlueprintID;
  private int myHitBoxX;
  private int myHitBoxY;
  private int myHitBoxWidth;
  private int myHitBoxHeight;
  private int myLayer;
  private String myName;
  private String myGroup;
  private SpriteData mySpriteData;
  private DynamicVariableCollection params;
  private List<Event> myEvents;


  public GameObject(UUID uuid, int blueprintID, int hitBoxX, int hitBoxY, int hitBoxWidth, int hitBoxHeight, int layer, String name, String group, SpriteData spriteData,
      DynamicVariableCollection params, List<Event> events) {
    this.uuid = uuid;
    this.myBlueprintID = blueprintID;
    this.myHitBoxX = hitBoxX;
    this.myHitBoxY = hitBoxY;
    this.myHitBoxWidth = hitBoxWidth;
    this.myHitBoxHeight = hitBoxHeight;
    this.myLayer = layer;
    this.myName = name;
    this.myGroup = group;
    this.mySpriteData = spriteData;
    this.params = params;
    this.myEvents = events;
  }

  public int getBlueprintID() {
    return myBlueprintID;
  }

  public int getHitBoxX() {
    return myHitBoxX;
  }

  public int getHitBoxY() {
    return myHitBoxY;
  }

  public int getHitBoxWidth() {
    return myHitBoxWidth;
  }

  public int getHitBoxHeight() {
    return myHitBoxHeight;
  }

  public int getLayer() {
    return myLayer;
  }
  /**
   * Returns the unique identifier of the object.
   *
   * @return the object's UUID
   */
  public String getUuid() {
    return uuid.toString();
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

  /**
   * Returns the dynamic parameters associated with the object.
   *
   * @return the object's dynamic variable collection
   */
  public DynamicVariableCollection getParams() {
    return params;
  }

  public void setEvents(List<Event> events) {
    myEvents = events;
  }
}
