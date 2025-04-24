package oogasalad.engine.view.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.zip.DataFormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import oogasalad.Main;
import oogasalad.editor.controller.EditorMaker;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.model.object.ImmutablePlayer;
import oogasalad.engine.view.DefaultView;
import oogasalad.engine.view.GameDisplay;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.screen.LoginScreen;
import oogasalad.engine.view.screen.ProfileEditScreen;
import oogasalad.engine.view.screen.UserDataScreen;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.InputException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.RenderingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.exceptions.ViewInitializationException;
import oogasalad.userData.SessionManager;
import oogasalad.userData.UserDataApiDefault;
import oogasalad.userData.records.UserData;

/**
 * This class returns the desired function for a specific button.
 *
 * @author Aksel Bell
 */
public class ButtonActionFactory {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");
  private static final String buttonIDToActionFilePath = "/oogasalad/screens/buttonAction.properties";
  private static final String gamesFilePath = "data/gameData/levels/";
  private static final Properties buttonIDToActionProperties = new Properties();
  private final ViewState viewState;
  UserDataApiDefault userDataApi;


  /**
   * Loads property file map of buttonIDs to Actions.
   */
  public ButtonActionFactory(ViewState state) {
    try {
      InputStream stream = getClass().getResourceAsStream(buttonIDToActionFilePath);
      buttonIDToActionProperties.load(stream);
      userDataApi = new UserDataApiDefault();
    } catch (IOException e) {
      LOG.warn("Unable to load button action properties");
    }
    this.viewState = state;
  }

  /**
   * Returns the corresponding runnable function for the specified button.
   *
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
   *
   * @throws ViewInitializationException thrown if error initializing the view.
   * @throws InputException              if error parsing user key inputs.
   */
  private Runnable startGame() throws ViewInitializationException, InputException {
    return () -> {
      try {
        DefaultView gameView = viewState.getDefaultView();
        GameManagerAPI gameManager = viewState.getGameManager();
        Stage currentStage = viewState.getStage();

        GameDisplay game = new GameDisplay(viewState);
        viewState.setDisplay(game);

        currentStage.setWidth(1000); // TODO set this to the game size
        currentStage.setHeight(1000);

        gameManager.displayGameObjects();
        setCurrentInputs(gameView.getCurrentScene()).run();
      } catch (Exception e) {
        LOG.error("Error starting game", e);
      }
    };
  }

  /**
   * Help/credits button on home page
   *
   * @return a runnable that opens the help/credits page
   * @throws ViewInitializationException thrown if issue with initialization.
   */
  private Runnable openHelp() throws ViewInitializationException {
    // TODO need to implement
    return null;
  }

  /**
   * Returns a runnable that resumes the game
   *
   * @return a runnable that resumes the game
   */
  private Runnable playGame() {
    return () -> {
      viewState.getGameManager().playGame();
    };
  }

  /**
   * Returns a runnable that pauses the game.
   *
   * @return a runnable that pauses the game
   */
  private Runnable pauseGame() {
    return () -> {
      savePlayerProgress().run();
      viewState.getGameManager().pauseGame();
    };
  }

  /**
   * Returns a runnable that restarts the game, or throws an exception given an error
   *
   * @return a runnable that restarts the game
   */
  private Runnable restartGame() {
    return () -> {
      try {
        restart();
      } catch (DataFormatException e) {
        LOG.error("Failed to restart game due to misformatted data", e);
      } catch (IOException e) {
        LOG.error("Failed to restart game due to I/O errors", e);
      } catch (ClassNotFoundException e) {
        LOG.error("Unable to find specified class for game restart", e);
      } catch (InvocationTargetException e) {
        LOG.error("Invoked exception cannot be called", e);
      } catch (NoSuchMethodException e) {
        LOG.error("Failed to call the provided exception", e);
      } catch (InstantiationException e) {
        LOG.error("Unable to create exception for provided class", e);
      } catch (IllegalAccessException e) {
        LOG.error("Illegal permissions for accessing provided class", e);
      } catch (LayerParseException e) {
        LOG.error("Failed to parse layer", e);
      } catch (LevelDataParseException e) {
        LOG.error("Failed to parse level data", e);
      } catch (PropertyParsingException e) {
        LOG.error("Failed to parse property", e);
      } catch (SpriteParseException e) {
        LOG.error("Failed to parse sprite", e);
      } catch (EventParseException e) {
        LOG.error("Failed to parse event", e);
      } catch (HitBoxParseException e) {
        LOG.error("Failed to parse hitbox", e);
      } catch (BlueprintParseException e) {
        LOG.error("Failed to parse blueprint", e);
      } catch (GameObjectParseException e) {
        LOG.error("Failed to parse game object", e);
      } catch (RenderingException e) {
        LOG.error("Failed to render game", e);
      }
    };
  }

  private void restart()
      throws DataFormatException, IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException, LayerParseException, LevelDataParseException, PropertyParsingException, SpriteParseException, EventParseException, HitBoxParseException, BlueprintParseException, GameObjectParseException, RenderingException {
    savePlayerProgress().run();

    viewState.getGameManager().restartGame();
    GameDisplay game = new GameDisplay(viewState);
    viewState.setDisplay(game);
    viewState.getGameManager().displayGameObjects();
    viewState.getGameManager().pauseGame();
  }

  /**
   * Returns any view to the homepage.
   *
   * @throws ViewInitializationException thrown if error initializing the view.
   */
  private Runnable goToHome() throws ViewInitializationException {
    return () -> {
      try {
        savePlayerProgress().run();

        DefaultView view = viewState.getDefaultView();
        viewState.getGameManager().pauseGame();
        view.initialize();
        Stage currentStage = viewState.getStage();
        currentStage.setWidth(view.getCurrentScene().getWidth());
        currentStage.setHeight(view.getCurrentScene().getHeight());
        currentStage.setScene(view.getCurrentScene());
      } catch (ViewInitializationException e) {
        LOG.error("Error returning to home screen", e);
      } catch (FileNotFoundException e) {
        LOG.error("The levels for the game cannot be found", e);
      }
    };
  }

  /**
   * Sets up input listeners when start is clicked.
   */
  private Runnable setCurrentInputs(Scene currentScene) throws ViewInitializationException {
    List<KeyCode> currentInputs = new ArrayList<>();
    List<KeyCode> releasedKeys = new ArrayList<>();
    viewState.setCurrentInputs(currentInputs, releasedKeys);
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
        releasedKeys.add(keyCode);
      });
    };
  }

  /**
   * Sets the current engine's game type.
   */
  private Runnable setGameType() {
    return () -> {
      FileChooser fileChooser = new FileChooser();
      // TODO the catching of the errors
      File selectedFile = fileChooser.showOpenDialog(viewState.getStage());
      if (selectedFile != null) {
        try {
          viewState.getGameManager().selectGame(selectedFile.getAbsolutePath());
        } catch (DataFormatException | IOException | ClassNotFoundException |
                 InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | LayerParseException | LevelDataParseException |
                 PropertyParsingException | SpriteParseException | EventParseException |
                 HitBoxParseException | BlueprintParseException | GameObjectParseException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  /**
   * Returns a {@link Runnable} that attempts to load and initialize a game level based on the
   * selected game and level names. This method constructs the path to the level file using the
   * provided game and level names and delegates to the {@code GameManager} to load the level.
   *
   *
   * @param game  the name of the game (i.e., the folder name under the game levels directory)
   * @param level the name of the level file (typically with .xml extension) inside the game folder
   * @return a {@code Runnable} that, when executed, loads the specified level into the game engine
   */
  public Runnable selectLevel(String game, String level) {
    return () -> {
      if (game != null && level != null) {
        try {
          viewState.getGameManager()
              .selectGame(gamesFilePath + game + "/" + level);
        } catch (DataFormatException | IOException | ClassNotFoundException |
                 InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | LayerParseException | LevelDataParseException |
                 PropertyParsingException | SpriteParseException | EventParseException |
                 HitBoxParseException | BlueprintParseException | GameObjectParseException e) {
          LOG.error(EXCEPTIONS.getString("CannotSelectLevel"), e);
        }
      }
    };
  }

  private Runnable openEditor() {
    return () -> {
      new EditorMaker(new Stage());
    };
  }

  private Runnable renderNewSplashScreen() {
    return () -> {
      try {
        new DefaultGameManager();
      } catch (ViewInitializationException | FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    };
  }

   /* Action to navigate to the user profile screen.
      */
  private Runnable goToProfile() {
    return () -> {
      try {
        // Check if data/userData directory exists
        File userDataDir = new File("data/userData");
        if (!userDataDir.exists()) {
          userDataDir.mkdirs();
        }
        
        // Create a session manager to check for login status
        SessionManager sessionManager = new SessionManager();
        
        // Check if there's an active session
        if (sessionManager.hasActiveSession()) {
          String username = sessionManager.getSavedUsername();
          String password = sessionManager.getSavedPassword();
          
          try {
            // Try to load user with saved credentials
            UserData user = userDataApi.parseUserData(username, password);
            viewState.setDisplay(new UserDataScreen(viewState, user));
            LOG.info("Automatically logged in user: " + username);
          } catch (Exception e) {
            // Saved credentials are invalid or expired
            LOG.info("Saved credentials are invalid, showing login screen");
            sessionManager.clearSession(); // Clear invalid session
            viewState.setDisplay(new LoginScreen(viewState));
          }
        } else {
          // No saved credentials, show login screen
          viewState.setDisplay(new LoginScreen(viewState));
        }
      } catch (Exception e) {
        LOG.error("Failed to load profile screen", e);
      }
    };
  }

  private Runnable savePlayerProgress() {
    return () -> {
      try {
        // Check if GameManager has players before trying to get stats
        GameManagerAPI gameManager = viewState.getGameManager();
        if (gameManager == null) {
          LOG.info("No game manager available, skipping player progress save");
          return;
        }
        
        // Check if there are any players
        try {
          ImmutablePlayer player = (ImmutablePlayer) gameManager.getPlayer();
          if (player == null) {
            LOG.info("No player object available, skipping player progress save");
            return;
          }
          
          // Get current stats
          Map<String, String> currentStats = player.getDisplayedStatsMap();
          
          // Get current game and level information
          String gameName = gameManager.getCurrentGameName();
          String levelName = gameManager.getCurrentLevelName();
          
          // Use SessionManager to get current user credentials
          SessionManager sessionManager = new SessionManager();
          String username;
          String password;
          
          if (sessionManager.hasActiveSession()) {
            username = sessionManager.getSavedUsername();
            password = sessionManager.getSavedPassword();
          } else {
            // If no session, use default account as fallback (ideally remove this in a real app)
            username = "gamer123";
            password = "bruh";
          }
          
          // Parse user data
          UserData userData = userDataApi.parseUserData(username, password);
          
          // Update stats in user data
          userDataApi.updatePlayerLevelStats(username, gameName, levelName, currentStats);
          
          // Save to file
          userDataApi.writeCurrentUserData();
          
          LOG.info("Successfully saved player progress for game: " + gameName +
              ", level: " + levelName + " to file: " + userDataApi.getUserDataFilePath());
        } catch (IndexOutOfBoundsException e) {
          // This happens when no player is found
          LOG.info("No player found (empty list), skipping player progress save");
        }
      } catch (Exception e) {
        LOG.error("Failed to save player progress", e);
      }
    };
  }

  /**
   * Navigate to the user profile/data screen with the specified user data
   * @param userData The user data to display
   * @return A runnable that navigates to the UserDataScreen
   */
  public Runnable navigateToUserProfile(UserData userData) {
    return () -> {
      try {
        viewState.setDisplay(new UserDataScreen(viewState, userData));
      } catch (Exception e) {
        LOG.error("Failed to navigate to user profile screen", e);
      }
    };
  }

  /**
   * Navigate to the profile edit screen for the specified user data
   * @param userData The user data to edit
   * @return A runnable that navigates to the ProfileEditScreen
   */
  public Runnable navigateToProfileEdit(UserData userData) {
    return () -> {
      try {
        viewState.setDisplay(new ProfileEditScreen(viewState, userData));
      } catch (Exception e) {
        LOG.error("Failed to navigate to profile edit screen", e);
      }
    };
  }

  /**
   * Opens a file chooser dialog and returns the selected file.
   * This method exists to handle access to the stage, which is restricted for security reasons.
   * 
   * @param fileChooser The configured FileChooser to display
   * @return The selected File, or null if no file was selected
   */
  public File openFileChooser(FileChooser fileChooser) {
    return fileChooser.showOpenDialog(viewState.getStage());
  }

  /**
   * Logs out the current user by clearing their session and redirecting to the login screen.
   * 
   * @return A runnable that handles the logout process
   */
  private Runnable logout() {
    return () -> {
      try {
        // Create a session manager and clear the session
        SessionManager sessionManager = new SessionManager();
        sessionManager.clearSession();
        
        // Navigate to login screen
        viewState.setDisplay(new LoginScreen(viewState));
        LOG.info("User logged out successfully");
      } catch (Exception e) {
        LOG.error("Failed to logout", e);
      }
    };
  }
}
