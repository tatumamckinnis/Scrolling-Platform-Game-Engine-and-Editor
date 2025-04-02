package oogasalad.editor.view.tools;

import java.util.UUID;
import oogasalad.editor.controller.EditorDataAPI;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.view.EditorGameView;

/**
 * Tool for adding entity objects to the game scene.
 * This tool allows users to add a new game object to the scene.
 * Uses a factory to create appropriate game objects for different games.
 *
 * @author Tatum McKinnis
 */
public class EntityObjectPlacementTool implements ObjectPlacementTool{
  private final EditorGameView editorView;
  private final EditorDataAPI editorAPI;
  private String entityType;
  private String spritePath;

  /**
   * Creates a new entity placement tool
   *
   * @param editorView The editor view to place objects on
   * @param editorAPI The editor API to use for backend operations
   * @param entityType The type of entity to create
   * @param spritePath The path to the entity sprite
   */
  public EntityObjectPlacementTool(EditorGameView editorView, EditorDataAPI editorAPI,
      String entityType, String spritePath) {
    this.editorView = editorView;
    this.editorAPI = editorAPI;
    this.entityType = entityType;
    this.spritePath = spritePath;
  }

  /**
   * Sets the entity type to place
   *
   * @param entityType The entity type
   */
  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  /**
   * Sets the sprite path for the entity
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

    editorAPI.getIdentityDataAPI().setName(newObjectId, "Entity_" + gridX + "_" + gridY);
    editorAPI.getIdentityDataAPI().setGroup(newObjectId, entityType);

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
    editorView.addObject(newObjectId, object, x, y);
  }
}
