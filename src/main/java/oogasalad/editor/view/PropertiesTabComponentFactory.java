package oogasalad.editor.view;

import java.util.Objects;
import java.util.UUID;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the "Properties" tab UI, displaying Identity & Hitbox data, etc. Implements
 * EditorViewListener to update whenever the selected object changes.
 */
public class PropertiesTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(PropertiesTabComponentFactory.class);
  private static final String KEY_ERROR_SELECTION_NEEDED = "errorSelectionNeeded";
  private static final String KEY_ERROR_API_FAILURE = "errorApiFailureTitle";

  private final EditorController editorController;

  private TextField nameField;
  private TextField groupField;

  private TextField xField;
  private TextField yField;
  private TextField widthField;
  private TextField heightField;
  private TextField shapeField;

  private UUID currentObjectId;

  /**
   * Constructs a new factory for the Properties tab.
   *
   * @param editorController the main controller, must not be null.
   */
  public PropertiesTabComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    LOG.info("PropertiesTabComponentFactory initialized.");
  }

  /**
   * Creates the scrollable Pane that holds our Identity & Hitbox sections. This method is called by
   * the code that sets up the "Properties" tab in EditorComponentFactory.
   */
  public ScrollPane createPropertiesPane() {
    VBox contentBox = new VBox(15);
    contentBox.setPadding(new Insets(15));
    contentBox.setAlignment(Pos.TOP_LEFT);

    contentBox.getStyleClass().add("input-section"); //TODO: Change this to its own section

    VBox identitySection = buildIdentitySection();
    VBox hitboxSection = buildHitboxSection();

    contentBox.getChildren().addAll(identitySection, hitboxSection);

    ScrollPane scrollPane = new ScrollPane(contentBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    return scrollPane;
  }

  /**
   * Builds a VBox containing fields for Identity data: Name + Group
   */
  private VBox buildIdentitySection() {
    VBox box = new VBox(8);
    box.getStyleClass().add("input-sub-section");

    Label identityLabel = new Label("Identity");
    identityLabel.getStyleClass().add("section-header");

    nameField = new TextField();
    nameField.setPromptText("Name");
    nameField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null) {
        editorController.getEditorDataAPI().getIdentityDataAPI().setName(currentObjectId, newVal);
      }
    });

    groupField = new TextField();
    groupField.setPromptText("Group");
    groupField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null) {
        editorController.getEditorDataAPI().getIdentityDataAPI().setGroup(currentObjectId, newVal);
      }
    });

    // Add labels, text fields:
    box.getChildren().addAll(identityLabel,
        new Label("Name"), nameField,
        new Label("Group"), groupField
    );

    return box;
  }

  /**
   * Builds a VBox containing fields for Hitbox data: X, Y, Width, Height, Shape
   */
  private VBox buildHitboxSection() {
    VBox box = new VBox(8);
    box.getStyleClass().add("input-sub-section");

    Label hitboxLabel = new Label("Hitbox");
    hitboxLabel.getStyleClass().add("section-header");

    // TODO: Implement a factory instead
    xField = new TextField();
    xField.setPromptText("X");
    xField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && newVal.matches("\\d*")) {
        editorController.getEditorDataAPI().getHitboxDataAPI()
            .setX(currentObjectId, parseSafeInt(newVal));
      }
    });

    yField = new TextField();
    yField.setPromptText("Y");
    yField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && newVal.matches("\\d*")) {
        editorController.getEditorDataAPI().getHitboxDataAPI()
            .setY(currentObjectId, parseSafeInt(newVal));
      }
    });

    widthField = new TextField();
    widthField.setPromptText("Width");
    widthField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && newVal.matches("\\d*")) {
        editorController.getEditorDataAPI().getHitboxDataAPI()
            .setWidth(currentObjectId, parseSafeInt(newVal));
      }
    });

    heightField = new TextField();
    heightField.setPromptText("Height");
    heightField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && newVal.matches("\\d*")) {
        editorController.getEditorDataAPI().getHitboxDataAPI()
            .setHeight(currentObjectId, parseSafeInt(newVal));
      }
    });

    shapeField = new TextField();
    shapeField.setPromptText("Shape (e.g. RECTANGLE)");
    shapeField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null) {
        editorController.getEditorDataAPI().getHitboxDataAPI().setShape(currentObjectId, newVal);
      }
    });

    box.getChildren().addAll(hitboxLabel,
        new Label("X"), xField,
        new Label("Y"), yField,
        new Label("Width"), widthField,
        new Label("Height"), heightField,
        new Label("Shape"), shapeField
    );

    return box;
  }

  private int parseSafeInt(String s) {
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  // TODO: implement logging and stuff
  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    Platform.runLater(() -> {
      if (!Objects.equals(this.currentObjectId, selectedObjectId)) {
        this.currentObjectId = selectedObjectId;
        refreshFields();
      }
    });
  }

  @Override
  public void onObjectUpdated(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      refreshFields();
    }
  }

  @Override
  public void onObjectRemoved(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      this.currentObjectId = null;
      clearFields();
    }
  }

  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("InputTab received: onObjectAdded {}", objectId);
  }

  @Override
  public void onDynamicVariablesChanged() {
    // TODO: implement dynamics
  }

  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("InputTab received: onErrorOccurred: {}", errorMessage);
  }


  /**
   * Loads the Identity + Hitbox data from the model for the currently selected object, and displays
   * it in our fields.
   */
  private void refreshFields() {
    if (currentObjectId == null) {
      clearFields();
      return;
    }

    // Switch to the FX thread if we might not be on it
    Platform.runLater(() -> {
      // Identity
      String currentName = editorController.getEditorDataAPI().getIdentityDataAPI()
          .getName(currentObjectId);
      String currentGroup = editorController.getEditorDataAPI().getIdentityDataAPI()
          .getGroup(currentObjectId);

      nameField.setText(currentName == null ? "" : currentName);
      groupField.setText(currentGroup == null ? "" : currentGroup);

      // Hitbox
      int x = editorController.getEditorDataAPI().getHitboxDataAPI().getX(currentObjectId);
      int y = editorController.getEditorDataAPI().getHitboxDataAPI().getY(currentObjectId);
      int w = editorController.getEditorDataAPI().getHitboxDataAPI().getWidth(currentObjectId);
      int h = editorController.getEditorDataAPI().getHitboxDataAPI().getHeight(currentObjectId);
      String shape = editorController.getEditorDataAPI().getHitboxDataAPI()
          .getShape(currentObjectId);

      xField.setText(String.valueOf(x));
      yField.setText(String.valueOf(y));
      widthField.setText(String.valueOf(w));
      heightField.setText(String.valueOf(h));
      shapeField.setText(shape == null ? "" : shape);
    });
  }

  private void clearFields() {
    Platform.runLater(() -> {
      nameField.setText("");
      groupField.setText("");
      xField.setText("");
      yField.setText("");
      widthField.setText("");
      heightField.setText("");
      shapeField.setText("");
    });
  }

}
