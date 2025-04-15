package oogasalad.engine.view.components;

import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.Main;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;

public class NewGameComponents extends Display {

  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");

  // contains a game over object if it is game ober
  // contains a play again
  // contains a high score object
  private final ViewState viewState;
  private final Text highScore;
  private final Button play;

  public NewGameComponents(ViewState viewState) {
    this.viewState = viewState;
    highScore = new Text("High score: " + 0);
    play = new Button("Play");
    play.setOnAction(event -> {
      viewState.getGameManager().playGame();
      this.hide();
    });
  }

  /**
   * renders the new game components
   */
  public void initialRender() {
    VBox buttonContainer = new VBox();
    buttonContainer.getChildren().addAll(highScore, play);
    buttonContainer.setSpacing(20);
    buttonContainer.setLayoutX(250); // change to levelViewWidth / 2

    this.getChildren().add(buttonContainer);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRemoveGameObjectImage"));
  }

}
