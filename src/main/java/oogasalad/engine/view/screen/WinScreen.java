package oogasalad.engine.view.screen;


import java.util.Objects;

import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.ViewState;

public class WinScreen extends GameOverlayScreen {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private static final Logger LOG = LogManager.getLogger();
  private String overlayStylesheet;
  private ViewState viewState;

  public WinScreen(ViewState viewState) {
    super(viewState);
    String overlayStylesheetFilepath = resourceManager.getConfig("engine.view.winScene",
        "win.stylesheet");
    overlayStylesheet = Objects.requireNonNull(getClass().getResource(overlayStylesheetFilepath))
        .toExternalForm();
    this.viewState = viewState;

    initialize();
  }

  private void initialize() {
    VBox combinedBox = new VBox();
    VBox textBox = createOverlayTextBox();
    VBox buttonBox = createOverlayButtonBox();
    combinedBox.getChildren().addAll(textBox, buttonBox);
    combinedBox.getStyleClass()
        .add(resourceManager.getConfig("engine.view.winScene", "win.background.style"));
    alignBox(combinedBox,
        Integer.parseInt(resourceManager.getConfig("engine.view.winScene", "win.boxSpacing")));
    this.getChildren().add(combinedBox);
    this.getStylesheets().add(overlayStylesheet);
  }

  @Override
  public VBox createOverlayTextBox() {
    VBox textBox = new VBox();
    int wrappingWidth = Integer.parseInt(
        resourceManager.getConfig("engine.view.winScene", "win.wrappingWidth"));

    // First, extract the "win message" from the properties file
    String winMessage = resourceManager.getConfig("engine.view.winScene", "win.message");
    int winFont = Integer.parseInt(
        resourceManager.getConfig("engine.view.winScene", "win.primaryFont"));
    Text winText = createStyledText(winMessage, new Font(winFont), wrappingWidth);

    // Then, extract the score message
    String scoreMessage = resourceManager.getConfig("engine.view.winScene", "win.score");
    int scoreFont = Integer.parseInt(
        resourceManager.getConfig("engine.view.winScene", "win.secondaryFont"));
    Text scoreText = createStyledText(scoreMessage, new Font(scoreFont), wrappingWidth);

    // Finally, combine both text messages together
    textBox.getChildren().addAll(winText, scoreText);
    alignBox(textBox,
        Integer.parseInt(resourceManager.getConfig("engine.view.winScene", "win.defaultSpacing")));
    return textBox;
  }

  @Override
  public VBox createOverlayButtonBox() {
    VBox buttonBox = new VBox();
    String[] buttonTexts = getWinButtonTexts();
    String[] buttonIDs = getWinButtonIDs();

    for (int i = 0; i < buttonIDs.length; i++) {
      Button currButton = new Button(buttonTexts[i]);
      currButton.getStyleClass()
          .add(resourceManager.getConfig("engine.view.winScene", "win.button.style"));
      buttonBox.getChildren().add(currButton);
    }

    int buttonSpacing = Integer.parseInt(
        resourceManager.getConfig("engine.view.winScene", "win.defaultSpacing"));
    alignBox(buttonBox, buttonSpacing);
    return buttonBox;
  }

  /**
   * Provides button texts for the win screen.
   *
   * @return array of strings for button strings
   */
  private String[] getWinButtonTexts() {
    return new String[]{
        resourceManager.getConfig("engine.view.winScene", "win.button.nextLevel.text"),
        resourceManager.getConfig("engine.view.winScene", "win.button.restartLevel.text"),
        resourceManager.getConfig("engine.view.winScene", "win.button.returnHome.text")
    };
  }

  /**
   * Provides button IDs for the win screen.
   *
   * @return array of strings for button IDs
   */
  private String[] getWinButtonIDs() {
    return new String[]{
        resourceManager.getConfig("engine.view.winScene", "win.button.nextLevel.id"),
        resourceManager.getConfig("engine.view.winScene", "win.button.restartLevel.id"),
        resourceManager.getConfig("engine.view.winScene", "win.button.returnHome.id")
    };
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    // No need to remove game objects from the win screen, as it is a static overlay
    // that doesn't support adding dynamic objects.
    // This method is intentionally left blank.
  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    throw new UnsupportedOperationException(
        resourceManager.getText("exceptions", "CannotRenderPlayerStats"));
  }

}
