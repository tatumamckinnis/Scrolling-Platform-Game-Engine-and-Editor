package oogasalad.engine.view;

import java.util.List;
import oogasalad.engine.exception.InputException;
import oogasalad.engine.exception.RenderingException;
import oogasalad.engine.exception.ViewInitializationException;
import oogasalad.engine.model.object.GameObject;

/**
 * Represents the primary view interface for the game application.
 * Responsible for rendering game objects and updating the visual state.
 * This interface serves as the boundary between the game engine and the visual representation,
 * allowing different rendering implementations to be used with the same game logic.
 *
 * @author Aksel Bell
 */
public interface GameAppView {
  /**
   * Initializes the view as a splash screen with the specified parameters.
   * Should be called before any rendering operations.
   *
   * @param width the width of the game window
   * @param height the height of the game window
   * @throws ViewInitializationException if the view cannot be initialized due to
   *      missing resources, hardware limitations, or window creation failures
   */
  void initialize(String title, int width, int height) throws ViewInitializationException;

  /**
   * Renders a list of game objects to the view.
   * This method is typically called by the game engine after updating game objects.
   *
   * @param gameObjects the list of game objects to render
   * @throws RenderingException if there is an error during the rendering process,
   *      such as invalid sprite resources, rendering context errors, or memory limitations
   */
  void renderGameObjects(List<GameObject> gameObjects) throws RenderingException;

  /**
   * Retrieves the current user inputs.
   * This allows the game engine to respond to user interactions.
   *
   * @return a list of inputs currently active
   * @throws InputException if there is an error accessing input devices
   *      or processing input events
   */
  List<String> getCurrentInputs() throws InputException;
}
