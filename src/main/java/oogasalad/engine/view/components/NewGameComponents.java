package oogasalad.engine.view.components;

import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;

/**
 * This class hold the components for a new game screen once the player either completes a level or
 * loses a life. It displays the high score of the player currently.
 * @author Aksel Bell, Luke Nam
 */
public class NewGameComponents extends Display {
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
  }

  /**
   * Renders the high score text on the game engine screen.
   */
  @Override
  public void initialRender() {
    VBox buttonContainer = new VBox();
    buttonContainer.getChildren().add(highScore);
    buttonContainer.setSpacing(20);
    buttonContainer.setLayoutX(250); // change to levelViewWidth / 2

    this.getChildren().add(buttonContainer);
  }

}
