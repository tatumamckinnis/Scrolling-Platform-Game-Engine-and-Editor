package oogasalad.engine.view;

import java.io.FileNotFoundException;
import java.util.List;
import javafx.scene.input.KeyCode;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.camera.Camera;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.ViewInitializationException;

/**
 * Represents the primary view interface for the game application. Responsible for rendering game
 * objects and updating the visual state. This interface serves as the boundary between the game
 * engine and the visual representation, allowing different rendering implementations to be used
 * with the same game logic.
 *
 * @author Aksel Bell
 */
public interface ViewAPI {

  /**
   * Initializes the view as a splash screen with the specified parameters. Should be called before
   * any rendering operations.
   *
   * @throws ViewInitializationException if the view cannot be initialized due to missing resources,
   *                                     hardware limitations, or window creation failures
   */
  void initialize() throws ViewInitializationException, FileNotFoundException;

  /**
   * Renders a list of game objects to the view. This method is typically called by the game engine
   * after updating game objects.
   *
   * @param gameObjects the list of game objects to render
   * @throws RenderingException if there is an error during the rendering process, such as invalid
   *                            sprite resources, rendering context errors, or memory limitations
   */
  void renderGameObjects(List<ImmutableGameObject> gameObjects, Camera camera)
      throws RenderingException, FileNotFoundException;

  /**
   * Retrieves the currently pressed keys.
   *
   * @return a list of active KeyCodes.
   * @throws InputException if there is an issue retrieving inputs.
   */
  List<KeyCode> getCurrentInputs() throws InputException;

}
