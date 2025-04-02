package oogasalad.engine.view.components;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import oogasalad.engine.view.Display;

/**
 * This class holds a panel of buttons such as a home button and a play/pause button.
 *
 * @author Aksel Bell
 */
public class GameControlPanel extends Display {
  private List<Button> buttons;
  private Runnable onHomeClicked;
  // TODO make it read from a property file the type of button, the image of the button

  /**
   * Adds initial buttons to the control panel.
   */
  public GameControlPanel() {
    buttons = new ArrayList<>();
    createHomeButton();
  }

  /**
   * @see Display#render()
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
    homeButton.setOnAction(event -> {
      if (onHomeClicked != null) {
        onHomeClicked.run();
      }
    });

    buttons.add(homeButton);
  }

  /**
   * Defines what happens when the home button is clicked.
   * @param onHomeClicked function to be triggered when home button clicked.
   */
  public void setOnHomeClicked(Runnable onHomeClicked) {
    this.onHomeClicked = onHomeClicked;
  }
}
