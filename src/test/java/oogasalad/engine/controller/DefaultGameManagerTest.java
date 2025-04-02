package oogasalad.engine.controller;

import javafx.animation.Timeline;
import oogasalad.engine.exception.ViewInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultGameManagerTest {

  private GameManagerAPI myGameManager;
  private Timeline myGameLoop;
  private EngineFileConverterAPI myEngineFile;
  private GameControllerAPI myGameController;

  @BeforeEach
  void setUp() throws ViewInitializationException {
    myGameManager = new DefaultGameManager((DefaultGameController) myGameController);
    myGameLoop = ((DefaultGameManager) myGameManager).getGameLoop();
  }

  @Test
  void playGame_PlayButtonPressed_TimelinePlays() {

  }

  @Test
  void pauseGame() {
  }

  @Test
  void restartGame() {
  }

  @Test
  void loadLevel() {
  }
}