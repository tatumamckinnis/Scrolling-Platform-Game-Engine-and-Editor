package oogasalad.engine.view.components;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class holds a panel of buttons such as a home button and a play/pause button.
 *
 * @author Aksel Bell
 */
public class GameControlPanel extends Display {
  private static final Logger LOG = LogManager.getLogger();
  private List<Button> buttons;
  private String homeButtonID = "levelHomeButton";
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
    buttons = new ArrayList<>();
    this.viewState = viewState;
    this.setViewOrder(-1);
    createHomeButton();
  }

  /**
   * @see Display#initialRender()
   * Adds all buttons a container.
   */
  @Override
  public void initialRender() {
    HBox buttonContainer = new HBox();
    buttonContainer.getChildren().addAll(buttons);
    int containerSpacing = Integer.parseInt(engineComponentProperties.getProperty("gameControlPanel.button.spacing"));
    int containerLayoutX = Integer.parseInt(engineComponentProperties.getProperty("gameControlPanel.button.layoutX"));
    buttonContainer.setSpacing(containerSpacing);
    buttonContainer.setLayoutX(containerLayoutX);

    this.getChildren().add(buttonContainer);
  }

  private void createHomeButton() {
    Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(
            engineComponentProperties.getProperty("gameControlPanel.image.home"))));
    ImageView imageView = new ImageView(image);

    int homeFitWidth = Integer.parseInt(engineComponentProperties.getProperty("gameControlPanel.image.home.width"));
    int homeFitHeight = Integer.parseInt(engineComponentProperties.getProperty("gameControlPanel.image.home.height"));

    imageView.setFitWidth(homeFitWidth);
    imageView.setFitHeight(homeFitHeight);
    Button homeButton = new Button();
    homeButton.setGraphic(imageView);
    homeButton.setMinSize(homeFitWidth, homeFitHeight);
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    homeButton.setOnAction(event -> {
      factory.getAction(homeButtonID).run();
    });

    homeButton.setFocusTraversable(false);
    homeButton.setViewOrder(-1);
    buttons.add(homeButton);
  }
}
