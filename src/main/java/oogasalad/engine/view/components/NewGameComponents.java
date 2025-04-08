package oogasalad.engine.view.components;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;

public class NewGameComponents extends Display {
  // contains a game over object if it is game ober
  // contains a play again
  // contains a high score object
  private ViewState viewState;
  private Text highScore;
  private Button play;

  public NewGameComponents(ViewState viewState) {
    this.viewState = viewState;
    highScore = new Text("High score: " + 0);
    play = new Button("Play");
    play.setOnAction(event -> {
      viewState.getGameManager().playGame();
      this.hide();
    });
  }

  @Override
  public void render() {
    VBox buttonContainer = new VBox();
    buttonContainer.getChildren().addAll(highScore, play);
    buttonContainer.setSpacing(20);
    buttonContainer.setLayoutX(250); // change to levelViewWidth / 2

    this.getChildren().add(buttonContainer);
  }

}
