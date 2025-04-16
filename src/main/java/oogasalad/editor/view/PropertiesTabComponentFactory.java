package oogasalad.editor.view;

import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
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
  private static final String NUMERIC_REGEX = "\\d*";

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

    contentBox.getStyleClass().add("input-section");

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

    nameField = createIdentityTextField("Name",
        (id, value) -> editorController.getEditorDataAPI().getIdentityDataAPI().setName(id, value));
    groupField = createIdentityTextField("Group",
        (id, value) -> editorController.getEditorDataAPI().getIdentityDataAPI()
            .setGroup(id, value));

    box.getChildren()
        .addAll(identityLabel, new Label("Name"), nameField, new Label("Group"), groupField);

    return box;
  }

  @Override
  public void onPrefabsChanged() {
    LOG.debug("PropertiesTabComponentFactory notified of prefab changes.");
  }

  /**
   * Factory method for creating identity text fields (Name, Group).
   */
  private TextField createIdentityTextField(String prompt, BiConsumer<UUID, String> setter) {
    TextField textField = new TextField();
    textField.setPromptText(prompt);
    textField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null) {
        setter.accept(currentObjectId, newVal);
      }
    });
    return textField;
  }


  /**
   * Builds a VBox containing fields for Hitbox data: X, Y, Width, Height, Shape using factory
   * methods.
   */
  private VBox buildHitboxSection() {
    VBox box = new VBox(8);
    box.getStyleClass().add("input-sub-section");

    Label hitboxLabel = new Label("Hitbox");
    hitboxLabel.getStyleClass().add("section-header");

    xField = createHitboxTextField("X",
        (id, value) -> editorController.getEditorDataAPI().getHitboxDataAPI().setX(id, value));
    yField = createHitboxTextField("Y",
        (id, value) -> editorController.getEditorDataAPI().getHitboxDataAPI().setY(id, value));
    widthField = createHitboxTextField("Width",
        (id, value) -> editorController.getEditorDataAPI().getHitboxDataAPI().setWidth(id, value));
    heightField = createHitboxTextField("Height",
        (id, value) -> editorController.getEditorDataAPI().getHitboxDataAPI().setHeight(id, value));
    shapeField = createHitboxShapeField();

    box.getChildren()
        .addAll(hitboxLabel, new Label("X"), xField, new Label("Y"), yField, new Label("Width"),
            widthField, new Label("Height"), heightField, new Label("Shape"), shapeField);

    return box;
  }

  /**
   * Factory method to create a TextField for a numeric hitbox property (X, Y, Width, Height).
   * Attaches a listener that parses the input as an integer and updates the model via the setter.
   *
   * @param promptText The prompt text for the TextField.
   * @param setter     A BiConsumer accepting the UUID and the parsed integer value to update the
   *                   model.
   * @return The configured TextField.
   */
  private TextField createHitboxTextField(String promptText, BiConsumer<UUID, Integer> setter) {
    TextField textField = new TextField();
    textField.setPromptText(promptText);
    textField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && (newVal == null || newVal.matches(NUMERIC_REGEX))) {
        int value = parseSafeInt(newVal);
        setter.accept(currentObjectId, value);
      }

    });
    return textField;
  }

  /**
   * Creates the TextField for the hitbox shape property. Attaches a listener that updates the model
   * with the string value.
   *
   * @return The configured TextField for the shape.
   */
  private TextField createHitboxShapeField() {
    TextField textField = new TextField();
    textField.setPromptText("Shape (e.g. RECTANGLE)");
    textField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null) {
        editorController.getEditorDataAPI().getHitboxDataAPI().setShape(currentObjectId, newVal);
      }
    });
    return textField;
  }

  /**
   * Safely parses a string into an integer. Returns 0 if parsing fails or the string is
   * null/empty.
   *
   * @param s The string to parse.
   * @return The parsed integer, or 0 on failure/empty input.
   */
  private int parseSafeInt(String s) {
    if (s == null || s.isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      LOG.warn("Invalid integer format for hitbox property: {}", s);
      return 0;
    }
  }


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
      Platform.runLater(this::refreshFields);
    }
  }

  @Override
  public void onObjectRemoved(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      this.currentObjectId = null;
      Platform.runLater(this::clearFields); // Ensure UI update on FX thread
    }
  }

  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("PropertiesTab received: onObjectAdded {}", objectId);
  }

  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("PropertiesTab received: onDynamicVariablesChanged (no direct action needed)");
  }

  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("PropertiesTab received: onErrorOccurred: {}", errorMessage);
  }


  /**
   * Refreshes all editable fields in the UI by fetching the latest data for the selected object. If
   * no object is selected, clears all fields. Runs on the JavaFX application thread.
   */
  private void refreshFields() {
    Platform.runLater(() -> {
      if (currentObjectId == null) {
        clearFieldsInternal();
        return;
      }
      try {
        populateIdentityFields();
        populateHitboxFields();
      } catch (Exception e) {
        LOG.error("Error refreshing properties fields for object {}: {}", currentObjectId,
            e.getMessage(), e);
        clearFieldsInternal();
      }
    });
  }

  /**
   * Fetches and populates the identity fields (name and group) for the selected object.
   */
  private void populateIdentityFields() {
    String currentName = editorController.getEditorDataAPI().getIdentityDataAPI()
        .getName(currentObjectId);
    String currentGroup = editorController.getEditorDataAPI().getIdentityDataAPI()
        .getGroup(currentObjectId);

    nameField.setText(Objects.toString(currentName, ""));
    groupField.setText(Objects.toString(currentGroup, ""));
  }

  /**
   * Fetches and populates the hitbox fields (X, Y, Width, Height, Shape) for the selected object.
   */
  private void populateHitboxFields() {
    int x = editorController.getEditorDataAPI().getHitboxDataAPI().getX(currentObjectId);
    int y = editorController.getEditorDataAPI().getHitboxDataAPI().getY(currentObjectId);
    int w = editorController.getEditorDataAPI().getHitboxDataAPI().getWidth(currentObjectId);
    int h = editorController.getEditorDataAPI().getHitboxDataAPI().getHeight(currentObjectId);
    String shape = editorController.getEditorDataAPI().getHitboxDataAPI().getShape(currentObjectId);

    xField.setText(String.valueOf(x));
    yField.setText(String.valueOf(y));
    widthField.setText(String.valueOf(w));
    heightField.setText(String.valueOf(h));
    shapeField.setText(Objects.toString(shape, ""));
  }


  /**
   * Clears all the property fields. Ensures execution on the JavaFX Application thread.
   */
  private void clearFields() {
    Platform.runLater(this::clearFieldsInternal);
  }

  /**
   * Internal method to clear fields, called on the FX thread.
   */
  private void clearFieldsInternal() {
    nameField.setText("");
    groupField.setText("");
    xField.setText("");
    yField.setText("");
    widthField.setText("");
    heightField.setText("");
    shapeField.setText("");
  }
}