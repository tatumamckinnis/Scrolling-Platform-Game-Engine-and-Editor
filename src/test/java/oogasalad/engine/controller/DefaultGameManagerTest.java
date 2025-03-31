package oogasalad.engine.controller;

import javafx.animation.Timeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultGameManagerTest {

  private GameManagerAPI myGameManager;
  private Timeline myGameLoop;
  private EngineFileAPI myEngineFile;
  private GameControllerAPI myGameController;

  @BeforeEach
  void setUp() {
    myGameManager = new DefaultGameManager((DefaultEngineFile) myEngineFile, (DefaultGameController) myGameController);
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