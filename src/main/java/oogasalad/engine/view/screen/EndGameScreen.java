package oogasalad.engine.view.screen;


import java.util.Objects;

import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.view.factory.ButtonActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.ViewState;

/**
 * Win Screen is displayed when a user winds a given level
 *
 * @author Luke Nam
 */
public class EndGameScreen extends GameOverlayScreen {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private final String generalOverlayResource = "engine.view.overlayScene";

  private static final Logger LOG = LogManager.getLogger();
  private String endGameScreenResource;
  private String overlayStylesheet;
  private ViewState viewState;

  /**
   * constructs a new end game screen instance
   *
   * @param viewState the viewstate of the view
   */
  public EndGameScreen(ViewState viewState) {
    super(viewState);
    this.viewState = viewState;
  }

  @Override
  public void renderEndGameScreen(boolean gameWon) {
    String key;
    key = gameWon ? "win" : "lose";
    String overlayStylesheetFilepath = resourceManager.getConfig(generalOverlayResource,
        key + ".stylesheet");
    overlayStylesheet = Objects.requireNonNull(getClass().getResource(overlayStylesheetFilepath))
        .toExternalForm();
    endGameScreenResource = "engine.view." + key + "GameScreen";
    LOG.info(endGameScreenResource);
    initialize();
  }

  private void initialize() {
    VBox combinedBox = new VBox();
    VBox textBox = createOverlayTextBox();
    VBox buttonBox = createOverlayButtonBox();
    combinedBox.getChildren().addAll(textBox, buttonBox);
    combinedBox.getStyleClass()
        .add(resourceManager.getConfig(endGameScreenResource, "background.style"));
    alignBox(combinedBox,
        Integer.parseInt(resourceManager.getConfig(endGameScreenResource, "boxSpacing")));
    combinedBox.setPrefSize(
        Integer.parseInt(resourceManager.getConfig(endGameScreenResource, "screen.width")),
        Integer.parseInt(resourceManager.getConfig(endGameScreenResource, "screen.height"))
    );
    this.getChildren().add(combinedBox);
    this.getStylesheets().add(overlayStylesheet);
  }

  @Override
  public VBox createOverlayTextBox() {
    VBox textBox = new VBox();
    int wrappingWidth = Integer.parseInt(
        resourceManager.getConfig(endGameScreenResource, "wrappingWidth"));

    // First, extract the "win message" from the properties file
    String winMessage = resourceManager.getConfig(endGameScreenResource, "message");
    int winFont = Integer.parseInt(
        resourceManager.getConfig(endGameScreenResource, "primaryFont"));
    Text winText = createStyledText(winMessage, new Font(winFont), wrappingWidth);

    // Finally, combine both text messages together
    textBox.getChildren().addAll(winText);
    alignBox(textBox,
        Integer.parseInt(resourceManager.getConfig(endGameScreenResource, "defaultSpacing")));
    return textBox;
  }

  @Override
  public VBox createOverlayButtonBox() {
    VBox buttonBox = new VBox();
    String[] buttonTexts = getButtonTexts();
    String[] buttonIDs = getButtonIDs();

    for (int i = 0; i < buttonIDs.length; i++) {
      Button currButton = new Button(buttonTexts[i]);
      currButton.getStyleClass()
          .add(resourceManager.getConfig(endGameScreenResource, "button.style"));
      LOG.info(buttonIDs[i]);
      setButtonAction(buttonIDs[i], currButton);
      buttonBox.getChildren().add(currButton);
    }

    int buttonSpacing = Integer.parseInt(
        resourceManager.getConfig(endGameScreenResource, "defaultSpacing"));
    alignBox(buttonBox, buttonSpacing);
    return buttonBox;
  }

  private void setButtonAction(String buttonID, Button currButton) {
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    currButton.setOnAction(event -> {
      factory.getAction(buttonID).run();
    });
  }

  /**
   * Provides button texts for the win screen.
   *
   * @return array of strings for button strings
   */
  private String[] getButtonTexts() {
    return new String[]{
        resourceManager.getConfig(endGameScreenResource, "button.restartLevel.text"),
        resourceManager.getConfig(endGameScreenResource, "button.returnHome.text")
    };
  }

  /**
   * Provides button IDs for the win screen.
   *
   * @return array of strings for button IDs
   */
  private String[] getButtonIDs() {
    return new String[]{
        resourceManager.getConfig(endGameScreenResource, "button.restartLevel.id"),
        resourceManager.getConfig(endGameScreenResource, "button.returnHome.id")
    };
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    // No need to remove game objects from the win screen, as it is a static overlay
    // that doesn't support adding dynamic objects.
    // This method is intentionally left blank.
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotRemoveAddObjectImage"));
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotRenderPlayerStats"));
  }

}
