package oogasalad.engine.view.factory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import javafx.stage.Stage;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.ViewState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ButtonActionFactoryTest {

  private ButtonActionFactory buttonActionFactory;
  private ViewState mockViewState;
  private GameManagerAPI mockGameManager;
  private DefaultView mockDefaultView;

  @BeforeEach
  void setUp() {
    mockViewState = mock(ViewState.class);
    mockGameManager = mock(GameManagerAPI.class);
    mockDefaultView = mock(DefaultView.class);
    Stage mockStage = mock(Stage.class);

    when(mockViewState.getGameManager()).thenReturn(mockGameManager);
    when(mockViewState.getDefaultView()).thenReturn(mockDefaultView);
    when(mockViewState.getStage()).thenReturn(mockStage);

    buttonActionFactory = new ButtonActionFactory(mockViewState);
  }

  @Test
  void PlayGame_InvokesGameManagerPlay() {
    Runnable playGame = buttonActionFactory.playGame();
    playGame.run();
    verify(mockGameManager, times(1)).playGame();
  }

  @Test
  void PauseGame_InvokesGameManagerPause() {
    Runnable pauseGame = buttonActionFactory.pauseGame();
    pauseGame.run();
    verify(mockGameManager, times(1)).pauseGame();
  }

  @Test
  void SelectLevel_ValidInput_DoesNotThrow() {
    Runnable selectLevel = buttonActionFactory.selectLevel("sampleGame", "level1.xml");
    assertDoesNotThrow(selectLevel::run);
  }

  @Test
  void SelectLanguage_SetsLanguage() {
    Runnable selectLanguage = buttonActionFactory.selectLanguage("Spanish");
    selectLanguage.run();
    verify(mockGameManager, times(1)).setLanguage("Spanish");
  }

  @Test
  void JoinLobby_CreatesClientSocket() {
    ViewState vs = mock(ViewState.class);
    GameManagerAPI gm = mock(GameManagerAPI.class);
    when(vs.getGameManager()).thenReturn(gm);
    when(gm.getCurrentLevel()).thenReturn("testLevel");

    Runnable joinLobby = ButtonActionFactory.joinLobby(1234, vs);
    assertDoesNotThrow(joinLobby::run);
  }
}
