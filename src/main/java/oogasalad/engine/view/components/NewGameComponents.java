package oogasalad.engine.view.components;

import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.Main;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.model.object.ImmutablePlayer;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;

/**
 * This class hold the components for a new game screen once the player either completes a level or
 * loses a life. It displays the high score of the player currently.
 * @author Aksel Bell, Luke Nam
 */
public class NewGameComponents extends Display {

  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");

  // contains a game over object if it is game ober
  // contains a play again
  // contains a high score object
  private final ViewState viewState;
  private final Text highScore;

  /**
   * Constructor for the NewGameComponents class that initializes the high score text.
   * TODO: Externalize high score configuration
   * @param viewState the current view state of the game
   */
  public NewGameComponents(ViewState viewState) {
    this.viewState = viewState;
    highScore = new Text("High score: " + 0);
    initialize();
  }

  /**
   * Renders the high score text on the game engine screen.
   */
  private void initialize() {
    VBox buttonContainer = new VBox();
    buttonContainer.getChildren().add(highScore);
    buttonContainer.setSpacing(20);
    buttonContainer.setLayoutX(250); // change to levelViewWidth / 2

    this.getChildren().add(buttonContainer);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRemoveGameObjectImage"));
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRenderPlayerStats"));
  }


}
