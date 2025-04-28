package oogasalad.editor.model.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import oogasalad.editor.controller.level.EditorDataAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.Layer;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.saver.api.EditorFileConverterAPI;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EditorLoadException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * LevelData Converter converts data from level data to editor object Data
 *
 * @author Alana Zinkin, Jacob You
 */
public class LevelDataConverter {

  private static final Logger LOG = LoggerFactory.getLogger(LevelDataConverter.class);

  /**
   * Loads level data from the specified file and populates the editor's internal object map.
   *
   * <p>This method parses the provided level file into {@link LevelData}, extracts all
   * {@link GameObjectData} and their corresponding {@link BlueprintData}, and uses an
   * {@link EditorObjectPopulator} to create {@link EditorObject}s. Each object is then added to the
   * editor's level data map for later use by the editor view or controller.</p>
   *
   * @param editorDataAPI
   * @param fileName      the path to the level file to load
   * @throws LayerParseException      if there is an error parsing layer information
   * @throws LevelDataParseException  if there is an error parsing the overall level structure
   * @throws PropertyParsingException if object properties fail to parse
   * @throws SpriteParseException     if sprite information fails to load
   * @throws EventParseException      if event definitions fail to parse
   * @throws HitBoxParseException     if hit box definitions are invalid
   * @throws BlueprintParseException  if blueprint data cannot be interpreted
   * @throws GameObjectParseException if an error occurs while creating game objects
   */
  public void loadLevelData(EditorDataAPI editorDataAPI, EditorLevelData editorLevelData,
      EditorFileConverterAPI fileConverterAPI,
      String fileName) throws EditorLoadException {
    try {
      LevelData levelData = fileConverterAPI.loadFileToEditor(fileName);
      Map<Integer, BlueprintData> blueprintMap = levelData.gameBluePrintData();

      List<GameObjectData> gameObjectData = levelData.gameObjects();
      
      // Print detailed information about game objects and their layers
      LOG.info("Loaded {} game objects", gameObjectData.size());
      Map<Integer, Integer> layerCounts = new HashMap<>();
      for (GameObjectData obj : gameObjectData) {
        int zLayer = obj.layer();
        layerCounts.put(zLayer, layerCounts.getOrDefault(zLayer, 0) + 1);
        LOG.debug("Object {} (blueprint {}) has z-layer: {}, layerName: '{}'", 
            obj.uniqueId(), obj.blueprintId(), zLayer, obj.layerName());
      }
      
      // Print summary of layers
      LOG.info("Layer distribution:");
      for (Map.Entry<Integer, Integer> entry : layerCounts.entrySet()) {
        LOG.info(" - Layer {}: {} objects", entry.getKey(), entry.getValue());
      }
      
      // Create layers from the z-coordinates in the game objects
      setupLayersByZValue(editorLevelData, gameObjectData);
      
      // Create objects and populate the level
      EditorObjectPopulator populator = new EditorObjectPopulator(editorDataAPI);
      for (GameObjectData gameObject : gameObjectData) {
        EditorObject object = populator.populateFromGameObjectData(gameObject, blueprintMap);
        editorLevelData.updateObjectInDataMap(object.getId(), object);
        
        // Verify which layer the object was actually assigned to
        Layer objLayer = object.getIdentityData().getLayer();
        LOG.debug("Object {} assigned to layer '{}' with priority {}",
            object.getId(), objLayer.getName(), objLayer.getPriority());
      }
      
      // Final verification of object-layer assignments
      verifyLayerAssignments(editorLevelData);
      
    } catch (LayerParseException | LevelDataParseException | PropertyParsingException |
             SpriteParseException | EventParseException | HitBoxParseException |
             BlueprintParseException | GameObjectParseException e) {
      throw new EditorLoadException(e.getMessage(), e);
    }
  }
  
  /**
   * Sets up layers in the editor level data based solely on the z-values (layer field) 
   * from game objects. This ensures each distinct z-value becomes a layer with the
   * appropriate priority.
   * 
   * @param data the editor level data to configure
   * @param gameObjects the list of game objects containing layer information
   */
  private void setupLayersByZValue(EditorLevelData data, List<GameObjectData> gameObjects) {
    Set<Integer> uniqueZ = collectUniqueZValues(gameObjects);
    if (uniqueZ.isEmpty()) {
      ensureDefaultLayer(data);
    } else {
      Set<Layer> processed = initializeLayers(data, uniqueZ);
      cleanupLayers(data, processed);
    }
    logLayerSummary(data);
  }

  private Set<Integer> collectUniqueZValues(List<GameObjectData> gameObjects) {
    Set<Integer> unique = new HashSet<>();
    for (GameObjectData obj : gameObjects) {
      unique.add(obj.layer());
    }
    return unique;
  }

  private Set<Layer> initializeLayers(EditorLevelData data, Set<Integer> zValues) {
    List<Layer> current = data.getLayers();
    Set<Layer> processed = new HashSet<>();
    boolean reusedFirst = false;
    for (Integer z : zValues) {
      String name = "Layer_" + z;
      if (!reusedFirst && !current.isEmpty()) {
        Layer first = current.get(0);
        first.setName(name);
        first.setPriority(z);
        processed.add(first);
        LOG.info("Updated existing layer to '{}' with priority {}", name, z);
        reusedFirst = true;
      } else {
        Layer layer = new Layer(name, z);
        data.addLayer(layer);
        processed.add(layer);
        LOG.info("Created new layer '{}' with priority {}", name, z);
      }
    }
    return processed;
  }

  private void cleanupLayers(EditorLevelData data, Set<Layer> processed) {
    List<Layer> layers = new ArrayList<>(data.getLayers());
    for (Layer layer : layers) {
      if (!processed.contains(layer)) {
        try {
          data.removeLayer(layer.getName());
          LOG.info("Removed unused layer '{}'", layer.getName());
        } catch (Exception e) {
          LOG.warn("Could not remove layer '{}': {}", layer.getName(), e.getMessage());
        }
      }
    }
  }

  private void ensureDefaultLayer(EditorLevelData data) {
    if (data.getLayers().isEmpty()) {
      Layer defaultLayer = new Layer("Default", 0);
      data.addLayer(defaultLayer);
      LOG.info("Created default layer with priority 0");
    } else {
      LOG.info("No layers found in game objects, keeping existing layers");
    }
  }

  private void logLayerSummary(EditorLevelData data) {
    LOG.info("Final layer setup - {} layers:", data.getLayers().size());
    for (Layer layer : data.getLayers()) {
      LOG.info(" - Layer: '{}' with priority {}", layer.getName(), layer.getPriority());
    }
  }


  /**
   * Verifies that objects are correctly assigned to layers.
   * 
   * @param editorLevelData the editor level data to check
   */
  private void verifyLayerAssignments(EditorLevelData editorLevelData) {
    Map<String, Integer> layerObjectCounts = new HashMap<>();
    
    // Count objects in each layer
    for (EditorObject obj : editorLevelData.getObjectDataMap().values()) {
      Layer layer = obj.getIdentityData().getLayer();
      String layerName = layer.getName();
      layerObjectCounts.put(layerName, layerObjectCounts.getOrDefault(layerName, 0) + 1);
    }
    
    // Print summary
    LOG.info("Final object-layer distribution:");
    for (Map.Entry<String, Integer> entry : layerObjectCounts.entrySet()) {
      LOG.info(" - Layer '{}': {} objects", entry.getKey(), entry.getValue());
    }
  }
}
