package oogasalad.editor.model.saver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.CameraData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;

/**
 * Converts in‑editor level data into the {@link LevelData} structure required by the file parser
 * API.
 * <p>
 * This class gathers all EditorObjects, assigns blueprint IDs, builds GameObjectData entries, and
 * assembles a complete LevelData record including camera and blueprint mappings.
 * </p>
 *
 * @author Jacob You
 */
public class EditorDataSaver {

  /**
   * Builds a {@link LevelData} record from the provided {@link EditorLevelData}.
   *
   * @param editorLevelData the in‑memory representation of the editor's level
   * @return a LevelData instance containing camera setup, blueprint mappings, and object data
   */
  public static LevelData buildLevelData(EditorLevelData editorLevelData) {
    Map<BlueprintData, Integer> blueprintToId = new HashMap<>();
    AtomicInteger nextId = new AtomicInteger();
    List<GameObjectData> gameObjects = new ArrayList<>();

    for (EditorObject obj : editorLevelData.getObjectDataMap().values()) {
      gameObjects.add(createGameObject(obj, blueprintToId, nextId));
    }

    int[] bounds = editorLevelData.getBounds();

    return new LevelData(
        editorLevelData.getLevelName(),
        bounds[0],
        bounds[1],
        bounds[2],
        bounds[3],
        new CameraData("camera", null, null),
        flipMapping(blueprintToId),
        gameObjects
    );
  }

  // Creates or reuses a blueprint ID for the given object and returns its GameObjectData.
  private static GameObjectData createGameObject(EditorObject object,
      Map<BlueprintData, Integer> blueprintToId, AtomicInteger nextId) {

    BlueprintData candidate = BlueprintBuilder.fromEditorObject(object);
    Integer id = blueprintToId.get(candidate);

    if (id == null) {
      id = nextId.incrementAndGet();
      BlueprintData finalBlueprint = candidate.withId(id);
      blueprintToId.put(finalBlueprint, id);
    }

    Layer layer = object.getIdentityData().getLayer();
    return new GameObjectData(
        id,
        object.getIdentityData().getId(),
        object.getHitboxData().getX(),
        object.getHitboxData().getY(),
        layer.getPriority(),
        layer.getName()
    );
  }

  // Flips the blueprint-to-ID map so that IDs become keys and BlueprintData becomes values.
  private static Map<Integer, BlueprintData> flipMapping(
      Map<BlueprintData, Integer> blueprintToId) {
    Map<Integer, BlueprintData> flipped = new HashMap<>();
    for (Map.Entry<BlueprintData, Integer> entry : blueprintToId.entrySet()) {
      flipped.put(entry.getValue(), entry.getKey());
    }
    return flipped;
  }
}
