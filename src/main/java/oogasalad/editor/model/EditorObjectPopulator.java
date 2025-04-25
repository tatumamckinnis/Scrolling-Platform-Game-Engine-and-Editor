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
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class responsible for populating an EditorObject with data.
 * Includes logic to ensure groups are added to the level data.
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
   * Registers the new object with EditorLevelData and ensures its group is added globally.
   */
  public EditorObject populateFromBlueprint(BlueprintData blueprint, double x, double y) {
    LOG.debug("Populating EditorObject from blueprint ID: {}, Type: {}", blueprint.blueprintId(), blueprint.type());
    EditorObject object = new EditorObject(levelData);

    Layer targetLayer = levelData.getFirstLayer();

    IdentityData identity = new IdentityData(
        object.getId(),
        blueprint.type(),
        blueprint.group(),
        targetLayer
    );
    object.setIdentityData(identity);

    if (blueprint.group() != null && !blueprint.group().trim().isEmpty()) {
      levelData.addGroup(blueprint.group());
      LOG.debug("Ensured group '{}' exists in level data.", blueprint.group());
    }

    String resolvedImagePath = "";
    oogasalad.fileparser.records.SpriteData recordSprite = blueprint.spriteData();
    if (recordSprite != null && recordSprite.spriteFile() != null && !recordSprite.spriteFile().getPath().isEmpty()) {
      resolvedImagePath = recordSprite.spriteFile().getPath();
      LOG.debug("Using image path from blueprint record: {}", resolvedImagePath);
      if (!new File(resolvedImagePath).isAbsolute() || !new File(resolvedImagePath).exists()) {
        LOG.warn("Image path from blueprint record is relative or does not exist: {}. Sprite might not load.", resolvedImagePath);
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
      LOG.warn("Blueprint {} has no HitBoxData, using default hitbox positioned at ({}, {}).", blueprint.blueprintId(), (int)x, (int)y);
    }

    PhysicsData physics = object.getPhysicsData();
    Map<String, Double> doubleProps = blueprint.doubleProperties();
    physics.setVelocityX(blueprint.velocityX());
    physics.setVelocityY(blueprint.velocityY());
    if (doubleProps != null) {
      trySetPhysicsProperty(physics, "gravity", doubleProps.get("gravity"));
      trySetPhysicsProperty(physics, "jump_force", doubleProps.get("jump_force"));
    }

    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().values().forEach(list -> list.removeIf(obj -> obj.getId().equals(object.getId())));
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>()).add(object);

    LOG.info("Populated EditorObject {} from blueprint {}", object.getId(), blueprint.blueprintId());
    return object;
  }


  /**
   * Helper to safely set physics properties.
   */
  private void trySetPhysicsProperty(PhysicsData physics, String propertyName, Double value) {
    if (value == null) return;
    try {
      switch (propertyName) {
        case "gravity": physics.setGravity(value); break;
        case "jump_force": physics.setJumpForce(value); break;
        default: LOG.trace("Physics property '{}' not handled during population.", propertyName); break;
      }
    } catch (Exception e) {
      LOG.warn("Could not set physics property '{}' with value {}: {}", propertyName, value, e.getMessage());
    }
  }

  /**
   * Converts a SpriteData record into a SpriteData model object.
   * Includes workaround logic for base frame name inconsistency.
   * Prioritizes 'Bird1' if base frame is 'Bird' and 'Bird1' exists.
   * Prioritizes 'big-mario-stand-right' if base frame is 'big-mario' and 'big-mario-stand-right' exists.
   */
  private oogasalad.editor.model.data.object.sprite.SpriteData convertRecordToModelSpriteData(
      oogasalad.fileparser.records.SpriteData recordSprite, String imagePath,
      double x, double y, double rotation, boolean isFlipped) {

    if (recordSprite == null) {
      LOG.warn("Input SpriteData record is null. Using default sprite data.");
      return createDefaultModelSpriteData(x, y);
    }

    oogasalad.editor.model.data.object.sprite.FrameData baseFrameModel = convertRecordToModelFrame(recordSprite.baseFrame());

    Map<String, oogasalad.editor.model.data.object.sprite.FrameData> frameMapModel = recordSprite.frames().stream()
        .map(this::convertRecordToModelFrame)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(oogasalad.editor.model.data.object.sprite.FrameData::name, frame -> frame, (existing, replacement) -> replacement));

    Map<String, AnimationData> animationMapModel = recordSprite.animations().stream()
        .map(this::convertRecordToModelAnimation)
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(AnimationData::getName, anim -> anim, (existing, replacement) -> replacement));

    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = new oogasalad.editor.model.data.object.sprite.SpriteData(
        recordSprite.name(),
        (int) x, (int) y, rotation, isFlipped,
        frameMapModel, animationMapModel, imagePath
    );

    String baseFrameNameToSet = null;
    String baseFrameNameFromRecord = (baseFrameModel != null && baseFrameModel.name() != null && !baseFrameModel.name().isEmpty()) ? baseFrameModel.name() : null;

    if (baseFrameNameFromRecord != null && frameMapModel.containsKey(baseFrameNameFromRecord)) {
      baseFrameNameToSet = baseFrameNameFromRecord;
      LOG.debug("Using exact match base frame name '{}' from blueprint record for sprite '{}'", baseFrameNameToSet, recordSprite.name());
    } else if ("Bird".equals(baseFrameNameFromRecord) && frameMapModel.containsKey("Bird1")) {
      baseFrameNameToSet = "Bird1";
      LOG.warn("Exact base frame name 'Bird' not found. Explicitly using existing frame 'Bird1' as requested for sprite '{}'.", recordSprite.name());
    } else if ("big-mario".equals(baseFrameNameFromRecord) && frameMapModel.containsKey("big-mario-stand-right")) {
      baseFrameNameToSet = "big-mario-stand-right";
      LOG.warn("Exact base frame name 'big-mario' not found. Explicitly using existing frame '{}' as requested for sprite '{}'.", baseFrameNameToSet, recordSprite.name());
    } else if (baseFrameNameFromRecord != null) {
      String foundPrefixMatch = null;
      for (String actualFrameName : frameMapModel.keySet()) {
        if (actualFrameName != null && actualFrameName.startsWith(baseFrameNameFromRecord)) {
          foundPrefixMatch = actualFrameName;
          LOG.warn("Exact base frame name '{}' not found, and special cases didn't apply. Found frame '{}' starting with it. Using this as base frame for sprite '{}'.",
              baseFrameNameFromRecord, foundPrefixMatch, recordSprite.name());
          break;
        }
      }
      baseFrameNameToSet = foundPrefixMatch;
    }

    if (baseFrameNameToSet == null && !frameMapModel.isEmpty()) {
      baseFrameNameToSet = frameMapModel.keySet().iterator().next();
      LOG.warn("Could not determine base frame from record or prefix/special case match for sprite '{}'. Falling back to first available frame name: '{}'",
          recordSprite.name(), baseFrameNameToSet);
    }

    if (baseFrameNameToSet == null && frameMapModel.isEmpty()){
      LOG.warn("No base frame name specified and no frames found for sprite record '{}'. Base frame name remains null.", recordSprite.name());
    }

    modelSpriteData.setBaseFrameName(baseFrameNameToSet);

    LOG.info("Converted sprite record '{}': Image='{}', BaseFrameNameSet='{}', Frames={}, Animations={}",
        recordSprite.name(), imagePath, modelSpriteData.getBaseFrameName(),
        frameMapModel.size(), animationMapModel.size());

    return modelSpriteData;
  }


  /**
   * Converts a FrameData record (from fileparser) to a FrameData model object.
   */
  private oogasalad.editor.model.data.object.sprite.FrameData convertRecordToModelFrame(oogasalad.fileparser.records.FrameData recordFrame) {
    if (recordFrame == null) return null;
    return new oogasalad.editor.model.data.object.sprite.FrameData(recordFrame.name(), recordFrame.x(), recordFrame.y(), recordFrame.width(), recordFrame.height());
  }

  /**
   * Converts an AnimationData record (from fileparser) to an AnimationData model object.
   */
  private AnimationData convertRecordToModelAnimation(oogasalad.fileparser.records.AnimationData recordAnimation) {
    if (recordAnimation == null) return null;
    List<String> frameNames = (recordAnimation.frameNames() != null) ? new ArrayList<>(recordAnimation.frameNames()) : new ArrayList<>();
    return new AnimationData(recordAnimation.name(), recordAnimation.frameLen(), frameNames);
  }

  /**
   * Creates a default SpriteData object for the model.
   */
  private oogasalad.editor.model.data.object.sprite.SpriteData createDefaultModelSpriteData(double x, double y) {
    return new oogasalad.editor.model.data.object.sprite.SpriteData(
        "DefaultSprite", (int) x, (int) y, 0.0, false, Map.of(), Map.of(), ""
    );
  }


  /**
   * Creates and populates an EditorObject from a GameObjectData record, using a map
   * of blueprints to retrieve template information. Correctly handles Layer assignment.
   * Registers the loaded object with EditorLevelData and ensures its group is added globally.
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
      IdentityData errorIdentity = new IdentityData(gameObjectData.uniqueId(), "ERROR_NoBlueprint_" + gameObjectData.uniqueId().toString().substring(0,4), "ERROR", targetLayer);
      object.setIdentityData(errorIdentity);
      object.setSpriteData(createDefaultModelSpriteData(gameObjectData.x(), gameObjectData.y()));
      object.getHitboxData().setX(gameObjectData.x());
      object.getHitboxData().setY(gameObjectData.y());
      levelData.addGroup("ERROR");

      levelData.getObjectDataMap().put(object.getId(), object);
      levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>()).add(object);
      return object;
    }

    String layerName = gameObjectData.layerName();
    Layer defaultLayer = levelData.getFirstLayer();
    Layer targetLayer = findLayerByName(layerName, defaultLayer);

    IdentityData identity = new IdentityData(
        gameObjectData.uniqueId(),
        gameObjectData.uniqueId().toString(),
        blueprint.group(),
        targetLayer
    );
    object.setIdentityData(identity);

    if (blueprint.group() != null && !blueprint.group().trim().isEmpty()) {
      levelData.addGroup(blueprint.group());
      LOG.debug("Ensured group '{}' exists in level data while loading GameObjectData.", blueprint.group());
    }

    String resolvedImagePath = "";
    oogasalad.fileparser.records.SpriteData recordSprite = blueprint.spriteData();
    if (recordSprite != null && recordSprite.spriteFile() != null && !recordSprite.spriteFile().getPath().isEmpty()) {
      resolvedImagePath = recordSprite.spriteFile().getPath();
      LOG.debug("Loading GameObject: Using image path from blueprint record: {}", resolvedImagePath);
      if (!new File(resolvedImagePath).isAbsolute() || !new File(resolvedImagePath).exists()) {
        LOG.warn("Image path from blueprint record is relative or does not exist when loading GameObject: {}. Sprite might not load.", resolvedImagePath);
        resolvedImagePath = "";
      }
    } else {
      LOG.warn("Blueprint {} for GameObject {} has no spriteData/spriteFile.", blueprint.blueprintId(), gameObjectData.uniqueId());
    }
    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = convertRecordToModelSpriteData(
        recordSprite, resolvedImagePath,
        gameObjectData.x(), gameObjectData.y(),
        blueprint.rotation(),
        blueprint.isFlipped()
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
      LOG.warn("Blueprint {} (GameObject {}) has no HitBoxData, using default hitbox positioned at ({}, {}).",
          blueprint.blueprintId(), gameObjectData.uniqueId(), gameObjectData.x(), gameObjectData.y());
    }

    PhysicsData physics = object.getPhysicsData();
    Map<String, Double> doubleProps = blueprint.doubleProperties();
    physics.setVelocityX(blueprint.velocityX());
    physics.setVelocityY(blueprint.velocityY());
    if (doubleProps != null) {
      trySetPhysicsProperty(physics, "gravity", doubleProps.get("gravity"));
      trySetPhysicsProperty(physics, "jump_force", doubleProps.get("jump_force"));
    }

    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().values().forEach(list -> list.removeIf(obj -> obj.getId().equals(object.getId())));
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>()).add(object);

    LOG.info("Populated EditorObject {} from GameObjectData {} using Blueprint {}",
        gameObjectData.uniqueId(), gameObjectData.uniqueId(), blueprint.blueprintId());

    return object;
  }

  /**
   * Helper method to find a Layer by name, returning a default if not found.
   */
  private Layer findLayerByName(String name, Layer defaultLayer) {
    Objects.requireNonNull(defaultLayer, "Default layer cannot be null in findLayerByName");

    if (name == null || name.trim().isEmpty()) {
      LOG.warn("Layer name is null or empty, using default layer '{}'", defaultLayer.getName());
      return defaultLayer;
    }
    for (Layer layer : levelData.getLayers()) {
      if (layer.getName() != null && layer.getName().equals(name)) {
        LOG.trace("Found layer '{}' by name.", name);
        return layer;
      }
    }
    LOG.warn("Layer with name '{}' not found in EditorLevelData, using default layer '{}'", name, defaultLayer.getName());
    return defaultLayer;
  }
}