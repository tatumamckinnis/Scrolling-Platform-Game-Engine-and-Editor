package oogasalad.engine.view.components;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.view.Display;

public class NewGameComponents extends Display {
  // contains a game over object if it is game ober
  // contains a play again
  // contains a high score object
  private GameManagerAPI myGameManager;
  private Text highScore;
  private Button play;

  public NewGameComponents(GameManagerAPI gameManager) {
    myGameManager = gameManager;
    highScore = new Text("High score: " + 0);
    play = new Button("Play");
    play.setOnAction(event -> {
      gameManager.playGame();
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
