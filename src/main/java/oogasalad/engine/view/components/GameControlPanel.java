package oogasalad.engine.view.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private static final Logger LOG = LogManager.getLogger();

  private List<Button> buttons;
  private String gameControlPanelStylesheet;
  private ViewState viewState;

  /**
   * Adds initial buttons to the control panel.
   */
  public GameControlPanel(ViewState viewState) {
    String gameControlPanelStylesheetFilepath = resourceManager.getConfig(
        "engine.view.engineComponent",
        "gameControlPanel.stylesheet");
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
        resourceManager.getConfig("engine.view.engineComponent",
            "gameControlPanel.button.spacing"));
    int containerLayoutX = Integer.parseInt(
        resourceManager.getConfig("engine.view.engineComponent",
            "gameControlPanel.button.layoutX"));
    buttonContainer.setSpacing(containerSpacing);
    buttonContainer.setLayoutX(containerLayoutX);
    buttonContainer.getStylesheets().add(gameControlPanelStylesheet);

    this.getChildren().add(buttonContainer);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotRemoveGameObjectImage"));
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotAddGameObjectImage"));
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotRenderPlayerStats"));
  }

  /**
   * Initializes the game control panel buttons based on the properties file
   */
  public void initializeGameControlPanelButtons() {
    String[] buttonKeys = resourceManager.getConfig("engine.view.engineComponent",
            "gameControlPanel.buttons")
        .split(",");
    for (String buttonKey : buttonKeys) {
      String formattedButtonKey = "gameControlPanel.button." + buttonKey.trim();
      Button gameControlPanelButton = createGameControlPanelButton(formattedButtonKey);
      buttons.add(gameControlPanelButton);
    }
  }

  /**
   * Creates a button for the game control panel, such as play, pause, restart, home, etc. The
   * button is created using the properties file to get its image and dimensions.
   *
   * @param buttonKey the properties key of the button to be created
   */
  private Button createGameControlPanelButton(String buttonKey) {
    Button gameControlPanelButton = new Button();
    applyButtonDimensions(gameControlPanelButton, buttonKey);

    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    String buttonID = resourceManager.getConfig("engine.view.engineComponent",
        buttonKey + ".id");
    gameControlPanelButton.setOnAction(event -> {
      factory.getActionAndSendServerMessage(buttonID).run();
    });

    gameControlPanelButton.setFocusTraversable(false);
    return gameControlPanelButton;
  }

  /**
   * Applies the image and preferred dimensions to the button.
   *
   * @param button    the button object where we will apply the image and dimensions
   * @param buttonKey the property key of the button to be modified
   */
  private void applyButtonDimensions(Button button, String buttonKey) {
    int fitWidth = Integer.parseInt(
        resourceManager.getConfig("engine.view.engineComponent",
            "gameControlPanel.button.width"));
    int fitHeight = Integer.parseInt(
        resourceManager.getConfig("engine.view.engineComponent",
            "gameControlPanel.button.height"));

    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(
        resourceManager.getConfig("engine.view.engineComponent",
            buttonKey + ".image"))));
    ImageView imageView = new ImageView(image);

    imageView.setFitWidth(fitWidth);
    imageView.setFitHeight(fitHeight);

    button.setGraphic(imageView);
    button.setMinSize(fitWidth, fitHeight);
  }
}
