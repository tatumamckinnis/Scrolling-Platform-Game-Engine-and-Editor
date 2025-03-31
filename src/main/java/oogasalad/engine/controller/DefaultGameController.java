/**
 * Game controller api logic implementation
 *
 */
package oogasalad.engine.controller;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.DataFormatException;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
import oogasalad.fileparser.records.LevelData;


public class DefaultGameController implements GameControllerAPI {

    private List<GameObject> myGameObjects = new ArrayList<>();
    private EngineFileAPI myEngineFile;

    @Override
    public List<GameObject> getObjects() {
        return myGameObjects;
    }

    @Override
    public void updateGameState() {

    }

    @Override
    public void setLevelData(LevelData data) throws DataFormatException, IOException {
        myGameObjects = myEngineFile.loadFileToEngine(data);
    }
}