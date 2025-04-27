package oogasalad.editor.model.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
   * @param fileName the path to the level file to load
   * @throws LayerParseException      if there is an error parsing layer information
   * @throws LevelDataParseException  if there is an error parsing the overall level structure
   * @throws PropertyParsingException if object properties fail to parse
   * @throws SpriteParseException     if sprite information fails to load
   * @throws EventParseException      if event definitions fail to parse
   * @throws HitBoxParseException     if hit box definitions are invalid
   * @throws BlueprintParseException  if blueprint data cannot be interpreted
   * @throws GameObjectParseException if an error occurs while creating game objects
   */
  public void loadLevelData(EditorLevelData editorLevelData,
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
      EditorObjectPopulator populator = new EditorObjectPopulator(editorLevelData);
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
   * @param editorLevelData the editor level data to configure
   * @param gameObjects the list of game objects containing layer information
   */
  private void setupLayersByZValue(EditorLevelData editorLevelData, List<GameObjectData> gameObjects) {
    // Collect all unique z-values (layer values) from game objects
    Set<Integer> uniqueZValues = new HashSet<>();
    for (GameObjectData obj : gameObjects) {
      uniqueZValues.add(obj.layer());
    }
    
    // If we found z-values, create layers for them
    if (!uniqueZValues.isEmpty()) {
      // Get current layers - we'll reuse the first one instead of removing it
      List<Layer> currentLayers = editorLevelData.getLayers();
      
      // Keep track of which layers we've processed
      Set<Layer> processedLayers = new HashSet<>();
      
      // Update or create layers for each z-value
      for (Integer z : uniqueZValues) {
        String layerName = "Layer_" + z;
        
        // Check if we can reuse an existing layer
        if (!currentLayers.isEmpty() && processedLayers.isEmpty()) {
          // Reuse the first layer instead of removing it
          Layer firstLayer = currentLayers.get(0);
          firstLayer.setName(layerName);
          firstLayer.setPriority(z);
          processedLayers.add(firstLayer);
          LOG.info("Updated existing layer to '{}' with priority {}", layerName, z);
        } else {
          // Create a new layer
          Layer layer = new Layer(layerName, z);
          editorLevelData.addLayer(layer);
          processedLayers.add(layer);
          LOG.info("Created new layer '{}' with priority {}", layerName, z);
        }
      }
      
      // Remove any extra layers (except the ones we've processed)
      if (currentLayers.size() > 1) {
        // Create a copy to avoid concurrent modification
        List<Layer> layersToCheck = new ArrayList<>(currentLayers);
        for (Layer layer : layersToCheck) {
          if (!processedLayers.contains(layer)) {
            try {
              editorLevelData.removeLayer(layer.getName());
              LOG.info("Removed unused layer '{}'", layer.getName());
            } catch (Exception e) {
              LOG.warn("Could not remove layer '{}': {}", layer.getName(), e.getMessage());
            }
          }
        }
      }
    } else {
      // No z-values found, keep or create a default layer
      if (editorLevelData.getLayers().isEmpty()) {
        Layer defaultLayer = new Layer("Default", 0);
        editorLevelData.addLayer(defaultLayer);
        LOG.info("Created default layer with priority 0");
      } else {
        LOG.info("No layers found in game objects, keeping existing layers");
      }
    }
    
    // Log a summary of all layers
    LOG.info("Final layer setup - {} layers:", editorLevelData.getLayers().size());
    for (Layer layer : editorLevelData.getLayers()) {
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
