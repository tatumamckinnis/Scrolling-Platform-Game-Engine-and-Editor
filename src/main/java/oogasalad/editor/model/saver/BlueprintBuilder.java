package oogasalad.editor.model.saver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.OutcomeData;
import oogasalad.fileparser.records.SpriteData;

/**
 * Builds {@link BlueprintData} records from in-editor objects.
 * This class translates an {@link EditorObject}'s identity, sprite, hitbox, event data,
 * and custom parameters into the {@code BlueprintData} format required by the file parser API.
 *
 * @author Jacob You
 */
public class BlueprintBuilder {

  private static final int BLUEPRINT_PLACEHOLDER_ID = -1;

  /**
   * Converts the given editor object into a {@link BlueprintData} instance.
   * This includes extracting identity, sprite, hitbox, events, and custom parameter data.
   *
   * @param obj the {@link EditorObject} to convert. Must not be null.
   * @return a {@link BlueprintData} record representing the object's state.
   */
  public static BlueprintData fromEditorObject(EditorObject obj) {
    // Get game name from the object's identity data
    String gameName = obj.getIdentityData().getGame();
    // Get group from the object's identity data 
    String group = obj.getIdentityData().getGroup();
    // Type already comes from identity data
    String type = obj.getIdentityData().getType();
    double rotation = obj.getSpriteData().getRotation();
    boolean isFlipped = obj.getSpriteData().getIsFlipped();
    SpriteData spriteData = toSpriteRecord(obj.getSpriteData());
    HitBoxData hitBoxData = toHitboxRecord(obj.getHitboxData(), obj.getSpriteData());
    List<EventData> eventData = toEventRecord(obj);

    double vX = obj.getPhysicsData().getVelocityX();
    double vY = obj.getPhysicsData().getVelocityY();
    Map<String, String> stringProps = new HashMap<>(obj.getStringParameters());
    Map<String, Double> doubleProps = new HashMap<>(obj.getDoubleParameters());

    // Ensure basic physics props are included if not already in custom doubleProps
    doubleProps.putIfAbsent("gravity", obj.getPhysicsData().getGravity());
    doubleProps.putIfAbsent("jump_force", obj.getPhysicsData().getJumpForce());

    // Handle displayed properties list
    List<String> displayList = new ArrayList<>();
    String displayedPropsStr = stringProps.get("displayedProperties");
    if (displayedPropsStr != null && !displayedPropsStr.isEmpty()) {
      String[] props = displayedPropsStr.split(",");
      displayList.addAll(Arrays.asList(props));
      // Remove the temporary string parameter since we now store it in the proper field
      stringProps.remove("displayedProperties");
    }

    return new BlueprintData(
        BLUEPRINT_PLACEHOLDER_ID,
        vX, vY, rotation, isFlipped,
        gameName, group, type,
        spriteData,
        hitBoxData,
        eventData,
        stringProps,
        doubleProps,
        displayList
    );
  }

  /**
   * Converts the editor's sprite data model into the file parser's {@link SpriteData} record format.
   *
   * @param editorSpriteData the {@link oogasalad.editor.model.data.object.sprite.SpriteData} from the editor object.
   * @return the corresponding {@link SpriteData} record.
   */
  private static SpriteData toSpriteRecord(
      oogasalad.editor.model.data.object.sprite.SpriteData editorSpriteData) {
    List<FrameData> frames = new ArrayList<>();
    editorSpriteData.getFrames().forEach((name, editorFrame) -> {
      frames.add(new FrameData(
          name,
          editorFrame.x(),
          editorFrame.y(),
          editorFrame.width(),
          editorFrame.height()));
    });

    List<AnimationData> animations = new ArrayList<>();
    editorSpriteData.getAnimations().forEach((name, editorAnimation) -> {
      animations.add(new AnimationData(
          name,
          editorAnimation.getFrameLength(),
          editorAnimation.getFrameNames()));
    });

    // Find the base frame using the stored name
    FrameData baseFrame = frames.stream()
        .filter(f -> f.name().equals(editorSpriteData.getBaseFrameName()))
        .findFirst()
        .orElse(frames.isEmpty() ? null : frames.get(0)); // Fallback to first frame if base name not found/null

    return new SpriteData(
        editorSpriteData.getName(),
        new File(editorSpriteData.getSpritePath()),
        baseFrame,
        frames,
        animations
    );
  }

  /**
   * Converts the editor's hitbox data and sprite data into the file parser's {@link HitBoxData} record format.
   * Calculates the delta offset between the hitbox and sprite origins.
   *
   * @param editorHitboxData the {@link HitboxData} from the editor object.
   * @param editorSpriteData the {@link oogasalad.editor.model.data.object.sprite.SpriteData} from the editor object.
   * @return the corresponding {@link HitBoxData} record.
   */
  private static HitBoxData toHitboxRecord(
      HitboxData editorHitboxData,
      oogasalad.editor.model.data.object.sprite.SpriteData editorSpriteData) {
    return new HitBoxData(
        editorHitboxData.getShape(),
        editorHitboxData.getWidth(),
        editorHitboxData.getHeight(),
        editorSpriteData.getX() - editorHitboxData.getX(),
        editorSpriteData.getY() - editorHitboxData.getY()
    );
  }

  /**
   * Aggregates and converts all event data (Collision, Input, Physics, Custom) from the editor object
   * into a list of file parser's {@link EventData} records. Sorts the events based on the order defined
   * in the object's general event data.
   *
   * @param obj the {@link EditorObject} containing the event data.
   * @return a sorted list of {@link EventData} records.
   */
  private static List<EventData> toEventRecord(EditorObject obj) {
    List<EventData> all = new ArrayList<>();

    addEvents("Collision", obj.getCollisionData().getEvents(), all);
    addEvents("Input",     obj.getInputData().getEvents(),     all);
    addEvents("Physics",   obj.getPhysicsData().getEvents(),   all);
    addEvents("Custom",    obj.getCustomEventData().getEvents(), all);

    sortEventRecord(obj, all);
    return all;
  }

  /**
   * Sorts the list of {@link EventData} records according to the order specified in the
   * {@link oogasalad.editor.model.data.object.event.EventData} of the {@link EditorObject}.
   * Events not found in the specified order are placed at the end.
   *
   * @param obj the {@link EditorObject} providing the sort order.
   * @param all the list of {@link EventData} records to be sorted.
   */
  private static void sortEventRecord(EditorObject obj, List<EventData> all) {
    List<String> order = obj.getEventData().getEvents();
    if (order == null || order.isEmpty()) {
      // No specific order defined, leave as is or sort alphabetically?
      Collections.sort(all, (a, b) -> a.eventId().compareTo(b.eventId())); // Example: Sort alphabetically
      return;
    }

    Map<String, Integer> indexMap = new HashMap<>();
    for (int i = 0; i < order.size(); i++) {
      indexMap.put(order.get(i), i);
    }

    all.sort((a, b) -> {
      int ia = indexMap.getOrDefault(a.eventId(), Integer.MAX_VALUE);
      int ib = indexMap.getOrDefault(b.eventId(), Integer.MAX_VALUE);
      return Integer.compare(ia, ib);
    });
  }

  /**
   * Helper method to convert and add events of a specific type (e.g., "Collision") from the
   * source map into the destination list of {@link EventData} records.
   *
   * @param type the type string to assign to the created {@link EventData} records (e.g., "Collision", "Input").
   * @param src the source map of event IDs to {@link EditorEvent} objects.
   * @param dest the destination list to add the converted {@link EventData} records to.
   */
  private static void addEvents(
      String type,
      Map<String, EditorEvent> src,
      List<EventData> dest) {
    if (src == null) return;
    for (var entry : src.entrySet()) {
      String eventId = entry.getKey();
      EditorEvent ev = entry.getValue();
      if (ev == null) continue; // Skip if event is null
      List<List<ConditionData>> condGroups = getConditionLists(ev);
      List<OutcomeData> outList = getOutcomesLists(ev);
      dest.add(new EventData(type, eventId, condGroups, outList));
    }
  }

  /**
   * Converts the nested list structure of conditions from an {@link EditorEvent} (using {@link ExecutorData})
   * into the format required by {@link EventData} (using {@link ConditionData}).
   *
   * @param ev the {@link EditorEvent} containing the conditions.
   * @return a list of lists of {@link ConditionData} records.
   */
  private static List<List<ConditionData>> getConditionLists(EditorEvent ev) {
    List<List<ConditionData>> condGroups = new ArrayList<>();
    if (ev.getConditions() == null) return condGroups; // Handle null conditions list

    for (List<ExecutorData> edGroup : ev.getConditions()) {
      if (edGroup == null) continue; // Skip null condition groups
      List<ConditionData> group = new ArrayList<>();
      for (ExecutorData ex : edGroup) {
        if (ex == null) continue; // Skip null executors within a group
        group.add(new ConditionData(
            ex.getExecutorName(),
            ex.getStringParams() != null ? new HashMap<>(ex.getStringParams()) : new HashMap<>(),
            ex.getDoubleParams() != null ? new HashMap<>(ex.getDoubleParams()) : new HashMap<>()
        ));
      }
      condGroups.add(group);
    }
    return condGroups;
  }

  /**
   * Converts the list of outcomes from an {@link EditorEvent} (using {@link ExecutorData})
   * into the format required by {@link EventData} (using {@link OutcomeData}).
   *
   * @param ev the {@link EditorEvent} containing the outcomes.
   * @return a list of {@link OutcomeData} records.
   */
  private static List<OutcomeData> getOutcomesLists(EditorEvent ev) {
    List<OutcomeData> outList = new ArrayList<>();
    if (ev.getOutcomes() == null) return outList; // Handle null outcomes list

    for (ExecutorData ex : ev.getOutcomes()) {
      if (ex == null) continue; // Skip null executors
      outList.add(new OutcomeData(
          ex.getExecutorName(),
          ex.getStringParams() != null ? new HashMap<>(ex.getStringParams()) : new HashMap<>(),
          ex.getDoubleParams() != null ? new HashMap<>(ex.getDoubleParams()) : new HashMap<>()
      ));
    }
    return outList;
  }
}