package oogasalad.engine.view.components;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;

import oogasalad.engine.model.object.ImmutablePlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import oogasalad.Main;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;

/**
 * This class holds a panel of buttons such as a home button and a play/pause button.
 *
 * @author Aksel Bell, Luke Nam
 */
public class GameControlPanel extends Display {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceBundle EXCEPTIONS = ResourceBundle.getBundle(
      Main.class.getPackage().getName() + "." + "Exceptions");
  private List<Button> buttons;
  private String gameControlPanelStylesheet;
  private ViewState viewState;

  private static final String engineComponentPropertiesFilepath = "/oogasalad/screens/engineComponent.properties";
  private static final Properties engineComponentProperties = new Properties();

  /**
   * Adds initial buttons to the control panel.
   */
  public GameControlPanel(ViewState viewState) {
    try {
      InputStream stream = getClass().getResourceAsStream(engineComponentPropertiesFilepath);
      engineComponentProperties.load(stream);
    } catch (IOException e) {
      LOG.warn("Unable to load engine component properties");
    }
    String gameControlPanelStylesheetFilepath = engineComponentProperties.getProperty("gameControlPanel.stylesheet");
    gameControlPanelStylesheet = Objects.requireNonNull(getClass().getResource(
      gameControlPanelStylesheetFilepath)).toExternalForm();
    buttons = new ArrayList<>();
    this.viewState = viewState;
    this.setViewOrder(-1);
    initializeGameControlPanelButtons();
    addButtonsToRoot();
  }

  private void addButtonsToRoot() {
    HBox buttonContainer = new HBox();
    buttonContainer.getChildren().addAll(buttons);
    int containerSpacing = Integer.parseInt(
        engineComponentProperties.getProperty("gameControlPanel.button.spacing"));
    int containerLayoutX = Integer.parseInt(
        engineComponentProperties.getProperty("gameControlPanel.button.layoutX"));
    buttonContainer.setSpacing(containerSpacing);
    buttonContainer.setLayoutX(containerLayoutX);
    buttonContainer.getStylesheets().add(gameControlPanelStylesheet);

    this.getChildren().add(buttonContainer);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRemoveGameObjectImage"));
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(EXCEPTIONS.getString("CannotRenderPlayerStats"));
  }

  /**
   * Initializes the game control panel buttons based on the properties file
   */
  public void initializeGameControlPanelButtons() {
    String[] buttonKeys = engineComponentProperties.getProperty("gameControlPanel.buttons").split(",");
    for (String buttonKey : buttonKeys) {
      String formattedButtonKey = "gameControlPanel.button." + buttonKey.trim();
      Button gameControlPanelButton = createGameControlPanelButton(formattedButtonKey);
      buttons.add(gameControlPanelButton);
    }
  }

  /**
   * Creates a button for the game control panel, such as play, pause, restart, home, etc.
   * The button is created using the properties file to get its image and dimensions.
   * @param buttonKey the properties key of the button to be created
   */
  private Button createGameControlPanelButton(String buttonKey) {
    Button gameControlPanelButton = new Button();
    applyButtonDimensions(gameControlPanelButton, buttonKey);

    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    String buttonID = engineComponentProperties.getProperty(buttonKey + ".id");
    gameControlPanelButton.setOnAction(event -> {
      factory.getAction(buttonID).run();
    });

    gameControlPanelButton.setFocusTraversable(false);
    return gameControlPanelButton;
  }

  /**
   * Applies the image and preferred dimensions to the button.
   * @param button the button object where we will apply the image and dimensions
   * @param buttonKey the property key of the button to be modified
   */
  private void applyButtonDimensions(Button button, String buttonKey) {
    int fitWidth = Integer.parseInt(engineComponentProperties.getProperty("gameControlPanel.button.width"));
    int fitHeight = Integer.parseInt(engineComponentProperties.getProperty("gameControlPanel.button.height"));

    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(
      engineComponentProperties.getProperty(buttonKey + ".image"))));
    ImageView imageView = new ImageView(image);

    imageView.setFitWidth(fitWidth);
    imageView.setFitHeight(fitHeight);

    button.setGraphic(imageView);
    button.setMinSize(fitWidth, fitHeight);
  }
}
