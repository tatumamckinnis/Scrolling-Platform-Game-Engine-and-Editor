package oogasalad.editor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.OutcomeData;
import oogasalad.fileparser.records.SpriteData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to populate an EditorObject with data from a BlueprintData record. This is
 * essentially the inverse operation of BlueprintBuilder.
 *
 * @author Tatum McKinnis
 */
public class EditorObjectPopulator {

  private static final Logger LOG = LogManager.getLogger(EditorObjectPopulator.class);

  /**
   * Populates the target EditorObject with data from the source BlueprintData. Note: Does NOT set
   * the object's ID or Name, as these should be unique within the level context. Does NOT set X/Y
   * position, as that's determined by placement.
   *
   * @param target The EditorObject to populate.
   * @param source The BlueprintData containing the source information.
   * @param api    The EditorDataAPI to interact with the object's data managers.
   */
  public static void populateFromBlueprint(EditorObject target, BlueprintData source,
      EditorDataAPI api) {
    if (target == null || source == null || api == null) {
      LOG.error("Cannot populate EditorObject from blueprint: null arguments provided.");
      return;
    }

    UUID targetId = target.getId();
    LOG.debug("Populating EditorObject {} from Blueprint ID {}", targetId, source.blueprintId());

    try {
      populateIdentity(targetId, source, api);
      populateSprite(target, source, api);
      populateHitbox(targetId, source, api);
      populateEvents(target, targetId, source, api);
      populateProperties(target, source, api);

    } catch (Exception e) {
      LOG.error("Error populating EditorObject {} from blueprint: {}", targetId, e.getMessage(),
          e);
    }
  }


  private static void populateIdentity(UUID targetId, BlueprintData source, EditorDataAPI api) {
    api.getIdentityDataAPI().setGroup(targetId, source.group());
    api.getIdentityDataAPI().setType(targetId, source.type());
    if (api.getLevel() != null && !api.getLevel().getLayers().isEmpty()) {
      api.getIdentityDataAPI().setLayer(targetId, api.getLevel().getLayers().get(0).getName());
    } else {
      LOG.warn("Could not set layer for object {} from prefab: No layers found in level.",
          targetId);
    }
  }

  private static void populateSprite(EditorObject target, BlueprintData source,
      EditorDataAPI api) {
    SpriteData sourceSprite = source.spriteData();
    if (sourceSprite == null) {
      LOG.warn("No sprite data found in blueprint {}. Skipping sprite population.",
          source.blueprintId());
      return;
    }

    UUID targetId = target.getId();
    api.getSpriteDataAPI().setName(targetId, sourceSprite.name());
    api.getSpriteDataAPI()
        .setSpritePath(targetId, safeGetPath(sourceSprite.spriteFile()));
    api.getSpriteDataAPI().setRotation(targetId, source.rotation());

    populateFrames(target.getSpriteData(), sourceSprite.frames());
    populateAnimations(target.getSpriteData(), sourceSprite.animations());
    setBaseFrame(target.getSpriteData(), sourceSprite.baseFrame());
  }

  private static String safeGetPath(File file) {
    return (file != null) ? file.getPath() : "";
  }

  private static void populateFrames(oogasalad.editor.model.data.object.sprite.SpriteData targetSprite,
      List<oogasalad.fileparser.records.FrameData> sourceFrames) {
    targetSprite.getFrames().clear();
    if (sourceFrames != null) {
      sourceFrames.forEach(frame -> {
        if (frame != null) {
          targetSprite.addFrame(frame.name(),
              new oogasalad.editor.model.data.object.sprite.FrameData(
                  frame.name(), frame.x(), frame.y(), frame.width(), frame.height()
              ));
        }
      });
    }
  }

  private static void populateAnimations(
      oogasalad.editor.model.data.object.sprite.SpriteData targetSprite,
      List<oogasalad.fileparser.records.AnimationData> sourceAnimations) {
    targetSprite.getAnimations().clear();
    if (sourceAnimations != null) {
      sourceAnimations.forEach(anim -> {
        if (anim != null) {
          targetSprite.addAnimation(anim.name(),
              new AnimationData(
                  anim.name(), anim.frameLength(), anim.frameNames()
              ));
        }
      });
    }
  }

  private static void setBaseFrame(oogasalad.editor.model.data.object.sprite.SpriteData targetSprite,
      oogasalad.fileparser.records.FrameData sourceBaseFrame) {
    String baseFrameName = null;
    if (sourceBaseFrame != null) {
      baseFrameName = sourceBaseFrame.name();
    } else if (!targetSprite.getFrames().isEmpty()) {
      baseFrameName = targetSprite.getFrames().keySet().iterator().next();
    }

    if (baseFrameName != null) {
      targetSprite.setBaseFrame(baseFrameName);
    } else {
      LOG.warn("Could not determine a base frame for sprite.");
    }
  }


  private static void populateHitbox(UUID targetId, BlueprintData source, EditorDataAPI api) {
    HitBoxData sourceHitbox = source.hitBoxData();
    if (sourceHitbox != null) {
      api.getHitboxDataAPI().setShape(targetId, sourceHitbox.shape());
      api.getHitboxDataAPI().setWidth(targetId, sourceHitbox.hitBoxWidth());
      api.getHitboxDataAPI().setHeight(targetId, sourceHitbox.hitBoxHeight());
    } else {
      LOG.warn("No hitbox data found in blueprint {}. Skipping hitbox population.",
          source.blueprintId());
    }
  }

  private static void populateEvents(EditorObject target, UUID targetId, BlueprintData source,
      EditorDataAPI api) {

    target.getInputData().getEvents().clear();
    target.getCollisionData().getEvents().clear();
    target.getPhysicsData().getEvents().clear();
    target.getCustomEventData().getEvents().clear();
    target.getEventData().getEvents().clear();

    if (source.eventDataList() == null) {
      return;
    }

    for (EventData sourceEvent : source.eventDataList()) {
      if (sourceEvent == null) {
        continue;
      }
      EditorEvent editorEvent = convertToEditorEvent(sourceEvent);
      String eventId = sourceEvent.eventId();
      String eventType = sourceEvent.type();

      if (eventId == null || eventType == null) {
        LOG.warn("Skipping event with null ID or type from blueprint.");
        continue;
      }

      boolean eventAdded = addEventToCorrectManager(targetId, eventId, eventType, editorEvent,
          api);

      if (eventAdded) {
        target.getEventData().addEvent(eventId);
      }
    }
  }


  private static boolean addEventToCorrectManager(UUID targetId, String eventId, String eventType,
      EditorEvent editorEvent, EditorDataAPI api) {
    switch (eventType.toLowerCase()) {
      case "input":
        api.getInputDataAPI().addEvent(targetId, eventId);
        api.getInputDataAPI().setEvent(targetId, eventId, editorEvent);
        return true;
      case "collision":
        api.getCollisionDataAPI().addEvent(targetId, eventId);
        api.getCollisionDataAPI().setEvent(targetId, eventId, editorEvent);
        return true;
      case "physics":
        api.getPhysicsDataAPI().addEvent(targetId, eventId);
        api.getPhysicsDataAPI().setEvent(targetId, eventId, editorEvent);
        return true;
      case "custom":
        api.getCustomEventDataAPI().addEvent(targetId, eventId);
        api.getCustomEventDataAPI().setEvent(targetId, eventId, editorEvent);
        return true;
      default:
        LOG.warn("Unknown event type '{}' encountered for blueprint event '{}'", eventType,
            eventId);
        return false;
    }
  }


  private static void populateProperties(EditorObject target, BlueprintData source,
      EditorDataAPI api) {
    // TODO: Implement setting string/double properties on the target EditorObject
    // Requires methods like target.setStringProperty(key, value) or similar on EditorObject or its data managers.
    if (source.stringProperties() != null) {
      source.stringProperties().forEach((key, value) -> {
        LOG.trace("Setting string property from blueprint: {} = {}", key, value);
        // target.getSomeDataManager().setStringProperty(target.getId(), key, value); // Example
      });
    }
    if (source.doubleProperties() != null) {
      source.doubleProperties().forEach((key, value) -> {
        LOG.trace("Setting double property from blueprint: {} = {}", key, value);
        // target.getSomeDataManager().setDoubleProperty(target.getId(), key, value); // Example
      });
    }
    if (source.stringProperties() == null && source.doubleProperties() == null) {
      LOG.trace("No custom properties found in blueprint {}.", source.blueprintId());
    } else {
      LOG.warn(
          "Setting custom string/double properties from blueprint {} requires target object methods.",
          source.blueprintId());
    }
  }


  /**
   * Converts a file-based EventData record into an editor-based EditorEvent object.
   *
   * @param sourceEvent The EventData record from the file/blueprint.
   * @return An EditorEvent object populated with conditions and outcomes.
   */
  private static EditorEvent convertToEditorEvent(EventData sourceEvent) {
    EditorEvent editorEvent = new EditorEvent();
    convertAndAddConditions(editorEvent, sourceEvent.conditionGroups());
    convertAndAddOutcomes(editorEvent, sourceEvent.outcomes());
    return editorEvent;
  }

  /**
   * Helper method to convert condition groups from BlueprintData format to EditorEvent format.
   */
  private static void convertAndAddConditions(EditorEvent editorEvent,
      List<List<ConditionData>> sourceConditionGroups) {
    if (sourceConditionGroups == null) {
      return;
    }
    for (List<ConditionData> sourceGroup : sourceConditionGroups) {
      if (sourceGroup == null) {
        editorEvent.addConditionGroup(new ArrayList<>());
        continue;
      }
      List<ExecutorData> editorGroup = sourceGroup.stream()
          .filter(Objects::nonNull)
          .map(sourceCond -> new ExecutorData(
              sourceCond.conditionType(),
              new HashMap<>(sourceCond.stringParams()),
              new HashMap<>(sourceCond.doubleParams())
          ))
          .collect(Collectors.toList());
      editorEvent.addConditionGroup(editorGroup);
    }
  }

  /**
   * Helper method to convert outcomes from BlueprintData format to EditorEvent format.
   */
  private static void convertAndAddOutcomes(EditorEvent editorEvent,
      List<OutcomeData> sourceOutcomes) {
    if (sourceOutcomes == null) {
      return;
    }
    for (OutcomeData sourceOutcome : sourceOutcomes) {
      if (sourceOutcome != null) {
        editorEvent.addOutcome(new ExecutorData(
            sourceOutcome.outcomeType(),
            new HashMap<>(sourceOutcome.stringParams()),
            new HashMap<>(sourceOutcome.doubleParams())
        ));
      }
    }
  }
}