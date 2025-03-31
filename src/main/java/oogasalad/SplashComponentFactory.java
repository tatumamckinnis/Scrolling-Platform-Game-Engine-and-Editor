package oogasalad;

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

public class SplashComponentFactory {
  private static final String splashComponentPropertiesFilepath = "/oogasalad/screens/splashScene.properties";
  private static final Properties splashComponentProperties = new Properties();

  public SplashComponentFactory() {
    try {
      InputStream stream = getClass().getResourceAsStream(splashComponentPropertiesFilepath);
      splashComponentProperties.load(stream);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
  }

  public Scene createSplashScene() {
    HBox root = new HBox();

    Pane leftPane = new Pane();
    leftPane.setPrefSize(600, 600);
    leftPane.setStyle("-fx-background-color: lightblue;");
    leftPane.getChildren().add(createSplashLogo());

    Pane rightPane = new Pane();
    rightPane.setPrefSize(600, 600);
    rightPane.setStyle("-fx-background-color: lightgreen;");
    rightPane.getChildren().add(createSplashButtonBox());

    root.getChildren().addAll(leftPane, rightPane);
    return new Scene(root, 1200, 600);
  }

  public ImageView createSplashLogo() {
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

  public void scaleSplashLogo(ImageView splashLogo) {
    int splashWidth = Integer.parseInt(splashComponentProperties.getProperty("splash.logo.width"));
    int splashHeight = Integer.parseInt(splashComponentProperties.getProperty("splash.logo.height"));
    splashLogo.setFitWidth(splashWidth);
    splashLogo.setFitHeight(splashHeight);
    splashLogo.setPickOnBounds(true);
    splashLogo.setPreserveRatio(true);
    splashLogo.setId("splashLogo");
  }

  public VBox createSplashButtonBox() {

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
  
  private void alignSplashButtonBox(VBox splashBox, double spacing) {
    splashBox.setAlignment(Pos.CENTER);
    splashBox.setMaxWidth(Double.MAX_VALUE);
    splashBox.setSpacing(spacing);
  }


  private String[] getSplashButtonTexts() {
    return new String[]{splashComponentProperties.getProperty("splash.button.gameType"),
        splashComponentProperties.getProperty("splash.button.startEngine"),
        splashComponentProperties.getProperty("splash.button.startEditor"),
        splashComponentProperties.getProperty("splash.button.help")
    };
  }

  private String[] getSplashButtonIDs() {
    return new String[]{"splashButtonGameType",
        "splashButtonStartEngine",
        "splashButtonStartEditor",
        "splashButtonHelp"
    };
  }



}
