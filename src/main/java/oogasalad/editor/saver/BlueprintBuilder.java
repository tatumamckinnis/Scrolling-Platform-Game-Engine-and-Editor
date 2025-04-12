package oogasalad.editor.saver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;

public class BlueprintBuilder {

  private static final int BLUEPRINT_PLACEHOLDER_ID = -1;

  public static BlueprintData fromEditorObject(EditorObject obj) {
    String gameName = obj.getIdentityData().getName();
    String group = "";
    String type = obj.getIdentityData().getType();
    double rotation = obj.getSpriteData().getRotation();

    SpriteData spriteData = toSpriteRecord(obj.getSpriteData());
    HitBoxData hitBoxData = toHitboxRecord(obj.getHitboxData(), obj.getSpriteData());
    List<EventData> eventData = toEventRecord(obj.getEventData());

    // Add to the saver upon implementation in the editor.
    double vX = 0.0;
    double vY = 0.0;
    Map<String, String> stringProps = Map.of();
    Map<String, Double> doubleProps = Map.of();
    List<String> displayList = List.of();

    return new BlueprintData(
        BLUEPRINT_PLACEHOLDER_ID,
        vX, vY, rotation,
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
      frames.add(new FrameData(name, editorFrame.x(), editorFrame.y(), editorFrame.width(),
          editorFrame.height()));
    });

    List<AnimationData> animations = new ArrayList<>();
    editorSpriteData.getAnimations().forEach((name, editorAnimation) -> {
      animations.add(new AnimationData(name, editorAnimation.getFrameLength(),
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

  private static HitBoxData toHitboxRecord(HitboxData editorHitboxData,
      oogasalad.editor.model.data.object.sprite.SpriteData editorSpriteData) {
    return new HitBoxData(
        editorHitboxData.getShape(),
        editorHitboxData.getWidth(),
        editorHitboxData.getHeight(),
        editorSpriteData.getX() - editorHitboxData.getX(),
        editorSpriteData.getY() - editorHitboxData.getY()
    );
  }

  private static List<EventData> toEventRecordList(EditorObject obj) {
    List<EventData> allEvents = new ArrayList<>();
    addEventsFromMap("Collision", obj.getCollisionData().getEvents(), allEvents);
    addEventsFromMap("Input", obj.getInputData().getEvents(), allEvents);
    addEventsFromMap("Physics", obj.getPhysicsData().getEvents(), allEvents);
    addEventsFromMap("Custom", obj.getCustomEventData().getEvents(), allEvents);
    return allEvents;
  }

  private static Collection<EventData> addEventsFromMap(String type, Map<String, EditorEvent> events, List<EventData> allEvents) {
    for (Map.Entry<String, EditorEvent> entry : events.entrySet()) {
      String eventName = entry.getKey();
      EditorEvent event = entry.getValue();

      List<List<ConditionData>> conditionGroups = new ArrayList<>();
      for (ConditionType conditionType : ConditionType.values()) {
        parseConditions(conditionGroups, conditionType);
      }
    }
  }

  private static void parseConditions(List<List<ConditionData>> conditionGroups, ConditionType conditionType) {
    List<ConditionData> singleGroup = new ArrayList<>();

    Map<String, String> stringProps = Map.of();
    Map<String, Double> doubleProps = Map.of();

  }
}
