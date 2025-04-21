package oogasalad.engine.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import javafx.scene.Scene;
import javafx.stage.Stage;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.view.screen.SplashScreen;
import oogasalad.exceptions.ViewInitializationException;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class GameAppViewTest extends ApplicationTest {
  private MockDefaultView gameAppView;
  private Stage testStage;

  @Override
  public void start(Stage stage) throws ViewInitializationException, FileNotFoundException {
    this.testStage = stage;
    gameAppView = new MockDefaultView(testStage, new DefaultGameManager());
    gameAppView.initialize();
    testStage.setScene(gameAppView.getCurrentScene());
    testStage.show();
  }

  @Test
  void initialize_StandardScene_SceneInitialized() {
    Scene scene = testStage.getScene();

    assertNotNull(scene, "Scene should be initialized");
    assertTrue(scene.getRoot() instanceof Display, "Scene root should be a Display");
    assertTrue(scene.getRoot() instanceof SplashScreen, "Scene root should be a SplashScreen");
  }

  @Test
  void renderGameObjects_SplashScreenView_NoExceptionThrown() {
    assertDoesNotThrow(() -> gameAppView.renderGameObjects(null, null), "Rendering should not throw an exception");
  }

  @Test
  void startGame_StartEngineClicked_SceneChanges() {
    clickOn("#splashButtonStartEngine");
    assertFalse(testStage.getScene().getRoot() instanceof SplashScreen, "Screen should change from splash screen");
  }

  private class MockDefaultView extends DefaultView {
    /**
     * Constructor to initialize the GameAppView with a Stage reference.
     *
     * @param stage
     * @param gameManager
     */
    public MockDefaultView(Stage stage, GameManagerAPI gameManager)
        throws ViewInitializationException {
      super(stage, gameManager);
    }

    @Override
    public void initialize() throws ViewInitializationException, FileNotFoundException {
      SplashScreen splashScreen = new SplashScreen(new ViewState());

      int width = splashScreen.getSplashWidth();
      int height = splashScreen.getSplashHeight();
      setCurrentDisplay(splashScreen);
      setDefaultPath();
    }

    private void setDefaultPath() throws ViewInitializationException {

      try {
        //getGameManager().selectGame("DinoLevel2.xml");
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
}
