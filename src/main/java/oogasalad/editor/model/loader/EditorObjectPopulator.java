package oogasalad.editor.model.loader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.HitboxData;
import oogasalad.editor.model.data.object.IdentityData;
import oogasalad.editor.model.data.object.event.PhysicsData;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class responsible for populating an EditorObject with data from various sources like
 * blueprints or game object data records. It handles the conversion between record formats and
 * editor model formats, including identity, sprite, hitbox, physics, and custom parameters. It also
 * ensures that necessary metadata like groups and layers are correctly handled within the
 * associated {@link EditorLevelData}.
 *
 * @author Tatum McKinnis
 */
public class EditorObjectPopulator {

  private static final Logger LOG = LogManager.getLogger(EditorObjectPopulator.class);
  private final EditorLevelData levelData;


  /**
   * Constructs an EditorObjectPopulator associated with a specific level.
   *
   * @param levelData the {@link EditorLevelData} instance this populator will interact with. Must
   *                  not be null.
   */
  public EditorObjectPopulator(EditorLevelData levelData) {
    this.levelData = Objects.requireNonNull(levelData, "levelData cannot be null");
    LOG.info("EditorObjectPopulator initialized.");
  }

  /**
   * Creates a new default EditorObject, initializes its components, and registers it within the
   * associated {@link EditorLevelData}.
   *
   * @return the newly created default {@link EditorObject}.
   */
  public EditorObject createDefaultObject() {
    LOG.debug("Creating default EditorObject.");
    EditorObject newObject = new EditorObject(levelData);
    levelData.getObjectDataMap().put(newObject.getId(), newObject);
    levelData.getObjectLayerDataMap()
        .computeIfAbsent(newObject.getIdentityData().getLayer(), k -> new ArrayList<>())
        .add(newObject);
    LOG.info("Default EditorObject created and registered with ID: {}", newObject.getId());
    return newObject;
  }

  /**
   * Creates and populates an {@link EditorObject} from a {@link BlueprintData} record and initial
   * position. This method translates the blueprint's properties (identity, sprite, hitbox, physics,
   * custom parameters) into the editor's object model format. It also registers the new object with
   * the {@link EditorLevelData} and ensures its group and layer are correctly handled.
   *
   * @param blueprint the {@link BlueprintData} record containing the object's template information.
   *                  Must not be null.
   * @param x         the initial x-coordinate for the object (typically sprite position).
   * @param y         the initial y-coordinate for the object (typically sprite position).
   * @return the newly created and populated {@link EditorObject}.
   */
  public EditorObject populateFromBlueprint(BlueprintData blueprint, double x, double y) {
    Objects.requireNonNull(blueprint, "Blueprint cannot be null");
    LOG.debug("Populating EditorObject from blueprint ID: {}, Type: {}", blueprint.blueprintId(),
        blueprint.type());
    EditorObject object = new EditorObject(levelData);

    Layer targetLayer = levelData.getFirstLayer();

    IdentityData identity = new IdentityData(object.getId(), blueprint.type(), blueprint.group(),
        targetLayer);
    object.setIdentityData(identity);

    if (blueprint.group() != null && !blueprint.group().trim().isEmpty()) {
      levelData.addGroup(blueprint.group());
      LOG.debug("Ensured group '{}' exists in level data.", blueprint.group());
    }

    String resolvedImagePath = "";
    SpriteData recordSprite = blueprint.spriteData();
    if (recordSprite != null && recordSprite.spriteFile() != null && !recordSprite.spriteFile()
        .getPath().isEmpty()) {
      resolvedImagePath = recordSprite.spriteFile().getPath();
      LOG.debug("Using image path from blueprint record: {}", resolvedImagePath);
      if (!new File(resolvedImagePath).isAbsolute() || !new File(resolvedImagePath).exists()) {
        LOG.warn(
            "Image path from blueprint record is relative or does not exist: {}. Sprite might not load.",
            resolvedImagePath);
        resolvedImagePath = "";
      }
    } else {
      LOG.warn("Blueprint {} spriteData or spriteFile is null/empty.", blueprint.blueprintId());
    }
    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = convertSpriteData(
        recordSprite, resolvedImagePath, x, y, blueprint.rotation(), blueprint.isFlipped());
    object.setSpriteData(modelSpriteData);

    if (blueprint.hitBoxData() != null) {
      HitBoxData bpHD = blueprint.hitBoxData();
      int hitboxX = (int) (x + bpHD.spriteDx());
      int hitboxY = (int) (y + bpHD.spriteDy());
      HitboxData modelHitbox = new HitboxData(
          hitboxX, hitboxY, bpHD.hitBoxWidth(), bpHD.hitBoxHeight(), bpHD.shape());
      object.setHitboxData(modelHitbox);
    } else {
      HitboxData defaultHitbox = object.getHitboxData();
      defaultHitbox.setX((int) x);
      defaultHitbox.setY((int) y);
      LOG.warn("Blueprint {} has no HitBoxData, using default hitbox positioned at ({}, {}).",
          blueprint.blueprintId(), (int) x, (int) y);
    }

    setPhysicsData(object, blueprint);

    object.setStringParameters(
        blueprint.stringProperties() != null ? blueprint.stringProperties() : new HashMap<>());
    object.setDoubleParameters(
        blueprint.doubleProperties() != null ? blueprint.doubleProperties() : new HashMap<>());
    LOG.debug("Populated {} string parameters and {} double parameters from blueprint.",
        object.getStringParameters().size(), object.getDoubleParameters().size());

    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().values()
        .forEach(list -> list.removeIf(obj -> obj.getId().equals(object.getId())));
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>())
        .add(object);

    LOG.info("Populated EditorObject {} from blueprint {}", object.getId(),
        blueprint.blueprintId());
    return object;
  }


  /**
   * Helper method to safely set a physics property (like gravity or jump force) on a
   * {@link PhysicsData} object from a {@link Double} value. Logs a warning if the value is null or
   * if setting the property fails.
   *
   * @param physics      the {@link PhysicsData} object to modify.
   * @param propertyName the name of the property to set (e.g., "gravity").
   * @param value        the {@link Double} value to set, can be null.
   */
  private void trySetPhysicsProperty(PhysicsData physics, String propertyName, Double value) {
    if (value == null) {
      return;
    }
    try {
      switch (propertyName) {
        case "gravity":
          physics.setGravity(value);
          break;
        case "jump_force":
          physics.setJumpForce(value);
          break;
        default:
          LOG.trace("Physics property '{}' not handled during population.", propertyName);
          break;
      }
    } catch (Exception e) {
      LOG.warn("Could not set physics property '{}' with value {}: {}", propertyName, value,
          e.getMessage());
    }
  }

  /**
   * Converts a {@link SpriteData} record (from file parsing) into an
   * {@link oogasalad.editor.model.data.object.sprite.SpriteData} model object used by the editor.
   * This handles the conversion of frames and animations, resolves the image path, and sets initial
   * positional and rotational properties. Includes logic to handle potential base frame name
   * inconsistencies.
   *
   * @param recordSprite the sprite data record from the file parser. Can be null.
   * @param imagePath    the resolved, potentially absolute, path to the sprite sheet image.
   * @param x            the initial x-coordinate for the sprite.
   * @param y            the initial y-coordinate for the sprite.
   * @param rotation     the initial rotation angle for the sprite.
   * @param isFlipped    the initial horizontal flip state for the sprite.
   * @return the corresponding editor sprite data model object. Returns a default if recordSprite is
   * null.
   */
  private oogasalad.editor.model.data.object.sprite.SpriteData convertSpriteData(
      SpriteData recordSprite, String imagePath, double x, double y,
      double rotation, boolean isFlipped) {

    if (recordSprite == null) {
      LOG.warn("Input SpriteData record is null. Using default sprite data.");
      return createDefaultSpriteData(x, y);
    }

    FrameData baseFrameRecord = convertFrameData(recordSprite.baseFrame());

    Map<String, FrameData> frameMapModel = recordSprite.frames()
        .stream().map(this::convertFrameData).filter(Objects::nonNull).collect(
            Collectors.toMap(FrameData::name,
                frame -> frame, (existing, replacement) -> replacement));

    Map<String, AnimationData> animationMapModel = recordSprite.animations().stream()
        .map(this::convertAnimationData).filter(Objects::nonNull).collect(
            Collectors.toMap(AnimationData::getName, anim -> anim,
                (existing, replacement) -> replacement));

    oogasalad.editor.model.data.object.sprite.SpriteData modelSpriteData = new oogasalad.editor.model.data.object.sprite.SpriteData(
        recordSprite.name(), (int) x, (int) y, rotation, isFlipped, frameMapModel,
        animationMapModel, imagePath);

    String baseFrameName =
        (baseFrameRecord != null && baseFrameRecord.name() != null && !baseFrameRecord.name()
            .isEmpty()) ? baseFrameRecord.name() : null;

    if (baseFrameName != null && frameMapModel.containsKey(baseFrameName)) {
      LOG.debug("Using exact match base frame name '{}' from blueprint record for sprite '{}'",
          baseFrameName, recordSprite.name());
    }
    else {
      if (!frameMapModel.isEmpty()) {
        baseFrameName = frameMapModel.keySet().iterator().next();
        LOG.warn(
            "Could not determine base frame from record or prefix/special case match for sprite '{}'. Falling back to first available frame name: '{}'",
            recordSprite.name(), baseFrameName);
      }
      else {
        LOG.warn(
            "No base frame name specified and no frames found for sprite record '{}'. Base frame name remains null.",
            recordSprite.name());
      }
      modelSpriteData.setBaseFrameName(baseFrameName);
    }

    LOG.info(
        "Converted sprite record '{}': Image='{}', BaseFrameNameSet='{}', Frames={}, Animations={}",
        recordSprite.name(), imagePath, modelSpriteData.getBaseFrameName(), frameMapModel.size(),
        animationMapModel.size());

    return modelSpriteData;
  }


  /**
   * Converts a {@link oogasalad.fileparser.records.FrameData} record (from fileparser) into an
   * {@link FrameData} model object used by the editor. Returns null if the input record is null.
   *
   * @param recordFrame the frame data record from the file parser.
   * @return the corresponding editor frame data model object, or null.
   */
  private FrameData convertFrameData(
      oogasalad.fileparser.records.FrameData recordFrame) {
    if (recordFrame == null) {
      return null;
    }
    return new FrameData(recordFrame.name(),
        recordFrame.x(), recordFrame.y(), recordFrame.width(), recordFrame.height());
  }

  /**
   * Converts an {@link oogasalad.fileparser.records.AnimationData} record (from fileparser) into an
   * {@link AnimationData} model object used by the editor. Handles null frame name lists. Returns
   * null if the input record is null.
   *
   * @param recordAnimation the animation data record from the file parser.
   * @return the corresponding editor animation data model object, or null.
   */
  private AnimationData convertAnimationData(
      oogasalad.fileparser.records.AnimationData recordAnimation) {
    if (recordAnimation == null) {
      return null;
    }
    List<String> frameNames =
        (recordAnimation.frameNames() != null) ? new ArrayList<>(recordAnimation.frameNames())
            : new ArrayList<>();
    return new AnimationData(recordAnimation.name(), recordAnimation.frameLen(), frameNames);
  }

  /**
   * Creates a default {@link oogasalad.editor.model.data.object.sprite.SpriteData} object for the
   * editor model, used as a fallback when conversion fails or input is missing.
   *
   * @param x the default x-coordinate.
   * @param y the default y-coordinate.
   * @return a default editor sprite data model object.
   */
  private oogasalad.editor.model.data.object.sprite.SpriteData createDefaultSpriteData(
      double x, double y) {
    return new oogasalad.editor.model.data.object.sprite.SpriteData("DefaultSprite", (int) x,
        (int) y, 0.0, false, Map.of(), Map.of(), "");
  }


  /**
   * Creates and populates an {@link EditorObject} from a {@link GameObjectData} record and a map of
   * available {@link BlueprintData}. This is typically used when loading a level file. It retrieves
   * the corresponding blueprint based on the ID in the GameObjectData, then populates the object
   * using the blueprint's template information and the specific instance data (position, layer)
   * from GameObjectData. It also registers the object with the {@link EditorLevelData}. If the
   * blueprint is not found, a minimal error object is created.
   *
   * @param gameObjectData the {@link GameObjectData} record containing instance-specific
   *                       information. Must not be null.
   * @param blueprintMap   a map from blueprint ID to {@link BlueprintData}, used to find the
   *                       template. Must not be null.
   * @return the newly created and populated {@link EditorObject}.
   */
  public EditorObject populateFromGameObjectData(GameObjectData gameObjectData,
      Map<Integer, BlueprintData> blueprintMap) {
    Objects.requireNonNull(gameObjectData, "GameObjectData cannot be null");
    Objects.requireNonNull(blueprintMap, "Blueprint map cannot be null");
    LOG.debug("Populating EditorObject from GameObjectData ID: {}, BlueprintID: {}",
        gameObjectData.uniqueId(), gameObjectData.blueprintId());
    EditorObject object = new EditorObject(levelData);

    BlueprintData blueprint = blueprintMap.get(gameObjectData.blueprintId());
    if (blueprint == null) {
      return createNullBlueprintObject(gameObjectData, object);
    }

    setIdentityData(gameObjectData, object, blueprint);
    setSpriteData(gameObjectData, object, blueprint);
    setHitboxData(gameObjectData, object, blueprint);
    setPhysicsData(object, blueprint);

    object.setStringParameters(
        blueprint.stringProperties() != null ? blueprint.stringProperties() : new HashMap<>());
    object.setDoubleParameters(
        blueprint.doubleProperties() != null ? blueprint.doubleProperties() : new HashMap<>());
    LOG.debug(
        "Populated {} string parameters and {} double parameters from GameObjectData/Blueprint.",
        object.getStringParameters().size(), object.getDoubleParameters().size());

    LOG.info("Populated EditorObject {} from GameObjectData {} using Blueprint {}",
        gameObjectData.uniqueId(), gameObjectData.uniqueId(), blueprint.blueprintId());

    return object;
  }

  private void setIdentityData(GameObjectData gameObjectData, EditorObject object,
      BlueprintData blueprint) {
    String layerName = gameObjectData.layerName();
    Layer defaultLayer = levelData.getFirstLayer();
    Layer targetLayer = findLayerByName(layerName, defaultLayer);

    String group;
    if (blueprint.group() != null && !blueprint.group().trim().isEmpty()) {
      group = blueprint.group();
    } else {
      group = "default"; // TODO: Make a constant/property
    }

    IdentityData identity = new IdentityData(gameObjectData.uniqueId(), gameObjectData.name(),
        group, targetLayer);
    object.setIdentityData(identity);

    levelData.addGroup(group);
    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().values()
        .forEach(list -> list.removeIf(obj -> obj.getId().equals(object.getId())));
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>())
        .add(object);
  }

  private void setSpriteData(GameObjectData gameObjectData, EditorObject object,
      BlueprintData blueprint) {
    String spriteFilePath = "";
    SpriteData recordSprite = blueprint.spriteData();
    if (recordSprite != null && recordSprite.spriteFile() != null && !recordSprite.spriteFile()
        .getPath().isEmpty()) {
      spriteFilePath = recordSprite.spriteFile().getPath();

    }
    if (spriteFilePath.isEmpty()) {
      LOG.warn("Blueprint {} for GameObject {} has no spriteData/spriteFile.",
          blueprint.blueprintId(), gameObjectData.uniqueId());
    }
    HitBoxData hitboxData = blueprint.hitBoxData();
    int spriteX = gameObjectData.x();
    int spriteY = gameObjectData.y();
    if (hitboxData != null) {
      spriteX += hitboxData.spriteDx();
      spriteY += hitboxData.spriteDy();
    }
    oogasalad.editor.model.data.object.sprite.SpriteData editorSpriteData = convertSpriteData(
        recordSprite, spriteFilePath, spriteX, spriteY,
        blueprint.rotation(), blueprint.isFlipped());
    object.setSpriteData(editorSpriteData);
  }

  private void setHitboxData(GameObjectData gameObjectData, EditorObject object, BlueprintData blueprint) {
    if (blueprint.hitBoxData() != null) {
      HitBoxData hitbox = blueprint.hitBoxData();
      int hitboxX = gameObjectData.x();
      int hitboxY = gameObjectData.y();
      HitboxData editorHitbox = new HitboxData(
          hitboxX, hitboxY, hitbox.hitBoxWidth(), hitbox.hitBoxHeight(), hitbox.shape());
      object.setHitboxData(editorHitbox);
    } else {
      HitboxData defaultHitbox = object.getHitboxData();
      defaultHitbox.setX(gameObjectData.x());
      defaultHitbox.setY(gameObjectData.y());
      LOG.warn(
          "Blueprint {} (GameObject {}) has no HitBoxData, using default hitbox positioned at ({}, {}).",
          blueprint.blueprintId(), gameObjectData.uniqueId(), gameObjectData.x(),
          gameObjectData.y());
    }
  }

  private void setPhysicsData(EditorObject object, BlueprintData blueprint) {
    PhysicsData physics = object.getPhysicsData();
    Map<String, Double> doubleProps = blueprint.doubleProperties();
    physics.setVelocityX(blueprint.velocityX());
    physics.setVelocityY(blueprint.velocityY());
    if (doubleProps != null) {
      trySetPhysicsProperty(physics, "gravity", doubleProps.get("gravity"));
      trySetPhysicsProperty(physics, "jump_force", doubleProps.get("jump_force"));
    }
  }

  /**
   * Helper method to find a {@link Layer} within the {@link EditorLevelData} by its name. If the
   * name is null, empty, or no matching layer is found, it returns the provided default layer and
   * logs a warning.
   *
   * @param name         the name of the layer to find.
   * @param defaultLayer the {@link Layer} to return if the lookup fails. Must not be null.
   * @return the found {@link Layer} or the defaultLayer.
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
    LOG.warn("Layer with name '{}' not found in EditorLevelData, using default layer '{}'", name,
        defaultLayer.getName());
    return defaultLayer;
  }

  private EditorObject createNullBlueprintObject(GameObjectData gameObjectData,
      EditorObject object) {
    LOG.error(
        "BlueprintData not found for blueprintId {} (GameObjectData {}). Creating minimal error object.",
        gameObjectData.blueprintId(), gameObjectData.uniqueId());
    Layer errorLayer = levelData.getFirstLayer();
    String layerName =
        gameObjectData.layerName() != null ? gameObjectData.layerName() : errorLayer.getName();
    Layer targetLayer = findLayerByName(layerName, errorLayer);
    IdentityData errorIdentity = new IdentityData(gameObjectData.uniqueId(),
        "ERROR_NoBlueprint_" + gameObjectData.uniqueId().toString().substring(0, 4), "ERROR",
        targetLayer);
    object.setIdentityData(errorIdentity);
    object.setSpriteData(createDefaultSpriteData(gameObjectData.x(), gameObjectData.y()));
    object.getHitboxData().setX(gameObjectData.x());
    object.getHitboxData().setY(gameObjectData.y());
    levelData.addGroup("ERROR");

    levelData.getObjectDataMap().put(object.getId(), object);
    levelData.getObjectLayerDataMap().computeIfAbsent(targetLayer, k -> new ArrayList<>())
        .add(object);
    return object;
  }
}