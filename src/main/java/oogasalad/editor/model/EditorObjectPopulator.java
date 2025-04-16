package oogasalad.editor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
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
 * Utility class to populate an EditorObject with data from a BlueprintData record.
 * This is essentially the inverse operation of BlueprintBuilder.
 *
 * @author Tatum McKinnis
 */
public class EditorObjectPopulator {

  private static final Logger LOG = LogManager.getLogger(EditorObjectPopulator.class);

  /**
   * Populates the target EditorObject with data from the source BlueprintData.
   * Note: Does NOT set the object's ID or Name, as these should be unique within the level context.
   * Does NOT set X/Y position, as that's determined by placement.
   *
   * @param target The EditorObject to populate.
   * @param source The BlueprintData containing the source information.
   * @param api    The EditorDataAPI to interact with the object's data managers.
   */
  public static void populateFromBlueprint(EditorObject target, BlueprintData source, EditorDataAPI api) {
    if (target == null || source == null || api == null) {
      LOG.error("Cannot populate EditorObject from blueprint: null arguments provided.");
      return;
    }

    UUID targetId = target.getId();
    LOG.debug("Populating EditorObject {} from Blueprint ID {}", targetId, source.blueprintId());

    try {
      api.getIdentityDataAPI().setGroup(targetId, source.group());
      api.getIdentityDataAPI().setType(targetId, source.type());
      if (api.getLevel() != null && !api.getLevel().getLayers().isEmpty()) {
        api.getIdentityDataAPI().setLayer(targetId, api.getLevel().getLayers().get(0).getName());
      } else {
        LOG.warn("Could not set layer for object {} from prefab: No layers found in level.", targetId);
      }

      SpriteData sourceSprite = source.spriteData();
      if (sourceSprite != null) {
        api.getSpriteDataAPI().setName(targetId, sourceSprite.name());
        api.getSpriteDataAPI().setSpritePath(targetId, sourceSprite.spriteFile().getPath());
        api.getSpriteDataAPI().setRotation(targetId, source.rotation());

        target.getSpriteData().getFrames().clear();
        if (sourceSprite.frames() != null) {
          sourceSprite.frames().forEach(frame ->
              target.getSpriteData().addFrame(frame.name(),
                  new oogasalad.editor.model.data.object.sprite.FrameData(
                      frame.name(), frame.x(), frame.y(), frame.width(), frame.height()
                  ))
          );
        }
        String baseFrameName = null;
        if(sourceSprite.baseFrame() != null) {
          baseFrameName = sourceSprite.baseFrame().name();
        } else if (!target.getSpriteData().getFrames().isEmpty()){
          baseFrameName = target.getSpriteData().getFrames().keySet().iterator().next();
        }
        if (baseFrameName != null) {
          api.getSpriteDataAPI().setBaseFrame(targetId, baseFrameName);
        }

        target.getSpriteData().getAnimations().clear();
        if (sourceSprite.animations() != null) {
          sourceSprite.animations().forEach(anim ->
              target.getSpriteData().addAnimation(anim.name(),
                  new AnimationData(
                      anim.name(), anim.frameLength(), anim.frameNames()
                  ))
          );
        }
      }

      HitBoxData sourceHitbox = source.hitBoxData();
      if (sourceHitbox != null) {
        api.getHitboxDataAPI().setShape(targetId, sourceHitbox.shape());
        api.getHitboxDataAPI().setWidth(targetId, sourceHitbox.hitBoxWidth());
        api.getHitboxDataAPI().setHeight(targetId, sourceHitbox.hitBoxHeight());
      }

      target.getInputData().getEvents().clear();
      target.getCollisionData().getEvents().clear();
      target.getPhysicsData().getEvents().clear();
      target.getCustomEventData().getEvents().clear();
      target.getEventData().getEvents().clear();

      if (source.eventDataList() != null) {
        for (EventData sourceEvent : source.eventDataList()) {
          if (sourceEvent == null) continue;
          EditorEvent editorEvent = convertToEditorEvent(sourceEvent);
          String eventId = sourceEvent.eventId();
          String eventType = sourceEvent.type();

          if (eventId == null || eventType == null) {
            LOG.warn("Skipping event with null ID or type from blueprint.");
            continue;
          }

          boolean eventAdded = false;
          switch (eventType.toLowerCase()) {
            case "input":
              api.getInputDataAPI().addEvent(targetId, eventId);
              api.getInputDataAPI().setEvent(targetId, eventId, editorEvent);
              eventAdded = true;
              break;
            case "collision":
              api.getCollisionDataAPI().addEvent(targetId, eventId);
              api.getCollisionDataAPI().setEvent(targetId, eventId, editorEvent);
              eventAdded = true;
              break;
            case "physics":
              api.getPhysicsDataAPI().addEvent(targetId, eventId);
              api.getPhysicsDataAPI().setEvent(targetId, eventId, editorEvent);
              eventAdded = true;
              break;
            case "custom":
              api.getCustomEventDataAPI().addEvent(targetId, eventId);
              api.getCustomEventDataAPI().setEvent(targetId, eventId, editorEvent);
              eventAdded = true;
              break;
            default:
              LOG.warn("Unknown event type '{}' encountered for blueprint event '{}'", eventType, eventId);
              break;
          }
          if (eventAdded) {
            target.getEventData().addEvent(eventId);
          }
        }
      }

      if (source.stringProperties() != null) {
        source.stringProperties().forEach((key, value) -> {
          LOG.trace("Setting string property from blueprint: {} = {}", key, value);
        });
      }
      if (source.doubleProperties() != null) {
        source.doubleProperties().forEach((key, value) -> {
          LOG.trace("Setting double property from blueprint: {} = {}", key, value);
        });
      }
      if (source.stringProperties() == null && source.doubleProperties() == null) {
        LOG.trace("No custom properties found in blueprint.");
      } else {
        LOG.warn("Setting custom string/double properties from blueprint requires target object methods (e.g., setStringProperty).");
      }

    } catch (Exception e) {
      LOG.error("Error populating EditorObject {} from blueprint: {}", targetId, e.getMessage(), e);
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

    if (sourceEvent.conditionGroups() != null) {
      for (List<ConditionData> sourceGroup : sourceEvent.conditionGroups()) {
        List<ExecutorData> editorGroup = new ArrayList<>();
        if (sourceGroup != null) {
          for (ConditionData sourceCond : sourceGroup) {
            if (sourceCond == null) continue;
            editorGroup.add(new ExecutorData(
                sourceCond.conditionType(),
                new HashMap<>(sourceCond.stringParams()),
                new HashMap<>(sourceCond.doubleParams())
            ));
          }
        }
        editorEvent.addConditionGroup(editorGroup);
      }
    }

    if (sourceEvent.outcomes() != null) {
      for (OutcomeData sourceOutcome : sourceEvent.outcomes()) {
        if (sourceOutcome == null) continue;
        editorEvent.addOutcome(new ExecutorData(
            sourceOutcome.outcomeType(),
            new HashMap<>(sourceOutcome.stringParams()),
            new HashMap<>(sourceOutcome.doubleParams())
        ));
      }
    }
    return editorEvent;
  }
}