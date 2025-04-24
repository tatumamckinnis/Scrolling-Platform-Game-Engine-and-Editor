package oogasalad.engine.view.screen;

import javafx.scene.text.Text;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;

import javafx.scene.control.Button;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;

/**
 * An online lobby screen with a start button and a text displaying the number of players.
 */
public class OnlineLobby extends Display {
  private final int players;
  private final int lobby;
  private final ViewState viewState;

  /**
   * Instantiate an online lobby.
   * @param players number of players in the lobby
   */
  public OnlineLobby(int players, ViewState viewState, int lobby) {
    this.players = players;
    this.lobby = lobby;
    this.viewState = viewState;
    initialize();
  }

  private void initialize() {
    Button startButton = new Button("Start Game");
    startButton.setOnAction(event -> {
      ButtonActionFactory factory = new ButtonActionFactory(viewState);
      factory.getActionAndSendServerMessage("splashButtonStartEngine").run();
    });
    Text playerCountText = new Text(String.format("Players in lobby: %s", players));
    Text lobbyText = new Text(String.format("Lobby number: %s", lobby));
    playerCountText.setX(100);
    playerCountText.setY(200);
    lobbyText.setX(100);
    lobbyText.setY(100);

    this.getChildren().addAll(playerCountText, startButton, lobbyText);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    // No game objects to remove in the lobby.
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    //
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    // No player stats to render in the lobby.
  }
}
