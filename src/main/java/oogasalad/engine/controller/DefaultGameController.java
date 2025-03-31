/**
 * Game controller api logic implementation
 *
 */
package oogasalad.engine.controller;
import java.util.ArrayList;
import oogasalad.engine.model.object.GameObject;

import java.util.List;
import oogasalad.fileparser.records.LevelData;


public class DefaultGameController implements GameController {
    @Override
    public List<GameObject> getUpdatedObjects() {
        return new ArrayList<>();
    }

    @Override
    public void updateGameState() {

    }

    @Override
    public void setLevelData(LevelData data) {

    }
}