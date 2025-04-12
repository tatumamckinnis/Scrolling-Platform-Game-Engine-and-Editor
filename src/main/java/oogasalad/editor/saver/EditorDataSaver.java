package oogasalad.editor.saver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import oogasalad.editor.model.data.EditorLevelData;
import oogasalad.editor.model.data.EditorObject;
import oogasalad.editor.model.data.Layer;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.LevelData;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter.Blue;

public class EditorDataSaver {

  public static LevelData buildLevelData(EditorLevelData editorLevelData) {
    Map<Integer, BlueprintData> blueprintIdToData = new HashMap<>();
    Map<BlueprintData, Integer> keyToId = new HashMap<>();
    AtomicInteger nextId = new AtomicInteger();
    List<GameObjectData> gameObjects = new ArrayList<>();

    for (EditorObject obj : editorLevelData.getObjectDataMap().values()) {
      parseObject(obj);
    }

    return null;
  }

  private static void parseObject(EditorObject object) {
    BlueprintData candidate = BlueprintBuilder.fromEditorObject(object);
  }
}
