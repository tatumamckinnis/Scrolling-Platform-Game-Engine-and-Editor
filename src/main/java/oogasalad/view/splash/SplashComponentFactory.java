package oogasalad.view.splash;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * The SplashComponentFactory creates all the JavaFX objects used to construct the splash screen,
 * such as loading the application's logo, or creating the buttons for switching to other scenes,
 * like an active game or a game editor.
 * @author Luke Nam
 */
public class SplashComponentFactory {
  private static final String splashComponentPropertiesFilepath = "/oogasalad/screens/splashScene.properties";
  private static final Properties splashComponentProperties = new Properties();

  /**
   * Initialize a SplashComponentFactory object given a properties file containing all the
   * pane, button, and image dimensions
   * TODO: Provide exception handling through the View to provide error pop-ups, rather than println
   */
  public SplashComponentFactory() {
    try {
      InputStream stream = getClass().getResourceAsStream(splashComponentPropertiesFilepath);
      splashComponentProperties.load(stream);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  /**
   * Creates the splash scene that can be loaded directly by the view
   * @return Scene containing the application image and the buttons
   */
  public Scene createSplashScene() {
    HBox root = new HBox();
    int splashWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.width"));
    int splashHeight = Integer.parseInt(splashComponentProperties.getProperty("splash.height"));

    Pane logoPane = createLogoPane(splashHeight);
    Pane optionsPane = createOptionsPane(splashHeight);

    root.getChildren().addAll(logoPane, optionsPane);
    return new Scene(root, splashWidth, splashHeight);
  }

  /**
   * Creates the left pane in the splash scene that contains the splash scene logo
   * TODO: Externalize an CSS config for logoPane color
   * @param splashHeight height of the left pane
   * @return pane containing the logo
   */
  private Pane createLogoPane(int splashHeight) {
    Pane logoPane = new Pane();
    int logoPaneWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.leftPane.width"));
    logoPane.setPrefSize(logoPaneWidth, splashHeight);
    logoPane.setStyle("-fx-background-color: lightblue;");
    logoPane.getChildren().add(createSplashLogo());
    return logoPane;
  }

  /**
   * Creates the right pane containing the button box for the splash scene
   * TODO: Externalize an CSS config for optionsPane color
   * @param splashHeight height of the left pane
   * @return pane containing the button box
   */
  private Pane createOptionsPane(int splashHeight) {
    Pane optionsPane = new Pane();
    int optionsPaneWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.rightPane.width"));
    optionsPane.setPrefSize(optionsPaneWidth, splashHeight);
    optionsPane.setStyle("-fx-background-color: lightgreen;");
    optionsPane.getChildren().add(createSplashButtonBox());
    return optionsPane;
  }

  /**
   * Creates an image containing the splash logo for the splash scene
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
      throw new NullPointerException(String.format("OOGASalad splash filepath not found: %s", e.getMessage()));
    }
    return splashLogo;
  }

  /**
   * Scales the splash logo given the configuration's image width and height.
   * @param splashLogo the logo we are expected to scale
   */
  private void scaleSplashLogo(ImageView splashLogo) {
    int splashWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.logo.width"));
    int splashHeight = Integer.parseInt(splashComponentProperties.getProperty("splash.logo.height"));
    splashLogo.setFitWidth(splashWidth);
    splashLogo.setFitHeight(splashHeight);
    splashLogo.setPickOnBounds(true);
    splashLogo.setPreserveRatio(true);
    splashLogo.setId("splashLogo");
  }

  /**
   * Create a box containing all the buttons for the splash scene
   * @return VBox of splash scene buttons
   */
  private VBox createSplashButtonBox() {

    VBox splashBox = new VBox();
    String[] buttonTexts = getSplashButtonTexts();
    String[] buttonIDs = getSplashButtonIDs();

    double buttonWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.button.width"));
    double buttonHeight = Integer.parseInt(splashComponentProperties.getProperty("splash.button.height"));

    for (int i = 0; i < buttonIDs.length; i++) {
      Button currButton = new Button(buttonTexts[i]);
      currButton.setId(buttonIDs[i]);
      currButton.setPrefSize(buttonWidth, buttonHeight);
      currButton.setWrapText(true);
      splashBox.getChildren().add(currButton);
    }

    int buttonSpacing = Integer.parseInt(splashComponentProperties.getProperty("splash.button.spacing"));
    alignSplashButtonBox(splashBox, buttonSpacing);
    return splashBox;
  }

  /**
   * Align a splash box for centering and spacing
   * @param splashBox box for buttons to the game engine, editor, and so on
   * @param spacing pixel integer spacing between buttons
   */
  private void alignSplashButtonBox(VBox splashBox, double spacing) {
    splashBox.setAlignment(Pos.CENTER);
    splashBox.setMaxWidth(Double.MAX_VALUE);
    splashBox.setSpacing(spacing);
  }

  /**
   * Provides button texts for the splash scene, like "Select Game Type"
   * @return array of strings for button strings
   */
  private String[] getSplashButtonTexts() {
    return new String[]{splashComponentProperties.getProperty("splash.button.gameType"),
        splashComponentProperties.getProperty("splash.button.startEngine"),
        splashComponentProperties.getProperty("splash.button.startEditor"),
        splashComponentProperties.getProperty("splash.button.help")
    };
  }

  /**
   * Provides button IDs for the splash scene to allow for TestFX end-to-end tests
   * @return array of strings for button IDs
   */
  private String[] getSplashButtonIDs() {
    return new String[]{"splashButtonGameType",
        "splashButtonStartEngine",
        "splashButtonStartEditor",
        "splashButtonHelp"
    };
  }
}
