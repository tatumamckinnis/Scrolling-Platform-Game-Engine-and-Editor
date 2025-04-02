package oogasalad.engine.model.object;

import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.Event;
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

  public GameObject(UUID uuid, int blueprintID, String type,  int hitBoxX, int hitBoxY, int hitBoxWidth, int hitBoxHeight, int layer, String name, String group, SpriteData spriteData,
      Map<String, String> params, List<Event> events) {
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
   * Returns the unique identifier of the object.
   *
   * @return the object's UUID
   */
  public String getUuid() {
    return uuid.toString();
  }

  public int getBlueprintID() {
    return myBlueprintID;
  }


  public String getType() {
    return myType;
  }

  public int getX() {
    return myHitBoxX;
  }

  public int getY() {
    return myHitBoxY;
  }

  public int getWidth() {
    return myHitBoxWidth;
  }

  public int getHeight() {
    return myHitBoxHeight;
  }
  /**
   * Returns the dynamic parameters associated with the object.
   *
   * @return the object's dynamic variable collection
   */
  public Map<String, String> getParams() {
    return myParams;
  }


  public String getName() {
    return myName;
  }

  public String getGroup() {
    return myGroup;
  }

  public SpriteData getSpriteData() {
    return mySpriteData;
  }

  public List<Event> getEvents() {
    return myEvents;
  }

  public void setX(int xPos) {
    this.myHitBoxX = xPos;
  }

  public void setY(int yPos) {
    this.myHitBoxY = yPos;
  }

  public void setWidth(int width) {
    this.myHitBoxWidth = width;
  }

  public void setHeight(int height) {
    this.myHitBoxHeight = height;
  }

  public void setGroup(String group) {
    this.myGroup = group;
  }

  public void setSpriteData(SpriteData spriteData) {
    this.mySpriteData = spriteData;
  }

  public void setEvents(List<Event> events) {
    myEvents = events;
  }

}
