package oogasalad.editor.model.saver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.EditorObject;
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
 * Builds {@link BlueprintData} records from in‑editor objects.
 * <p>
 * This class translates an {@link EditorObject}'s identity, sprite, hitbox, and event data
 * into the {@code BlueprintData} format required by the file parser API.
 * </p>
 *
 * @author Jacob You
 */
public class BlueprintBuilder {

  private static final int BLUEPRINT_PLACEHOLDER_ID = -1;

  /**
   * Converts the given editor object into a {@link BlueprintData} instance.
   *
   * @param obj the editor object to convert
   * @return a BlueprintData record representing the object's state
   */
  public static BlueprintData fromEditorObject(EditorObject obj) {
    String gameName = obj.getIdentityData().getName();
    String group = "";  // placeholder, replace with actual grouping logic if needed
    String type = obj.getIdentityData().getType();
    double rotation = obj.getSpriteData().getRotation();
    boolean isFlipped = obj.getSpriteData().getIsFlipped();
    SpriteData spriteData = toSpriteRecord(obj.getSpriteData());
    HitBoxData hitBoxData = toHitboxRecord(obj.getHitboxData(), obj.getSpriteData());
    List<EventData> eventData = toEventRecord(obj);

    double vX = 0.0;
    double vY = 0.0;
    Map<String, String> stringProps = Map.of();
    Map<String, Double> doubleProps = Map.of();
    List<String> displayList = List.of();

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

    FrameData baseFrame = frames.isEmpty() ? null : frames.get(0);

    return new SpriteData(
        editorSpriteData.getName(),
        new File(editorSpriteData.getSpritePath()),
        baseFrame,
        frames,
        animations
    );
  }

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

  private static List<EventData> toEventRecord(EditorObject obj) {
    List<EventData> all = new ArrayList<>();

    addEvents("Collision", obj.getCollisionData().getEvents(), all);
    addEvents("Input",     obj.getInputData().getEvents(),     all);
    addEvents("Physics",   obj.getPhysicsData().getEvents(),   all);
    addEvents("Custom",    obj.getCustomEventData().getEvents(), all);

    sortEventRecord(obj, all);
    return all;
  }

  private static void sortEventRecord(EditorObject obj, List<EventData> all) {
    List<String> order = obj.getEventData().getEvents();
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

  private static void addEvents(
      String type,
      Map<String, EditorEvent> src,
      List<EventData> dest) {
    for (var entry : src.entrySet()) {
      String eventId = entry.getKey();
      EditorEvent ev = entry.getValue();
      List<List<ConditionData>> condGroups = getConditionLists(ev);
      List<OutcomeData> outList = getOutcomesLists(ev);
      dest.add(new EventData(type, eventId, condGroups, outList));
    }
  }

  private static List<List<ConditionData>> getConditionLists(EditorEvent ev) {
    List<List<ConditionData>> condGroups = new ArrayList<>();
    for (List<ExecutorData> edGroup : ev.getConditions()) {
      List<ConditionData> group = new ArrayList<>();
      for (ExecutorData ex : edGroup) {
        group.add(new ConditionData(
            ex.getExecutorName(),
            new HashMap<>(ex.getStringParams()),
            new HashMap<>(ex.getDoubleParams())
        ));
      }
      condGroups.add(group);
    }
    return condGroups;
  }

  private static List<OutcomeData> getOutcomesLists(EditorEvent ev) {
    List<OutcomeData> outList = new ArrayList<>();
    for (ExecutorData ex : ev.getOutcomes()) {
      outList.add(new OutcomeData(
          ex.getExecutorName(),
          new HashMap<>(ex.getStringParams()),
          new HashMap<>(ex.getDoubleParams())
      ));
    }
    return outList;
  }
}