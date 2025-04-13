package oogasalad.engine.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import oogasalad.Main;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.factory.ButtonActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Initial splash screen a user sees when running the game engine.
 *
 * @author Luke Nam, Aksel Bell
 */
public class SplashScreen extends Display {

  private static final Logger LOG = LogManager.getLogger();
  private static final String splashComponentPropertiesFilepath = "/oogasalad/screens/splashScene.properties";
  private static final Properties splashComponentProperties = new Properties();
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");
  private String splashStylesheet;
  private int splashWidth;
  private int splashHeight;
  private ViewState viewState;

  /**
   * Constructor for making a new opening splash screen
   *
   * @param viewState the state of the view
   */
  public SplashScreen(ViewState viewState) {
    try {
      InputStream stream = getClass().getResourceAsStream(splashComponentPropertiesFilepath);
      splashComponentProperties.load(stream);
    } catch (IOException e) {
      LOG.warn("Unable to load splash screen properties");
    }
    String splashStylesheetFilepath = splashComponentProperties.getProperty("splash.stylesheet");
    splashStylesheet = Objects.requireNonNull(getClass().getResource(splashStylesheetFilepath))
        .toExternalForm();
    splashWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.width"));
    splashHeight = Integer.parseInt(splashComponentProperties.getProperty("splash.height"));
    this.viewState = viewState;
  }

  @Override
  public void initialRender() {
    initializeSplashScreen();
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRemoveGameObjectImage"));
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

  private void initializeSplashScreen() {
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
    int logoPaneWidth = Integer.parseInt(
        splashComponentProperties.getProperty("splash.leftPane.width"));
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
  private StackPane createOptionsPane(int splashHeight) {
    StackPane optionsPane = new StackPane();
    int optionsPaneWidth = Integer.parseInt(
        splashComponentProperties.getProperty("splash.rightPane.width"));
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
      String logoFilepath = splashComponentProperties.getProperty("splash.logo.image");
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
    int splashWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.logo.width"));
    int splashHeight = Integer.parseInt(
        splashComponentProperties.getProperty("splash.logo.height"));
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
      String logoFilepath = splashComponentProperties.getProperty("splash.background");
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
        splashComponentProperties.getProperty("splash.leftPane.width"));
    int backgroundHeight = Integer.parseInt(splashComponentProperties.getProperty("splash.height"));
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
  private VBox createSplashButtonBox() {
    VBox splashBox = new VBox();
    String[] buttonTexts = getSplashButtonTexts();
    String[] buttonIDs = getSplashButtonIDs();
    String[] buttonStyles = getSplashButtonStyles();

    double buttonWidth = Integer.parseInt(
        splashComponentProperties.getProperty("splash.button.width"));
    double buttonHeight = Integer.parseInt(
        splashComponentProperties.getProperty("splash.button.height"));

    for (int i = 0; i < buttonIDs.length; i++) {
      Button currButton = new Button(buttonTexts[i]);
      currButton.setPrefSize(buttonWidth, buttonHeight);
      setButtonStyle(currButton, buttonIDs[i], buttonStyles[i]);
      splashBox.getChildren().add(currButton);
    }

    int buttonSpacing = Integer.parseInt(
        splashComponentProperties.getProperty("splash.button.spacing"));
    alignSplashButtonBox(splashBox, buttonSpacing);
    return splashBox;
  }

  private void setButtonStyle(Button currButton, String buttonID, String buttonStyle) {
    currButton.setId(buttonID);
    String defaultButtonStyle = splashComponentProperties.getProperty(
        "splash.button.default.style");
    currButton.getStyleClass().add(defaultButtonStyle);
    currButton.getStyleClass().add(buttonStyle);
    currButton.setWrapText(true);
    setButtonAction(buttonID, currButton);
  }

  private void setButtonAction(String buttonID, Button currButton) {
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    currButton.setOnAction(event -> {
      factory.getAction(buttonID).run();
    });
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
    return new String[]{splashComponentProperties.getProperty("splash.button.gameType.text"),
        splashComponentProperties.getProperty("splash.button.startEngine.text"),
        splashComponentProperties.getProperty("splash.button.startEditor.text"),
        splashComponentProperties.getProperty("splash.button.help.text")
    };
  }

  /**
   * Provides button IDs for the splash scene to allow for TestFX end-to-end tests
   *
   * @return array of strings for button IDs
   */
  private String[] getSplashButtonIDs() {
    return new String[]{splashComponentProperties.getProperty("splash.button.gameType.id"),
        splashComponentProperties.getProperty("splash.button.startEngine.id"),
        splashComponentProperties.getProperty("splash.button.startEditor.id"),
        splashComponentProperties.getProperty("splash.button.help.id")
    };
  }

  /**
   * Provides button styles for the splash scene to allow for custom CSS styling
   *
   * @return array of strings for button styles
   */
  private String[] getSplashButtonStyles() {
    return new String[]{splashComponentProperties.getProperty("splash.button.gameType.style"),
        splashComponentProperties.getProperty("splash.button.startEngine.style"),
        splashComponentProperties.getProperty("splash.button.startEditor.style"),
        splashComponentProperties.getProperty("splash.button.help.style")
    };
  }
}
