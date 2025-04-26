package oogasalad.editor.view.panes.properties;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorViewListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the "Properties" tab UI, displaying Identity & Hitbox data, etc. Implements
 * EditorViewListener to update whenever the selected object changes. Handles Group selection via ComboBox.
 *
 * @author Tatum McKinnis
 */
public class PropertiesTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(PropertiesTabComponentFactory.class);
  private static final String NUMERIC_REGEX = "\\d*";
  private static final String NO_GROUP_OPTION = "<None>";

  private final EditorController editorController;

  private TextField nameField;
  private ComboBox<String> groupComboBox;

  private TextField xField;
  private TextField yField;
  private TextField widthField;
  private TextField heightField;
  private TextField shapeField;

  private UUID currentObjectId;
  private boolean isUpdatingGroupComboBox = false;

  /**
   * Constructs a new factory for the Properties tab.
   *
   * @param editorController the main controller, must not be null.
   */
  public PropertiesTabComponentFactory(EditorController editorController) {
    this.editorController = Objects.requireNonNull(editorController, "EditorController cannot be null.");
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
   * Builds a VBox containing fields for Identity data: Name (TextField) + Group (ComboBox)
   */
  private VBox buildIdentitySection() {
    VBox box = new VBox(8);
    box.getStyleClass().add("input-sub-section");

    Label identityLabel = new Label("Identity");
    identityLabel.getStyleClass().add("section-header");

    nameField = createIdentityTextField("Name",
        (id, value) -> editorController.getEditorDataAPI().getIdentityDataAPI().setName(id, value));

    groupComboBox = new ComboBox<>();
    groupComboBox.setPromptText("Select Group");
    groupComboBox.setMaxWidth(Double.MAX_VALUE);
    groupComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (!isUpdatingGroupComboBox && currentObjectId != null && !Objects.equals(oldVal, newVal)) {
        String groupToSet = newVal;
        if (NO_GROUP_OPTION.equals(newVal)) {
          groupToSet = "";
        }

        LOG.debug("Group ComboBox value changed to: '{}' for object {}", groupToSet, currentObjectId);
        editorController.getEditorDataAPI().getIdentityDataAPI().setGroup(currentObjectId, groupToSet);

      }
    });

    box.getChildren()
        .addAll(identityLabel, new Label("Name"), nameField, new Label("Group"), groupComboBox);

    return box;
  }


  @Override
  public void setSnapToGrid(boolean doSnap) {

  }

  @Override
  public void setCellSize(int cellSize) {

  }

  /**
   * Factory method for creating identity text fields (Name, Group).
   */
  private TextField createIdentityTextField(String prompt, BiConsumer<UUID, String> setter) {
    TextField textField = new TextField();
    textField.setPromptText(prompt);
    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal && currentObjectId != null) {
        String currentValue = textField.getText();
        String modelValue = editorController.getEditorDataAPI().getIdentityDataAPI().getName(currentObjectId);
        if (!Objects.equals(currentValue, modelValue)) {
          LOG.debug("Name field focus lost. Updating object {} name to: {}", currentObjectId, currentValue);
          setter.accept(currentObjectId, currentValue);
        }
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
   * Attaches a listener that parses the input as an integer and updates the model via the setter
   * only when focus is lost.
   */
  private TextField createHitboxTextField(String promptText, BiConsumer<UUID, Integer> setter) {
    TextField textField = new TextField();
    textField.setPromptText(promptText);
    textField.textProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null && !newVal.matches(NUMERIC_REGEX)) {
        textField.setText(oldVal);
      }
    });
    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal && currentObjectId != null) {
        String currentValueStr = textField.getText();
        int value = parseSafeInt(currentValueStr);
        int modelValue = getModelHitboxValue(promptText, currentObjectId);
        if (value != modelValue) {
          LOG.debug("Hitbox field '{}' focus lost. Updating object {} to: {}", promptText, currentObjectId, value);
          setter.accept(currentObjectId, value);
        }
      }
    });
    return textField;
  }

  private int getModelHitboxValue(String property, UUID id) {
    try {
      switch (property) {
        case "X": return editorController.getEditorDataAPI().getHitboxDataAPI().getX(id);
        case "Y": return editorController.getEditorDataAPI().getHitboxDataAPI().getY(id);
        case "Width": return editorController.getEditorDataAPI().getHitboxDataAPI().getWidth(id);
        case "Height": return editorController.getEditorDataAPI().getHitboxDataAPI().getHeight(id);
        default: return 0;
      }
    } catch (Exception e) {
      LOG.warn("Could not get hitbox model value for property '{}', object {}: {}", property, id, e.getMessage());
      return 0;
    }
  }

  /**
   * Creates the TextField for the hitbox shape property. Updates on focus lost.
   */
  private TextField createHitboxShapeField() {
    TextField textField = new TextField();
    textField.setPromptText("Shape (e.g. RECTANGLE)");
    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal && currentObjectId != null) {
        String currentValue = textField.getText();
        String modelValue = editorController.getEditorDataAPI().getHitboxDataAPI().getShape(currentObjectId);
        if (!Objects.equals(currentValue, modelValue)) {
          LOG.debug("Shape field focus lost. Updating object {} shape to: {}", currentObjectId, currentValue);
          editorController.getEditorDataAPI().getHitboxDataAPI().setShape(currentObjectId, currentValue);
        }
      }
    });
    return textField;
  }

  /**
   * Safely parses a string into an integer. Returns 0 if parsing fails or the string is null/empty.
   */
  private int parseSafeInt(String s) {
    if (s == null || s.trim().isEmpty()) {
      return 0;
    }
    try {
      return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
      LOG.warn("Invalid integer format for hitbox property: '{}'. Using 0.", s);
      return 0;
    }
  }

  @Override
  public void onSelectionChanged(UUID selectedObjectId) {
    Platform.runLater(() -> {
      if (!Objects.equals(this.currentObjectId, selectedObjectId)) {
        LOG.debug("Selection changed to: {}", selectedObjectId);
        this.currentObjectId = selectedObjectId;
        refreshFields();
      }
    });
  }

  @Override
  public void onObjectUpdated(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      LOG.debug("Selected object {} updated, refreshing properties.", objectId);
      Platform.runLater(this::refreshFields);
    }
  }

  @Override
  public void onObjectRemoved(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      LOG.debug("Selected object {} removed, clearing properties.", objectId);
      this.currentObjectId = null;
      Platform.runLater(this::clearFields);
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

  @Override
  public void onPrefabsChanged() {
    LOG.debug("PropertiesTabComponentFactory notified of prefab changes (no direct action).");
  }

  @Override
  public void onSpriteTemplateChanged() {
    LOG.debug("PropertiesTabComponentFactory notified of sprite template changes (no direct action).");
  }


  /**
   * Refreshes all editable fields in the UI by fetching the latest data for the
   * selected object. If no object is selected, clears all fields. Ensures execution
   * on the JavaFX application thread.
   */
  private void refreshFields() {
    if (currentObjectId == null) {
      clearFieldsInternal();
      return;
    }
    LOG.debug("Refreshing properties fields for object {}", currentObjectId);
    try {
      populateIdentityFields();
      populateHitboxFields();
    } catch (Exception e) {
      LOG.error("Error refreshing properties fields for object {}: {}", currentObjectId, e.getMessage(), e);
      clearFieldsInternal();
    }
  }

  /**
   * Fetches and populates the identity fields (name and group ComboBox) for the selected object.
   * Handles updating the ComboBox items and selection.
   */
  private void populateIdentityFields() {
    String currentName = editorController.getEditorDataAPI().getIdentityDataAPI().getName(currentObjectId);
    nameField.setText(Objects.toString(currentName, ""));

    String currentGroup = editorController.getEditorDataAPI().getIdentityDataAPI().getGroup(currentObjectId);
    List<String> availableGroups = editorController.getEditorDataAPI().getGroups();
    ObservableList<String> groupOptions = FXCollections.observableArrayList();
    groupOptions.add(NO_GROUP_OPTION);
    groupOptions.addAll(availableGroups);

    isUpdatingGroupComboBox = true;
    groupComboBox.setItems(groupOptions);

    if (currentGroup != null && !currentGroup.isEmpty() && availableGroups.contains(currentGroup)) {
      groupComboBox.setValue(currentGroup);
    } else {
      groupComboBox.setValue(NO_GROUP_OPTION);
    }
    isUpdatingGroupComboBox = false;

    LOG.trace("Populated identity fields: Name='{}', Group='{}', Options={}", currentName, groupComboBox.getValue(), groupOptions);
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

    LOG.trace("Populated hitbox fields: X={}, Y={}, W={}, H={}, Shape='{}'", x, y, w, h, shape);
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
    LOG.debug("Clearing properties fields.");
    currentObjectId = null;

    nameField.setText("");

    isUpdatingGroupComboBox = true;
    groupComboBox.getItems().clear();
    groupComboBox.setValue(null);
    isUpdatingGroupComboBox = false;

    xField.setText("");
    yField.setText("");
    widthField.setText("");
    heightField.setText("");
    shapeField.setText("");
  }

  /**
   * Updates the items in the group ComboBox. Call this when the global list of groups changes.
   * This might be called from an onGroupsChanged listener method if implemented.
   */
  private void updateGroupComboBoxItems() {
    LOG.debug("Updating group combo box items.");
    List<String> availableGroups = editorController.getEditorDataAPI().getGroups();
    ObservableList<String> groupOptions = FXCollections.observableArrayList();
    groupOptions.add(NO_GROUP_OPTION);
    groupOptions.addAll(availableGroups);

    String currentSelection = groupComboBox.getValue();

    isUpdatingGroupComboBox = true;
    groupComboBox.setItems(groupOptions);
    if (currentSelection != null && groupOptions.contains(currentSelection)) {
      groupComboBox.setValue(currentSelection);
    } else if (currentObjectId != null) {
      String objectGroup = editorController.getEditorDataAPI().getIdentityDataAPI().getGroup(currentObjectId);
      if (objectGroup != null && !objectGroup.isEmpty() && groupOptions.contains(objectGroup)) {
        groupComboBox.setValue(objectGroup);
      } else {
        groupComboBox.setValue(NO_GROUP_OPTION);
      }
    }
    else {
      groupComboBox.setValue(NO_GROUP_OPTION);
    }
    isUpdatingGroupComboBox = false;
  }
}