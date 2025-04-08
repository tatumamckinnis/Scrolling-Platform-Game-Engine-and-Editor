package oogasalad.engine.view.components;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import oogasalad.engine.controller.api.GameManagerAPI;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewAPI;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;

/**
 * This class holds a panel of buttons such as a home button and a play/pause button.
 *
 * @author Aksel Bell
 */
public class GameControlPanel extends Display {
  private List<Button> buttons;
  private String homeButtonID = "levelHomeButton";
  private ViewState viewState;
  // TODO make it read from a property file the type of button, the image of the button

  /**
   * Adds initial buttons to the control panel.
   */
  public GameControlPanel(ViewState viewState) {
    buttons = new ArrayList<>();
    this.viewState = viewState;
    createHomeButton();
  }

  /**
   * @see Display#render()
   * Adds all buttons a container.
   */
  @Override
  public void render() {
    HBox buttonContainer = new HBox();
    buttonContainer.getChildren().addAll(buttons);
    buttonContainer.setSpacing(10);
    buttonContainer.setLayoutX(340);

    this.getChildren().add(buttonContainer);
  }

  private void createHomeButton() {
    Image image = new Image("oogasalad/gameIcons/home.jpeg");
    ImageView imageView = new ImageView(image);
    imageView.setFitWidth(40);
    imageView.setFitHeight(40);

    Button homeButton = new Button();
    homeButton.setGraphic(imageView);
    homeButton.setMinSize(40, 40);
    ButtonActionFactory factory = new ButtonActionFactory(viewState);
    homeButton.setOnAction(event -> {
      factory.getAction(homeButtonID).run();
    });

    homeButton.setFocusTraversable(false);
    buttons.add(homeButton);
  }
}
