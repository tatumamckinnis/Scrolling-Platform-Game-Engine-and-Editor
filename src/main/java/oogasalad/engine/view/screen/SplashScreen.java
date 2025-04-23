package oogasalad.engine.view.screen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.factory.ButtonActionFactory;

/**
 * Initial splash screen a user sees when running the game engine.
 *
 * @author Luke Nam, Aksel Bell
 */
public class SplashScreen extends Display {

  private static ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private static final Logger LOG = LogManager.getLogger();
  private static final String gamesFilePath = "data/gameData/levels/";
  private String splashStylesheet;
  private int splashWidth;
  private int splashHeight;
  private ViewState viewState;

  /**
   * Constructor for making a new opening splash screen
   *
   * @param viewState the state of the view
   */
  public SplashScreen(ViewState viewState) throws FileNotFoundException {
    String splashStylesheetFilepath = resourceManager.getConfig("engine.view.splashScene","splash.stylesheet");
    splashStylesheet = Objects.requireNonNull(getClass().getResource(splashStylesheetFilepath))
        .toExternalForm();
    splashWidth = Integer.parseInt(resourceManager.getConfig("engine.view.splashScene","splash.width"));
    splashHeight = Integer.parseInt(resourceManager.getConfig("engine.view.splashScene","splash.height"));
    this.viewState = viewState;

    initializeSplashScreen();
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(resourceManager.getText("exceptions", "CannotRemoveGameObjectImage"));
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(resourceManager.getText("exceptions", "CannotRenderPlayerStats"));
  }

  /**
   * Returns the width of the splash effect.
   *
   * @return the splash width
   */
  public int getSplashWidth() {
    return splashWidth;
  }

  /**
   * Returns the height of the splash effect.
   *
   * @return the splash height
   */
  public int getSplashHeight() {
    return splashHeight;
  }

  private void initializeSplashScreen() throws FileNotFoundException {
    HBox root = new HBox();
    Pane logoPane = createLogoPane(splashHeight);
    Pane optionsPane = createOptionsPane(splashHeight);
    root.getChildren().addAll(logoPane, optionsPane);
    this.getChildren().add(root);
    this.getStylesheets().add(splashStylesheet);
  }

  /**
   * Creates the left pane in the splash scene that contains the splash scene logo
   *
   * @param splashHeight height of the left pane
   * @return pane containing the logo
   */
  private StackPane createLogoPane(int splashHeight) {
    StackPane logoPane = new StackPane();
    int logoPaneWidth = Integer.parseInt(resourceManager.getConfig("engine.view.splashScene","splash.leftPane.width"));
    logoPane.setPrefSize(logoPaneWidth, splashHeight);
    logoPane.getStyleClass().add("logo-pane");
    logoPane.getChildren().add(createSplashBackground());
    logoPane.getChildren().add(createSplashLogo());
    return logoPane;
  }

  /**
   * Creates the right pane containing the button box for the splash scene
   *
   * @param splashHeight height of the left pane
   * @return pane containing the button box
   */
  private StackPane createOptionsPane(int splashHeight) throws FileNotFoundException {
    StackPane optionsPane = new StackPane();
    int optionsPaneWidth = Integer.parseInt(resourceManager.getConfig("engine.view.splashScene","splash.rightPane.width"));
    optionsPane.setPrefSize(optionsPaneWidth, splashHeight);
    optionsPane.getStyleClass().add("options-pane");
    optionsPane.getChildren().add(createSplashButtonBox());
    return optionsPane;
  }

  /**
   * Creates an image containing the splash logo for the splash scene
   *
   * @return application logo
   */
  private ImageView createSplashLogo() {
    ImageView splashLogo = new ImageView();
    try {
      String logoFilepath = resourceManager.getConfig("engine.view.splashScene","splash.logo.image");
      Image splashImage = new Image(getClass().getResourceAsStream(logoFilepath));
      scaleSplashLogo(splashLogo);
      splashLogo.setImage(splashImage);
    } catch (NullPointerException e) {
      throw new NullPointerException(
          String.format("OOGASalad splash filepath not found: %s", e.getMessage()));
    }
    return splashLogo;
  }

  /**
   * Scales the splash logo given the configuration's image width and height.
   *
   * @param splashLogo the logo we are expected to scale
   */
  private void scaleSplashLogo(ImageView splashLogo) {
    int splashWidth = Integer.parseInt(resourceManager.getConfig("engine.view.splashScene","splash.logo.width"));
    int splashHeight = Integer.parseInt(
        resourceManager.getConfig("engine.view.splashScene","splash.logo.height"));
    splashLogo.setFitWidth(splashWidth);
    splashLogo.setFitHeight(splashHeight);
    splashLogo.setPickOnBounds(true);
    splashLogo.setPreserveRatio(true);
    splashLogo.setId("splashLogo");
  }

  /**
   * TODO: Refactor createSplashLogo and createSplashBackground for reusable code (DRY)
   *
   * @return splash background image
   */
  private ImageView createSplashBackground() {
    ImageView splashBackground = new ImageView();
    try {
      String logoFilepath = resourceManager.getConfig("engine.view.splashScene",
          "splash.background");
      Image splashImage = new Image(getClass().getResourceAsStream(logoFilepath));
      splashBackground.setImage(splashImage);
      scaleSplashBackground(splashBackground);
    } catch (NullPointerException e) {
      throw new NullPointerException(
          String.format("OOGASalad splash background filepath not found: %s", e.getMessage()));
    }
    return splashBackground;
  }

  private void scaleSplashBackground(ImageView splashLogo) {
    int backgroundWidth = Integer.parseInt(
        resourceManager.getConfig("engine.view.splashScene", "splash.leftPane.width"));
    int backgroundHeight = Integer.parseInt(
        resourceManager.getConfig("engine.view.splashScene", "splash.height"));
    splashLogo.setFitWidth(backgroundWidth);
    splashLogo.setFitHeight(backgroundHeight);
    splashLogo.setPickOnBounds(true);
    splashLogo.setPreserveRatio(true);
    splashLogo.setId("splashBackground");
  }

  /**
   * Create a box containing all the buttons for the splash scene
   *
   * @return VBox of splash scene buttons
   */
  private VBox createSplashButtonBox() throws FileNotFoundException {
    VBox splashBox = new VBox();
    String[] buttonTexts = getSplashButtonTexts();
    String[] buttonIDs = getSplashButtonIDs();
    String[] buttonStyles = getSplashButtonStyles();
    String[] comboBoxTexts = getSplashComboBoxTexts();
    String[] comboBoxIDs = getSplashComboBoxIDs();
    String[] comboBoxStyles = getSplashButtonStylesID();

    double buttonWidth = Integer.parseInt(
        resourceManager.getConfig("engine.view.splashScene", "splash.button.width"));
    double buttonHeight = Integer.parseInt(
        resourceManager.getConfig("engine.view.splashScene", "splash.button.height"));

    renderLanguageSelector(comboBoxTexts, buttonWidth, buttonHeight, comboBoxIDs, comboBoxStyles,
        splashBox);

    renderGameAndLevelSelectors(comboBoxTexts, buttonWidth, buttonHeight, comboBoxIDs,
        comboBoxStyles, splashBox);

    for (int i = 0; i < buttonIDs.length; i++) {
      Button currButton = new Button(buttonTexts[i]);
      currButton.setPrefSize(buttonWidth, buttonHeight);
      setButtonStyle(currButton, buttonIDs[i], buttonStyles[i]);
      splashBox.getChildren().add(currButton);
    }

    addOnlineButtons(splashBox);

    int buttonSpacing = Integer.parseInt(
        resourceManager.getConfig("engine.view.splashScene", "splash.button.spacing"));
    alignSplashButtonBox(splashBox, buttonSpacing);
    return splashBox;
  }

  private void renderLanguageSelector(String[] comboBoxTexts, double buttonWidth,
      double buttonHeight,
      String[] comboBoxIDs, String[] comboBoxStyles, VBox splashBox) {
    ComboBox<String> languageSelector = createComboBox(comboBoxTexts, 2, buttonWidth, buttonHeight,
        comboBoxIDs, comboBoxStyles, splashBox);
    String languages = resourceManager.getConfig("engine.view.splashScene", "available.languages");
    String[] languagesArray = languages.split(",");
    for (String lang : languagesArray) {
      languageSelector.getItems().add(lang);
    }
    //splashBox.getChildren().add(languageSelector);
    setLanguageComboBoxButtonAction(languageSelector);
  }

  private void renderGameAndLevelSelectors(String[] comboBoxTexts, double buttonWidth,
      double buttonHeight,
      String[] comboBoxIDs, String[] comboBoxStyles, VBox splashBox) throws FileNotFoundException {
    ComboBox<String> gameTypeBox = createComboBox(comboBoxTexts, 0, buttonWidth, buttonHeight,
        comboBoxIDs, comboBoxStyles, splashBox);
    ComboBox<String> levelBox = createComboBox(comboBoxTexts, 1, buttonWidth, buttonHeight,
        comboBoxIDs, comboBoxStyles, splashBox);
    populateGameTypeComboBox(gameTypeBox);
    selectGameType(gameTypeBox, levelBox);
    setComboBoxButtonAction(gameTypeBox, levelBox);
  }

  /**
   * Adds buttons needed for starting online games. Will need refactoring.
   *
   * @param splashBox box to add buttons to.
   */
  private void addOnlineButtons(VBox splashBox) {
    TextField lobbyField = new TextField();
    lobbyField.setPromptText("Enter lobby number");

    Button joinServer = new Button("Create/Join Lobby");
    joinServer.setOnAction(event -> {
      ButtonActionFactory.joinLobby(Integer.parseInt(lobbyField.getText()), viewState).run();
    });

    splashBox.getChildren().addAll(joinServer, lobbyField);
  }

  private ComboBox<String> createComboBox(String[] comboBoxTexts, int i, double buttonWidth,
      double buttonHeight,
      String[] comboBoxIDs, String[] comboBoxStyles, VBox splashBox) {
    ComboBox<String> currBox = new ComboBox<>();
    currBox.setPromptText(comboBoxTexts[i]);
    currBox.setPrefSize(buttonWidth, buttonHeight);
    setComboBoxStyle(currBox, comboBoxIDs[i], comboBoxStyles[i]);
    splashBox.getChildren().add(currBox);
    return currBox;
  }

  private void setButtonStyle(Button currButton, String buttonID, String buttonStyle) {
    currButton.setId(buttonID);
    String defaultButtonStyle = resourceManager.getConfig("engine.view.splashScene",
        "splash.button.default.style");
    currButton.getStyleClass().add(defaultButtonStyle);
    currButton.getStyleClass().add(buttonStyle);
    currButton.setWrapText(true);
    setButtonAction(buttonID, currButton);
  }

  private void setComboBoxStyle(ComboBox<String> currBox, String comboBoxID, String comboBoxStyle) {
    currBox.setId(comboBoxID);
    String defaultButtonStyle = resourceManager.getConfig("engine.view.splashScene",
        "splash.button.default.style");
    currBox.getStyleClass().add(defaultButtonStyle);
    currBox.getStyleClass().add(comboBoxStyle);
  }


  private void setButtonAction(String buttonID, Button currButton) {
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    currButton.setOnAction(event -> {
      factory.getAction(buttonID).run();
    });
  }

  private void setComboBoxButtonAction(ComboBox<String> gameBox, ComboBox<String> levelBox) {
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    levelBox.valueProperty().addListener((obs, oldValue, level) -> {
      String game = gameBox.getValue();
      factory.selectLevel(game, level).run();
    });
  }

  private void setLanguageComboBoxButtonAction(ComboBox<String> comboBox) {
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    comboBox.valueProperty().addListener((obs, oldValue, language) -> {
      Runnable action = factory.selectLanguage(language);
      if (action != null) {
        action.run();
      } else {
        LOG.warn("No action defined for language: {}", language);
      }
    });
  }

  private void selectGameType(ComboBox<String> gameTypeComboBox, ComboBox<String> levelComboBox) {
    gameTypeComboBox.valueProperty().addListener((obs, oldValue, gameType) -> {
      if (gameType == null) {
        levelComboBox.getItems().clear();
        levelComboBox.setDisable(true);
      } else {
        populateLevelComboBox(gameType, levelComboBox);
      }
    });
  }

  private void populateLevelComboBox(String game, ComboBox<String> levelComboBox) {
    File levelFile = new File(gamesFilePath, game);
    File[] levels = levelFile.listFiles((dir, name) -> name.endsWith(".xml"));
    if (levels != null) {
      levelComboBox.getItems().clear();
      for (File level : levels) {
        levelComboBox.getItems().add(level.getName());
      }
      levelComboBox.getItems().sort(Comparator.naturalOrder());
    }
  }

  private void populateGameTypeComboBox(ComboBox<String> gameTypeComboBox)
      throws FileNotFoundException {
    File[] gameFolders = getGameFolders();
    gameTypeComboBox.getItems().setAll(
        Arrays.stream(gameFolders)
            .map(File::getName)
            .sorted()
            .toList()
    );
  }

  private File[] getGameFolders() throws FileNotFoundException {
    File gamesDir = new File(gamesFilePath);
    if (!gamesDir.exists() || !gamesDir.isDirectory()) {
      throw new FileNotFoundException(resourceManager.getText("exceptions", "GameDirectoryNotFound")
          + gamesDir.getAbsolutePath());
    }
    File[] folders = gamesDir.listFiles(File::isDirectory);
    return folders != null ? folders : new File[0];
  }


  /**
   * Align a splash box for centering and spacing
   *
   * @param splashBox box for buttons to the game engine, editor, and so on
   * @param spacing   pixel integer spacing between buttons
   */
  private void alignSplashButtonBox(VBox splashBox, double spacing) {
    splashBox.setAlignment(Pos.CENTER);
    splashBox.setMaxWidth(Double.MAX_VALUE);
    splashBox.setSpacing(spacing);
  }

  /**
   * Provides button texts for the splash scene, like "Select Game Type"
   *
   * @return array of strings for button strings
   */
  private String[] getSplashButtonTexts() {
    return new String[]{
        resourceManager.getText("displayedText", "splash.button.startEngine.text"),
        resourceManager.getText("displayedText", "splash.button.startEditor.text"),
        resourceManager.getText("displayedText", "splash.button.help.text"),
        resourceManager.getText("displayedText", "splash.button.play.another.game.text")
    };
  }

  private String[] getSplashComboBoxTexts() {
    return new String[]{resourceManager.getText("displayedText", "splash.button.gameType.text"),
        resourceManager.getText("displayedText", "splash.button.gameLevel.text"),
        resourceManager.getText("displayedText", "splash.button.select.language.text")
    };
  }

  /**
   * Provides button IDs for the splash scene to allow for TestFX end-to-end tests
   *
   * @return array of strings for button IDs
   */
  private String[] getSplashButtonIDs() {
    return new String[]{
        resourceManager.getConfig("engine.view.splashScene", "splash.button.startEngine.id"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.startEditor.id"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.help.id"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.play.another.game.id")
    };
  }

  private String[] getSplashComboBoxIDs() {
    return new String[]{
        resourceManager.getConfig("engine.view.splashScene", "splash.button.gameType.id"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.gameLevel.id"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.select.language.id")

    };
  }

  /**
   * Provides button styles for the splash scene to allow for custom CSS styling
   *
   * @return array of strings for button styles
   */
  private String[] getSplashButtonStyles() {
    return new String[]{
        resourceManager.getConfig("engine.view.splashScene", "splash.button.startEngine.style"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.startEditor.style"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.help.style"),
        resourceManager.getConfig("engine.view.splashScene",
            "splash.button.play.another.game.style")
    };
  }

  private String[] getSplashButtonStylesID() {
    return new String[]{
        resourceManager.getConfig("engine.view.splashScene", "splash.button.gameType.style"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.gameLevel.style"),
        resourceManager.getConfig("engine.view.splashScene", "splash.button.select.language.style")
    };
  }
}
