package oogasalad.editor.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.event.PhysicsData;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class responsible for populating an EditorObject with data.
 *
 * @author Tatum McKinnis
 */
public class EditorObjectPopulator {

  private static final Logger LOG = LogManager.getLogger(EditorObjectPopulator.class);
  private final EditorLevelData levelData;


  /**
   * Constructs an EditorObjectPopulator associated with a specific level.
   */
  public EditorObjectPopulator(EditorLevelData levelData) {
    this.levelData = Objects.requireNonNull(levelData, "levelData cannot be null");
    LOG.info("EditorObjectPopulator initialized.");
  }

  /**
   * Creates a new default EditorObject and registers it.
   */
  public EditorObject createDefaultObject() {
    LOG.debug("Creating default EditorObject.");
    EditorObject newObject = new EditorObject(levelData);
    levelData.getObjectDataMap().put(newObject.getId(), newObject);
    levelData.getObjectLayerDataMap().computeIfAbsent(newObject.getIdentityData().getLayer(), k -> new ArrayList<>()).add(newObject);
    LOG.info("Default EditorObject created and registered with ID: {}", newObject.getId());
    return newObject;
  }

  /**
   * Creates and populates an EditorObject from a BlueprintData record.
   * Assumes the BlueprintData's spriteData record contains resolved image path and parsed frame/animation data.
   * Registers the new object with EditorLevelData.
   */
  public EditorObject populateFromBlueprint(BlueprintData blueprint, double x, double y) {
    LOG.debug("Populating EditorObject from blueprint ID: {}, Type: {}", blueprint.blueprintId(), blueprint.type());
    EditorObject object = new EditorObject(levelData);

    IdentityData identity = new IdentityData(
        object.getId(), blueprint.type(), blueprint.group(), levelData.getFirstLayer()
    );
    object.setIdentityData(identity);


    String resolvedImagePath = "";
    oogasalad.fileparser.records.SpriteData recordSprite = blueprint.spriteData();
    if (recordSprite != null && recordSprite.spriteFile() != null && !recordSprite.spriteFile().getPath().isEmpty()) {

      resolvedImagePath = recordSprite.spriteFile().getPath();
      LOG.debug("Using image path from blueprint record: {}", resolvedImagePath);
      if (!new File(resolvedImagePath).exists()) {
        LOG.warn("Image path from blueprint record does not exist: {}", resolvedImagePath);

        resolvedImagePath = "";
      }
    } else {
      LOG.warn("Blueprint {} spriteData or spriteFile is null/empty.", blueprint.blueprintId());
    }
    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = convertRecordToModelSpriteData(
        recordSprite, resolvedImagePath, x, y, blueprint.rotation(), blueprint.isFlipped()
    );
    object.setSpriteData(modelSpriteData);


    if (blueprint.hitBoxData() != null) {
      oogasalad.fileparser.records.HitBoxData bpHD = blueprint.hitBoxData();
      int hitboxX = (int) (x + bpHD.spriteDx());
      int hitboxY = (int) (y + bpHD.spriteDy());
      oogasalad.editor.model.data.object.HitboxData modelHitbox = new oogasalad.editor.model.data.object.HitboxData(
          hitboxX, hitboxY, bpHD.hitBoxWidth(), bpHD.hitBoxHeight(), bpHD.shape()
      );
      object.setHitboxData(modelHitbox);
    } else {
      HitboxData defaultHitbox = object.getHitboxData();
      defaultHitbox.setX((int) x);
      defaultHitbox.setY((int) y);
      LOG.warn("Blueprint {} has no HitBoxData, using default position for default hitbox.", blueprint.blueprintId());
    }


    PhysicsData physics = object.getPhysicsData();
    Map<String, Double> doubleProps = blueprint.doubleProperties();
    if (doubleProps != null) {
      physics.setVelocityX(blueprint.velocityX());
      physics.setVelocityY(blueprint.velocityY());
      trySetPhysicsProperty(physics, "gravity", doubleProps.get("gravity"));
      trySetPhysicsProperty(physics, "jump_force", doubleProps.get("jump_force"));
    }



    LOG.info("Populated EditorObject {} from blueprint {}", object.getId(), blueprint.blueprintId());


    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().computeIfAbsent(identity.getLayer(), k -> new ArrayList<>()).removeIf(obj -> obj.getId().equals(object.getId()));
    levelData.getObjectLayerDataMap().computeIfAbsent(identity.getLayer(), k -> new ArrayList<>()).add(object);

    return object;
  }


  private void trySetPhysicsProperty(PhysicsData physics, String propertyName, Double value) {
    if (value == null) return;
    try {
      switch (propertyName) {
        case "gravity": physics.setGravity(value); break;
        case "jump_force": physics.setJumpForce(value); break;
        default: LOG.trace("Physics property '{}' not handled.", propertyName); break;
      }
    } catch (Exception e) {
      LOG.warn("Could not set physics property '{}': {}", propertyName, e.getMessage());
    }
  }

  /**
   * Converts a SpriteData record into a SpriteData model object.
   * Casts position to int and sets base frame name as String.
   * Uses the explicitly provided imagePath. Attempts heuristic for base frame name if needed.
   */
  private oogasalad.editor.model.data.object.sprite.SpriteData convertRecordToModelSpriteData(
      oogasalad.fileparser.records.SpriteData recordSprite, String imagePath,
      double x, double y, double rotation, boolean isFlipped) {

    if (recordSprite == null) {
      LOG.warn("Input SpriteData record is null. Using default sprite data.");
      return createDefaultModelSpriteData(x, y);
    }

    FrameData baseFrameModel = convertRecordToModelFrame(recordSprite.baseFrame());

    Map<String, FrameData> frameMapModel = recordSprite.frames().stream()
        .map(this::convertRecordToModelFrame)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(FrameData::name, frame -> frame, (e, r) -> r));

    Map<String, AnimationData> animationMapModel = recordSprite.animations().stream()
        .map(this::convertRecordToModelAnimation)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(AnimationData::getName, anim -> anim, (e, r) -> r));

    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = new oogasalad.editor.model.data.object.sprite.SpriteData(
        recordSprite.name(), (int) x, (int) y, rotation, isFlipped,
        frameMapModel, animationMapModel, imagePath
    );


    String baseFrameNameToSet = null;
    if (baseFrameModel != null) {
      baseFrameNameToSet = baseFrameModel.name();
    } else if (!frameMapModel.isEmpty()) {
      baseFrameNameToSet = recordSprite.name();
      if (!frameMapModel.containsKey(baseFrameNameToSet)) {
        baseFrameNameToSet = frameMapModel.values().iterator().next().name();
        LOG.warn("Heuristic base frame name '{}' not found in frame map for sprite '{}'. Using first frame name '{}' instead.",
            recordSprite.name(), recordSprite.name(), baseFrameNameToSet);
      }
      LOG.debug("Setting base frame name heuristically to '{}' for sprite '{}'", baseFrameNameToSet, recordSprite.name());
    } else {

      LOG.warn("No base frame record and no frames found for sprite record '{}'. Cannot set base frame name.", recordSprite.name());
    }
    modelSpriteData.setBaseFrameName(baseFrameNameToSet);


    LOG.info("Converted sprite record '{}': Image='{}', BaseFrame='{}', Frames={}, Animations={}",
        recordSprite.name(), imagePath, modelSpriteData.getFrame(recordSprite.name()),
        frameMapModel.size(), animationMapModel.size());
    return modelSpriteData;
  }

  private FrameData convertRecordToModelFrame(oogasalad.fileparser.records.FrameData recordFrame) {
    if (recordFrame == null) return null;
    return new FrameData(recordFrame.name(), recordFrame.x(), recordFrame.y(), recordFrame.width(), recordFrame.height());
  }

  private AnimationData convertRecordToModelAnimation(oogasalad.fileparser.records.AnimationData recordAnimation) {
    if (recordAnimation == null) return null;
    List<String> frameNames = (recordAnimation.frameNames() != null) ? new ArrayList<>(recordAnimation.frameNames()) : new ArrayList<>();
    return new AnimationData(recordAnimation.name(), recordAnimation.frameLen(), frameNames);
  }

  private oogasalad.editor.model.data.object.sprite.SpriteData createDefaultModelSpriteData(double x, double y) {
    return new oogasalad.editor.model.data.object.sprite.SpriteData(
        "Default", (int) x, (int) y, 0, false, Map.of(), Map.of(), ""
    );
  }


  /**
   * Creates and populates an EditorObject from a GameObjectData record, using a map
   * of blueprints to retrieve template information. Correctly handles Layer assignment.
   * Registers the loaded object with EditorLevelData.
   */
  public EditorObject populateFromGameObjectData(GameObjectData gameObjectData, Map<Integer, BlueprintData> blueprintMap) {
    LOG.debug("Populating EditorObject from GameObjectData ID: {}, BlueprintID: {}", gameObjectData.uniqueId(), gameObjectData.blueprintId());
    EditorObject object = new EditorObject(levelData);

    BlueprintData blueprint = blueprintMap.get(gameObjectData.blueprintId());
    if (blueprint == null) {
      LOG.error("BlueprintData not found for blueprintId {} (GameObjectData {}). Creating minimal error object.",
          gameObjectData.blueprintId(), gameObjectData.uniqueId());
      Layer errorLayer = levelData.getFirstLayer();
      String layerName = gameObjectData.layerName() != null ? gameObjectData.layerName() : errorLayer.getName();
      Layer targetLayer = findLayerByName(layerName, errorLayer);
      IdentityData errorIdentity = new IdentityData(gameObjectData.uniqueId(), "ERROR_NoBlueprint", "ERROR", targetLayer);
      object.setIdentityData(errorIdentity);
      object.setSpriteData(createDefaultModelSpriteData(gameObjectData.x(), gameObjectData.y()));

      levelData.getObjectDataMap().put(object.getId(), object);
      levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>()).add(object);
      return object;
    }


    String layerName = gameObjectData.layerName();
    Layer targetLayer = findLayerByName(layerName, levelData.getFirstLayer());
    IdentityData identity = new IdentityData(
        gameObjectData.uniqueId(), blueprint.type(), blueprint.group(), targetLayer
    );
    object.setIdentityData(identity);


    String resolvedImagePath = "";
    oogasalad.fileparser.records.SpriteData recordSprite = blueprint.spriteData();
    if (recordSprite != null && recordSprite.spriteFile() != null && !recordSprite.spriteFile().getPath().isEmpty()) {
      resolvedImagePath = recordSprite.spriteFile().getPath();
      LOG.debug("Loading GameObject: Using image path from blueprint record: {}", resolvedImagePath);
      if (!new File(resolvedImagePath).exists()) {
        LOG.warn("Image path from blueprint record does not exist when loading GameObject: {}", resolvedImagePath);
        resolvedImagePath = "";
      }
    } else {
      LOG.warn("Blueprint {} for GameObject {} has no spriteData/spriteFile.", blueprint.blueprintId(), gameObjectData.uniqueId());
    }
    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = convertRecordToModelSpriteData(
        recordSprite, resolvedImagePath,
        gameObjectData.x(), gameObjectData.y(),
        blueprint.rotation(), blueprint.isFlipped()
    );
    object.setSpriteData(modelSpriteData);



    if (blueprint.hitBoxData() != null) {
      oogasalad.fileparser.records.HitBoxData bpHitbox = blueprint.hitBoxData();
      int hitboxX = gameObjectData.x() + bpHitbox.spriteDx();
      int hitboxY = gameObjectData.y() + bpHitbox.spriteDy();
      oogasalad.editor.model.data.object.HitboxData modelHitbox = new oogasalad.editor.model.data.object.HitboxData(
          hitboxX, hitboxY, bpHitbox.hitBoxWidth(), bpHitbox.hitBoxHeight(), bpHitbox.shape()
      );
      object.setHitboxData(modelHitbox);
    } else {
      HitboxData defaultHitbox = object.getHitboxData();
      defaultHitbox.setX(gameObjectData.x());
      defaultHitbox.setY(gameObjectData.y());
      LOG.warn("Blueprint {} (GameObject {}) has no HitBoxData.", blueprint.blueprintId(), gameObjectData.uniqueId());
    }


    PhysicsData physics = object.getPhysicsData();
    Map<String, Double> doubleProps = blueprint.doubleProperties();
    if (doubleProps != null) {
      physics.setVelocityX(blueprint.velocityX());
      physics.setVelocityY(blueprint.velocityY());
      trySetPhysicsProperty(physics, "gravity", doubleProps.get("gravity"));
      trySetPhysicsProperty(physics, "jump_force", doubleProps.get("jump_force"));
    }


    LOG.info("Populated EditorObject {} from GameObjectData {} using Blueprint {}",
        gameObjectData.uniqueId(), gameObjectData.blueprintId(), blueprint.blueprintId());


    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>()).removeIf(obj -> obj.getId().equals(object.getId()));
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>()).add(object);

    return object;
  }


  private Layer findLayerByName(String name, Layer defaultLayer) {
    if (name == null || name.trim().isEmpty()) {
      LOG.warn("Layer name is null or empty, using default layer '{}'", defaultLayer.getName());
      return defaultLayer;
    }
    for (Layer layer : levelData.getLayers()) {
      if (layer.getName().equals(name)) {
        return layer;
      }
    }
    LOG.warn("Layer with name '{}' not found in EditorLevelData, using default layer '{}'", name, defaultLayer.getName());
    return defaultLayer;
  }
}