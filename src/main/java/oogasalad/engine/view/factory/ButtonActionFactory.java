package oogasalad.engine.view.factory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.swing.JFileChooser;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.view.Display;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.ViewInitializationException;
import oogasalad.engine.view.ViewAPI;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.GameDisplay;
import oogasalad.engine.view.ViewVariableBridge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class returns the desired function for a specific button.
 *
 * @author Aksel Bell
 */
public class ButtonActionFactory {
  private static final Logger LOG = LogManager.getLogger();
  private static final String buttonIDToActionFilePath = "/oogasalad/screens/buttonAction.properties";
  private static final Properties buttonIDToActionProperties = new Properties();
  private final ViewAPI gameView;
  private final GameManagerAPI gameManager;

  /**
   * Loads property file map of buttonIDs to Actions.
   */
  public ButtonActionFactory(GameManagerAPI gameManager, ViewAPI gameView) {
    try {
      InputStream stream = getClass().getResourceAsStream(buttonIDToActionFilePath);
      buttonIDToActionProperties.load(stream);
    } catch (IOException e) {
      LOG.warn("Unable to load button action properties");
    }
    this.gameManager = gameManager;
    this.gameView = gameView;
  }

  /**
   * Returns the corresponding runnable function for the specified button.
   * @param buttonID string of the button's unique ID
   * @return runnable function for the button's onClick action
   */
  public Runnable getAction(String buttonID) {
    String methodName = buttonIDToActionProperties.getProperty(buttonID);

    try {
      Method method = ButtonActionFactory.class.getDeclaredMethod(methodName);
      method.setAccessible(true);

      return (Runnable) method.invoke(this);
    } catch (Exception e) {
      LOG.warn("Failed to find function associated with buttonID " + buttonID);
      throw new RuntimeException("Failed to return function: " + methodName, e);
    }
  }

  /**
   * Start button on the home page.
   * @throws ViewInitializationException thrown if error initializing the view.
   * @throws InputException if error parsing user key inputs.
   */
  private Runnable startGame() throws ViewInitializationException, InputException {
    return () -> {
      try {
        DefaultView view = (DefaultView) gameView;
        Stage currentStage = ViewVariableBridge.getStage(view);

        GameDisplay game = new GameDisplay(gameView, gameManager);
        gameManager.playGame();

        game.render();
        ViewVariableBridge.setDisplay(view, game);

        currentStage.setWidth(1000); // TODO set this to the game size
        currentStage.setHeight(1000);

        setCurrentInputs(view.getCurrentScene()).run();
      } catch (Exception e) {
        LOG.error("Error starting game", e);
      }
    };
  }

  /**
   * Help/credits button on home page
   * @return a runnable that opens the help/credits page
   * @throws ViewInitializationException thrown if issue with initialization.
   */
  private Runnable openHelp() throws ViewInitializationException {
    // TODO need to implement
    return null;
  }

  /**
   * Returns any view to the homepage.
   @throws ViewInitializationException thrown if error initializing the view.
   */
  private Runnable goToHome() throws ViewInitializationException {
    return () -> {
      try {
        DefaultView view = (DefaultView) gameView;
        gameManager.pauseGame();
        gameView.initialize();
        Stage currentStage = ViewVariableBridge.getStage(view);
        currentStage.setWidth(view.getCurrentScene().getWidth());
        currentStage.setHeight(view.getCurrentScene().getHeight());
        currentStage.setScene(view.getCurrentScene());
      } catch (ViewInitializationException e) {
        LOG.error("Error returning to home screen", e);
      }
    };
  }

  /**
   * Sets up input listeners when start is clicked.
   */
  private Runnable setCurrentInputs(Scene currentScene) throws ViewInitializationException {
    List<KeyCode> currentInputs = new ArrayList<>();
    ViewVariableBridge.setCurrentInputs((DefaultView) gameView, currentInputs);
    return () -> {
      currentScene.setOnKeyPressed(event -> {
        KeyCode keyCode = event.getCode();
        if (!currentInputs.contains(keyCode)) {
          currentInputs.add(keyCode);
        }
      });

      currentScene.setOnKeyReleased(event -> {
        KeyCode keyCode = event.getCode();
        currentInputs.remove(keyCode);
      });
    };
  }

  /**
   * Sets the current engine's game type.
   */
  private Runnable setGameType() {
    return () -> {
      try {
        gameManager.selectGame("dinosaurgame", "Easy", "DinoLevel1.xml");
      } catch (Exception e) {
        LOG.error("Error setting game type", e);
      }
    };
  }
//  TODO make open file chooser
//    return () -> {
//      FileChooser fileChooser = new FileChooser();
//
//      File selectedFile = fileChooser.showOpenDialog(ViewVariableBridge.getStage((DefaultView) gameView));
//      if (selectedFile != null) {
//        // TODO set the game type based on the file
//      }
//    };
//  }
}
