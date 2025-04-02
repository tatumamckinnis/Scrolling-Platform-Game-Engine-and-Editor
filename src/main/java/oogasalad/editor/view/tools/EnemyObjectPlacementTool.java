package oogasalad.editor.view.tools;

import java.util.UUID;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.view.EditorGameView;
import oogasalad.editor.view.InputTabComponentFactory;

/**
 * Tool for adding enemy objects to the game scene.
 * @author Tatum McKinnis
 */
public class EnemyObjectPlacementTool implements ObjectPlacementTool {
  private final EditorGameView editorView;
  private final EditorDataAPI editorAPI;
  private String enemyType;
  private String spritePath;

  /**
   * Creates a new enemy placement tool
   *
   * @param editorView The editor view to place objects on
   * @param editorAPI The editor API to use for backend operations
   * @param enemyType The type of enemy to create
   * @param spritePath The path to the enemy sprite
   */
  public EnemyObjectPlacementTool(EditorGameView editorView, EditorDataAPI editorAPI,
      String enemyType, String spritePath) {
    this.editorView = editorView;
    this.editorAPI = editorAPI;
    this.enemyType = enemyType;
    this.spritePath = spritePath;
  }

  /**
   * Sets the enemy type to place
   *
   * @param enemyType The enemy type
   */
  public void setEnemyType(String enemyType) {
    this.enemyType = enemyType;
  }

  /**
   * Sets the sprite path for the enemy
   *
   * @param spritePath The path to the sprite
   */
  public void setSpritePath(String spritePath) {
    this.spritePath = spritePath;
  }

  @Override
  public void placeObjectAt(int gridX, int gridY) {
    int x = gridX * editorView.getCellSize();
    int y = gridY * editorView.getCellSize();

    UUID newObjectId = editorAPI.createEditorObject();

    editorAPI.getIdentityDataAPI().setName(newObjectId, "Enemy_" + gridX + "_" + gridY);
    editorAPI.getIdentityDataAPI().setGroup(newObjectId, "ENEMY");

    editorAPI.getSpriteDataAPI().setX(newObjectId, x);
    editorAPI.getSpriteDataAPI().setY(newObjectId, y);
    editorAPI.getSpriteDataAPI().setSpritePath(newObjectId, spritePath);

    int cellSize = editorView.getCellSize();
    editorAPI.getHitboxDataAPI().setX(newObjectId, x);
    editorAPI.getHitboxDataAPI().setY(newObjectId, y);
    editorAPI.getHitboxDataAPI().setWidth(newObjectId, cellSize);
    editorAPI.getHitboxDataAPI().setHeight(newObjectId, cellSize);
    editorAPI.getHitboxDataAPI().setShape(newObjectId, "RECTANGLE");

    EditorObject object = editorAPI.getLevel().getEditorObject(newObjectId);
    editorView.addObject(newObjectId);
  }
}
