package oogasalad.editor.view.panes.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorViewListener;

/**
 * Builds the "Properties" tab UI, displaying Identity, Hitbox data, and custom object parameters.
 * Implements {@link EditorViewListener} to update whenever the selected object changes. Handles
 * Group selection via ComboBox and allows adding/removing string and double parameters for the
 * selected object.
 *
 * @author Tatum McKinnis, Billy McCune
 */
public class PropertiesTabComponentFactory implements EditorViewListener {

  private static final Logger LOG = LogManager.getLogger(PropertiesTabComponentFactory.class);
  private static final String NUMERIC_REGEX = "\\d*";
  private static final String DOUBLE_REGEX = "-?\\d*\\.?\\d*";
  private static final String NO_GROUP_OPTION = "<None>";
  private static final String PARAM_DISPLAY_REGEX = "^(.*?) \\((String|Double)\\) = .*$";
  private static final Pattern PARAM_KEY_PATTERN = Pattern.compile(PARAM_DISPLAY_REGEX);

  private final EditorController editorController;

  private TextField uuidField;
  private TextField nameField;
  private ComboBox<String> gameNameComboBox;
  private ComboBox<String> typeComboBox;
  private TextField customTypeField;
  private ComboBox<String> groupComboBox;

  private TextField xField;
  private TextField yField;
  private TextField widthField;
  private TextField heightField;
  private TextField shapeField;
  
  private ListView<String> displayedStatsListView;
  private ComboBox<String> availableStatsComboBox;
  private Button addStatButton;
  private Button removeStatButton;
  private VBox displayedStatsSection;
  private final ObservableList<String> displayedStatsItems = FXCollections.observableArrayList();

  private ListView<String> parametersListView;
  private TextField paramKeyField;
  private TextField paramStringValueField;
  private TextField paramDoubleValueField;
  private Button addStringParamButton;
  private Button addDoubleParamButton;
  private Button removeParamButton;

  private UUID currentObjectId;
  private boolean isUpdatingGroupComboBox = false;
  private final ObservableList<String> parameterItems = FXCollections.observableArrayList();

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
   * Creates the scrollable Pane that holds our Identity, Hitbox, and Parameters sections. This
   * method is called by the code that sets up the "Properties" tab in EditorComponentFactory.
   *
   * @return A {@link ScrollPane} containing the properties UI sections.
   */
  public ScrollPane createPropertiesPane() {
    VBox contentBox = new VBox(15);
    contentBox.setPadding(new Insets(15));
    contentBox.setAlignment(Pos.TOP_LEFT);
    contentBox.getStyleClass().add("input-section");

    VBox identitySection = buildIdentitySection();
    VBox hitboxSection = buildHitboxSection();
    VBox displayedStatsSection = buildDisplayedStatsSection();
    TitledPane parametersSection = buildParametersSection();

    contentBox.getChildren().addAll(identitySection, hitboxSection, displayedStatsSection, parametersSection);

    ScrollPane scrollPane = new ScrollPane(contentBox);
    scrollPane.setFitToWidth(true);
    scrollPane.setFitToHeight(false);
    return scrollPane;
  }

  /**
   * Builds a VBox containing fields for Identity data: Name (TextField) + Group (ComboBox).
   *
   * @return A {@link VBox} containing the identity UI controls.
   */
  private VBox buildIdentitySection() {
    VBox box = new VBox(8);
    box.getStyleClass().add("input-sub-section");

    Label identityLabel = new Label("Identity");
    identityLabel.getStyleClass().add("section-header");

    Label uuidLabel = new Label("Object UUID");
    uuidField = new TextField();
    uuidField.setEditable(false);
    uuidField.setFocusTraversable(false);
    uuidField.getStyleClass().add("uuid-field");

    nameField = createIdentityTextField("Name",
        (id, value) -> editorController.getEditorDataAPI().getIdentityDataAPI().setName(id, value));

    // Create the game name dropdown
    gameNameComboBox = new ComboBox<>();
    gameNameComboBox.setPromptText("Select Game");
    gameNameComboBox.setMaxWidth(Double.MAX_VALUE);
    gameNameComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && newVal != null && !Objects.equals(oldVal, newVal)) {
        LOG.debug("Game Name ComboBox value changed to: '{}' for object {}", newVal, currentObjectId);
        editorController.getEditorDataAPI().getIdentityDataAPI().setGame(currentObjectId, newVal);
      }
    });
    
    // Create the type ComboBox with fixed options
    typeComboBox = new ComboBox<>();
    typeComboBox.getItems().addAll("player", "custom");
    typeComboBox.setPromptText("Select Type");
    typeComboBox.setMaxWidth(Double.MAX_VALUE);
    typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (currentObjectId != null && newVal != null && !Objects.equals(oldVal, newVal)) {
        // If "custom" is selected, show the custom field but don't update type yet
        if ("custom".equals(newVal)) {
          customTypeField.setVisible(true);
          customTypeField.setManaged(true);
          // Only update if we have a custom value, otherwise wait for user input
          if (!customTypeField.getText().isEmpty()) {
            editorController.getEditorDataAPI().getIdentityDataAPI()
                .setType(currentObjectId, customTypeField.getText());
            LOG.debug("Set type to custom value '{}' for object {}", 
                customTypeField.getText(), currentObjectId);
          }
          
          // Hide the displayed stats section for non-player objects
          displayedStatsSection.setVisible(false);
          displayedStatsSection.setManaged(false);
        } else {
          // For built-in types like "player", update directly and hide custom field
          customTypeField.setVisible(false);
          customTypeField.setManaged(false);
          editorController.getEditorDataAPI().getIdentityDataAPI().setType(currentObjectId, newVal);
          LOG.debug("Set type to '{}' for object {}", newVal, currentObjectId);
          
          // Show the displayed stats section only for player objects
          boolean isPlayer = "player".equals(newVal);
          displayedStatsSection.setVisible(isPlayer);
          displayedStatsSection.setManaged(isPlayer);
          
          if (isPlayer) {
            // Populate available stats from parameters
            updateAvailableStatsComboBox();
          }
        }
      }
    });
    
    // Create the custom type field
    customTypeField = new TextField();
    customTypeField.setPromptText("Enter custom type...");
    customTypeField.setVisible(false);
    customTypeField.setManaged(false);
    customTypeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal && currentObjectId != null && "custom".equals(typeComboBox.getValue())) {
        String customType = customTypeField.getText();
        if (customType != null && !customType.trim().isEmpty()) {
          editorController.getEditorDataAPI().getIdentityDataAPI()
              .setType(currentObjectId, customType);
          LOG.debug("Custom type field focus lost. Updated object {} type to: {}", 
              currentObjectId, customType);
        }
      }
    });

    // Create a VBox to hold the type components
    VBox typeVBox = new VBox(5);
    typeVBox.getChildren().addAll(typeComboBox, customTypeField);
    HBox.setHgrow(typeVBox, Priority.ALWAYS);
    
    // Create an HBox to hold the type VBox and the player checkbox horizontally
    HBox typeContainer = new HBox(10);
    typeContainer.getChildren().addAll(typeVBox);
    HBox.setHgrow(typeVBox, Priority.ALWAYS);

    groupComboBox = new ComboBox<>();
    groupComboBox.setPromptText("Select Group");
    groupComboBox.setMaxWidth(Double.MAX_VALUE);
    groupComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (!isUpdatingGroupComboBox && currentObjectId != null && !Objects.equals(oldVal, newVal)) {
        String groupToSet = newVal;
        if (NO_GROUP_OPTION.equals(newVal)) {
          groupToSet = "";
        }

        LOG.debug("Group ComboBox value changed to: '{}' for object {}", groupToSet,
            currentObjectId);
        editorController.getEditorDataAPI().getIdentityDataAPI()
            .setGroup(currentObjectId, groupToSet);

      }
    });

    box.getChildren()
        .addAll(identityLabel, uuidLabel, uuidField,
            new Label("Name"), nameField,
            new Label("Game Name"), gameNameComboBox,
            new Label("Type"), typeContainer,
            new Label("Group"), groupComboBox);

    return box;
  }


  /**
   * Builds a VBox containing fields for Hitbox data: X, Y, Width, Height, Shape using factory
   * methods.
   *
   * @return A {@link VBox} containing the hitbox UI controls.
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
   * Builds a {@link TitledPane} containing controls for viewing, adding, and removing custom string
   * and double parameters associated with the selected object.
   *
   * @return A {@link TitledPane} for managing custom parameters.
   */
  private TitledPane buildParametersSection() {
    VBox container = new VBox(10);
    container.setPadding(new Insets(10));

    parametersListView = new ListView<>(parameterItems);
    parametersListView.setPlaceholder(new Label("No parameters defined"));
    parametersListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    parametersListView.setPrefHeight(150);

    removeParamButton = new Button("Remove Selected");
    removeParamButton.setOnAction(e -> removeSelectedParameter());
    removeParamButton.disableProperty()
        .bind(parametersListView.getSelectionModel().selectedItemProperty().isNull());
    HBox removeBox = new HBox(removeParamButton);
    removeBox.setAlignment(Pos.CENTER_RIGHT);

    GridPane addGrid = new GridPane();
    addGrid.setHgap(8);
    addGrid.setVgap(5);

    paramKeyField = new TextField();
    paramKeyField.setPromptText("Parameter Name");
    paramStringValueField = new TextField();
    paramStringValueField.setPromptText("String Value");
    paramDoubleValueField = new TextField();
    paramDoubleValueField.setPromptText("Double Value");
    paramDoubleValueField.textProperty().addListener((obs, ov, nv) -> {
      if (nv != null && !nv.matches(DOUBLE_REGEX)) {
        paramDoubleValueField.setText(ov);
      }
    });

    addStringParamButton = new Button("Add String");
    addStringParamButton.setOnAction(e -> addStringParameter());
    addDoubleParamButton = new Button("Add Double");
    addDoubleParamButton.setOnAction(e -> addDoubleParameter());

    addGrid.add(new Label("Key:"), 0, 0);
    addGrid.add(paramKeyField, 1, 0);
    GridPane.setHgrow(paramKeyField, Priority.ALWAYS);

    addGrid.add(new Label("String:"), 0, 1);
    addGrid.add(paramStringValueField, 1, 1);
    addGrid.add(addStringParamButton, 2, 1);
    GridPane.setHgrow(paramStringValueField, Priority.ALWAYS);

    addGrid.add(new Label("Double:"), 0, 2);
    addGrid.add(paramDoubleValueField, 1, 2);
    addGrid.add(addDoubleParamButton, 2, 2);
    GridPane.setHgrow(paramDoubleValueField, Priority.ALWAYS);

    container.getChildren().addAll(new Label("Current Parameters:"), parametersListView, removeBox,
        new Label("Add New Parameter:"), addGrid);

    TitledPane titledPane = new TitledPane("Custom Parameters", container);
    titledPane.setCollapsible(true);
    titledPane.setExpanded(true);
    return titledPane;
  }

  /**
   * Handles the action of adding a new string parameter based on the input fields. Performs basic
   * validation and calls the controller.
   */
  private void addStringParameter() {
    if (currentObjectId == null) {
      return;
    }
    String key = paramKeyField.getText();
    String value = paramStringValueField.getText();

    if (key == null || key.trim().isEmpty()) {
      showError("Parameter key cannot be empty.");
      return;
    }

    editorController.setObjectStringParameter(currentObjectId, key.trim(), value);
    paramKeyField.clear();
    paramStringValueField.clear();
  }

  /**
   * Handles the action of adding a new double parameter based on the input fields. Performs
   * validation (key not empty, value is a valid double) and calls the controller.
   */
  private void addDoubleParameter() {
    if (currentObjectId == null) {
      return;
    }
    String key = paramKeyField.getText();
    String valueStr = paramDoubleValueField.getText();

    if (key == null || key.trim().isEmpty()) {
      showError("Parameter key cannot be empty.");
      return;
    }
    if (valueStr == null || valueStr.trim().isEmpty()) {
      showError("Double value cannot be empty.");
      return;
    }

    try {
      double value = Double.parseDouble(valueStr.trim());
      editorController.setObjectDoubleParameter(currentObjectId, key.trim(), value);
      paramKeyField.clear();
      paramDoubleValueField.clear();
    } catch (NumberFormatException e) {
      showError("Invalid double value: " + valueStr);
    }
  }

  /**
   * Handles the action of removing the parameter selected in the list view. Parses the key from the
   * selected item string and calls the controller.
   */
  private void removeSelectedParameter() {
    if (currentObjectId == null) {
      return;
    }
    String selectedItem = parametersListView.getSelectionModel().getSelectedItem();
    if (selectedItem == null) {
      return;
    }

    Optional<String> keyOpt = parseKeyFromDisplayString(selectedItem);
    if (keyOpt.isPresent()) {
      editorController.removeObjectParameter(currentObjectId, keyOpt.get());
    } else {
      LOG.error("Could not parse key from selected parameter item: {}", selectedItem);
      showError("Could not determine parameter key to remove.");
    }
  }

  /**
   * Parses the parameter key from the display string format "key (Type) = value".
   *
   * @param displayString The string from the ListView item.
   * @return An Optional containing the key if parsed successfully, otherwise empty.
   */
  private Optional<String> parseKeyFromDisplayString(String displayString) {
    if (displayString == null) {
      return Optional.empty();
    }
    Matcher matcher = PARAM_KEY_PATTERN.matcher(displayString);
    if (matcher.matches()) {
      return Optional.of(matcher.group(1));
    }
    return Optional.empty();
  }


  /**
   * Factory method for creating identity text fields (Name). Updates model on focus lost.
   *
   * @param prompt The prompt text for the field.
   * @param setter The BiConsumer to call when updating the model.
   * @return A configured {@link TextField}.
   */
  private TextField createIdentityTextField(String prompt, BiConsumer<UUID, String> setter) {
    TextField textField = new TextField();
    textField.setPromptText(prompt);
    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal && currentObjectId != null) {
        String currentValue = textField.getText();
        String modelValue = editorController.getEditorDataAPI().getIdentityDataAPI()
            .getName(currentObjectId);
        if (!Objects.equals(currentValue, modelValue)) {
          LOG.debug("Name field focus lost. Updating object {} name to: {}", currentObjectId,
              currentValue);
          setter.accept(currentObjectId, currentValue);
        }
      }
    });
    return textField;
  }


  /**
   * Factory method to create a TextField for a numeric hitbox property (X, Y, Width, Height).
   * Attaches a listener that parses the input as an integer and updates the model via the setter
   * only when focus is lost. Includes basic numeric input filtering.
   *
   * @param promptText The prompt text for the field.
   * @param setter     The BiConsumer to call when updating the model.
   * @return A configured {@link TextField} for numeric hitbox input.
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
          LOG.debug("Hitbox field '{}' focus lost. Updating object {} to: {}", promptText,
              currentObjectId, value);
          setter.accept(currentObjectId, value);
        }
      }
    });
    return textField;
  }

  /**
   * Helper to get the current value of a numeric hitbox property from the model. Used for
   * comparison before updating to avoid unnecessary updates.
   *
   * @param property The name of the property ("X", "Y", "Width", "Height").
   * @param id       The UUID of the object.
   * @return The current integer value from the model, or 0 on error.
   */
  private int getModelHitboxValue(String property, UUID id) {
    try {
      switch (property) {
        case "X":
          return editorController.getEditorDataAPI().getHitboxDataAPI().getX(id);
        case "Y":
          return editorController.getEditorDataAPI().getHitboxDataAPI().getY(id);
        case "Width":
          return editorController.getEditorDataAPI().getHitboxDataAPI().getWidth(id);
        case "Height":
          return editorController.getEditorDataAPI().getHitboxDataAPI().getHeight(id);
        default:
          LOG.warn("Unknown hitbox property requested: {}", property);
          return 0;
      }
    } catch (Exception e) {
      LOG.warn("Could not get hitbox model value for property '{}', object {}: {}", property, id,
          e.getMessage());
      return 0;
    }
  }

  /**
   * Creates the TextField for the hitbox shape property. Updates on focus lost.
   *
   * @return A configured {@link TextField} for the shape property.
   */
  private TextField createHitboxShapeField() {
    TextField textField = new TextField();
    textField.setPromptText("Shape (e.g. RECTANGLE)");
    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal && currentObjectId != null) {
        String currentValue = textField.getText();
        String modelValue = editorController.getEditorDataAPI().getHitboxDataAPI()
            .getShape(currentObjectId);
        if (!Objects.equals(currentValue, modelValue)) {
          LOG.debug("Shape field focus lost. Updating object {} shape to: {}", currentObjectId,
              currentValue);
          editorController.getEditorDataAPI().getHitboxDataAPI()
              .setShape(currentObjectId, currentValue);
        }
      }
    });
    return textField;
  }

  /**
   * Safely parses a string into an integer. Returns 0 if parsing fails or the string is null/empty.
   * Logs a warning on parsing failure.
   *
   * @param s The string to parse.
   * @return The parsed integer, or 0 on failure.
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

  /**
   * Displays an error alert dialog to the user.
   *
   * @param message The error message to display.
   */
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Error");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  /**
   * {@inheritDoc} Updates the currently selected object ID and refreshes all fields.
   */
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

  /**
   * {@inheritDoc} If the updated object is the currently selected one, refreshes all fields.
   */
  @Override
  public void onObjectUpdated(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      LOG.debug("Selected object {} updated, refreshing properties.", objectId);
      Platform.runLater(this::refreshFields);
    }
  }

  /**
   * {@inheritDoc} If the removed object is the currently selected one, clears the selection and all
   * fields.
   */
  @Override
  public void onObjectRemoved(UUID objectId) {
    if (Objects.equals(this.currentObjectId, objectId)) {
      LOG.debug("Selected object {} removed, clearing properties.", objectId);
      this.currentObjectId = null;
      Platform.runLater(this::clearFields);
    }
  }

  /**
   * {@inheritDoc} No action needed in this component.
   */
  @Override
  public void onObjectAdded(UUID objectId) {
    LOG.trace("PropertiesTab received: onObjectAdded {}", objectId);
  }

  /**
   * {@inheritDoc} No action needed in this component for global dynamic variables.
   */
  @Override
  public void onDynamicVariablesChanged() {
    LOG.trace("PropertiesTab received: onDynamicVariablesChanged (no direct action needed)");
  }

  /**
   * {@inheritDoc} Logs the error message.
   */
  @Override
  public void onErrorOccurred(String errorMessage) {
    LOG.warn("PropertiesTab received: onErrorOccurred: {}", errorMessage);
  }

  /**
   * {@inheritDoc} No action needed in this component.
   */
  @Override
  public void onPrefabsChanged() {
    LOG.debug("PropertiesTabComponentFactory notified of prefab changes (no direct action).");
  }

  /**
   * {@inheritDoc} No action needed in this component.
   */
  @Override
  public void onSpriteTemplateChanged() {
    LOG.debug(
        "PropertiesTabComponentFactory notified of sprite template changes (no direct action).");
  }


  /**
   * {@inheritDoc} No action needed in this component.
   */
  @Override
  public void setSnapToGrid(boolean doSnap) {
    //No action needed in this component
  }

  /**
   * {@inheritDoc} No action needed in this component.
   */
  @Override
  public void setCellSize(int cellSize) {
    //No action needed in this component
  }

  /**
   * Refreshes all editable fields in the UI (Identity, Hitbox, Parameters) by fetching the latest
   * data for the currently selected object from the controller. If no object is selected, clears
   * all fields. Ensures execution on the JavaFX application thread.
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
      refreshParameterList();
    } catch (Exception e) {
      LOG.error("Error refreshing properties fields for object {}: {}", currentObjectId,
          e.getMessage(), e);
      clearFieldsInternal();
    }
  }

  /**
   * Fetches and populates the identity fields (name and group ComboBox) for the selected object.
   * Handles updating the ComboBox items and selection state carefully to avoid triggering
   * listeners.
   */
  private void populateIdentityFields() {
    uuidField.setText(currentObjectId.toString());

    String currentName = editorController.getEditorDataAPI().getIdentityDataAPI()
        .getName(currentObjectId);
    nameField.setText(Objects.toString(currentName, ""));

    // Populate and set game name dropdown
    String currentGame = editorController.getEditorDataAPI().getIdentityDataAPI()
        .getObjectGame(currentObjectId);
    List<String> availableGames = editorController.getEditorDataAPI().getGames();
    gameNameComboBox.getItems().clear();
    gameNameComboBox.getItems().addAll(availableGames);
    
    if (currentGame != null && !currentGame.isEmpty() && availableGames.contains(currentGame)) {
      gameNameComboBox.setValue(currentGame);
    } else if (!availableGames.isEmpty()) {
      // Default to first available game if current game not set or invalid
      gameNameComboBox.setValue(availableGames.get(0));
    }

    // Set type field
    String currentType = editorController.getEditorDataAPI().getIdentityDataAPI().getType(currentObjectId);
    
    // Check if current type is "player" or something custom
    if (currentType == null || currentType.isEmpty()) {
      typeComboBox.setValue(null);
      customTypeField.setText("");
      customTypeField.setVisible(false);
      customTypeField.setManaged(false);
      displayedStatsSection.setVisible(false);
      displayedStatsSection.setManaged(false);
    } else if ("player".equals(currentType)) {
      typeComboBox.setValue("player");
      customTypeField.setVisible(false);
      customTypeField.setManaged(false);
      
      // Show and populate displayed stats section for player objects
      displayedStatsSection.setVisible(true);
      displayedStatsSection.setManaged(true);
      updateAvailableStatsComboBox();
    } else {
      // For any other type, set to custom
      typeComboBox.setValue("custom");
      customTypeField.setText(currentType);
      customTypeField.setVisible(true);
      customTypeField.setManaged(true);
      displayedStatsSection.setVisible(false);
      displayedStatsSection.setManaged(false);
    }

    // Populate and set group dropdown
    List<String> availableGroups = editorController.getEditorDataAPI().getGroups();
    ObservableList<String> groupOptions = FXCollections.observableArrayList();
    groupOptions.add(NO_GROUP_OPTION);
    groupOptions.addAll(availableGroups);

    isUpdatingGroupComboBox = true;
    groupComboBox.setItems(groupOptions);

    String currentGroup = editorController.getEditorDataAPI().getIdentityDataAPI()
        .getGroup(currentObjectId);
    if (currentGroup != null && !currentGroup.isEmpty() && availableGroups.contains(currentGroup)) {
      groupComboBox.setValue(currentGroup);
    } else {
      groupComboBox.setValue(NO_GROUP_OPTION);
    }
    isUpdatingGroupComboBox = false;

    LOG.trace("Populated identity fields: Name='{}', Game='{}', Type='{}', Group='{}', Options={}", 
        currentName, currentGame, currentType, groupComboBox.getValue(), groupOptions);
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
   * Clears and repopulates the parameters list view based on the currently selected object's string
   * and double parameters fetched from the controller. Sorts the parameters alphabetically by key.
   */
  private void refreshParameterList() {
    parameterItems.clear();
    if (currentObjectId == null) {
      return;
    }

    List<String> combinedParams = new ArrayList<>();
    try {
      Map<String, String> stringParams = editorController.getObjectStringParameters(
          currentObjectId);
      if (stringParams != null) {
        stringParams.forEach(
            (key, value) -> combinedParams.add(String.format("%s (String) = %s", key, value)));
      }

      Map<String, Double> doubleParams = editorController.getObjectDoubleParameters(
          currentObjectId);
      if (doubleParams != null) {
        doubleParams.forEach(
            (key, value) -> combinedParams.add(String.format("%s (Double) = %s", key, value)));
      }

      Collections.sort(combinedParams);
      parameterItems.addAll(combinedParams);
      LOG.trace("Refreshed parameter list for object {}: {} items.", currentObjectId,
          parameterItems.size());
          
      // If this is a player object, update the available stats dropdown
      String type = editorController.getEditorDataAPI().getIdentityDataAPI().getType(currentObjectId);
      if ("player".equals(type) && displayedStatsSection.isVisible()) {
        // Update available parameters in stats dropdown
        updateAvailableStatsComboBox();
      }

    } catch (Exception e) {
      LOG.error("Failed to refresh parameter list for object {}: {}", currentObjectId,
          e.getMessage(), e);
    }
  }


  /**
   * Clears all the property fields (Identity, Hitbox, Parameters). Ensures execution on the JavaFX
   * Application thread.
   */
  private void clearFields() {
    Platform.runLater(this::clearFieldsInternal);
  }

  /**
   * Clears all property fields and lists when no object is selected or an error occurs. Called only
   * internally from the refresh methods.
   */
  private void clearFieldsInternal() {
    // Clear identity fields
    uuidField.clear();
    nameField.clear();
    gameNameComboBox.getItems().clear();
    typeComboBox.setValue(null);
    customTypeField.clear();
    customTypeField.setVisible(false);
    customTypeField.setManaged(false);
    
    // Clear displayed stats section
    displayedStatsItems.clear();
    availableStatsComboBox.getItems().clear();
    displayedStatsSection.setVisible(false);
    displayedStatsSection.setManaged(false);
    
    isUpdatingGroupComboBox = true;
    groupComboBox.getItems().clear();
    groupComboBox.getItems().add(NO_GROUP_OPTION);
    groupComboBox.setValue(NO_GROUP_OPTION);
    isUpdatingGroupComboBox = false;

    // Clear hitbox fields
    xField.clear();
    yField.clear();
    widthField.clear();
    heightField.clear();
    shapeField.clear();

    // Clear parameter lists
    parameterItems.clear();

    currentObjectId = null;
    LOG.debug("All property fields cleared.");
  }

  /**
   * Updates the items in the group ComboBox based on the global list from the controller. Preserves
   * the current selection if possible, otherwise defaults based on the selected object's group or
   * the "<None>" option. This might be called from an `onGroupsChanged` listener method if
   * implemented.
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
      String objectGroup = editorController.getEditorDataAPI().getIdentityDataAPI()
          .getGroup(currentObjectId);
      if (objectGroup != null && !objectGroup.isEmpty() && groupOptions.contains(objectGroup)) {
        groupComboBox.setValue(objectGroup);
      } else {
        groupComboBox.setValue(NO_GROUP_OPTION);
      }
    } else {
      groupComboBox.setValue(NO_GROUP_OPTION);
    }
    isUpdatingGroupComboBox = false;
  }

  /**
   * Builds a VBox containing the displayed stats section for player objects.
   * This section allows selecting which parameters should be displayed as stats in the game.
   * Only appears when the object type is "player".
   *
   * @return A {@link VBox} containing the displayed stats controls.
   */
  private VBox buildDisplayedStatsSection() {
    displayedStatsSection = new VBox(8);
    displayedStatsSection.getStyleClass().add("input-sub-section");
    displayedStatsSection.setVisible(false);
    displayedStatsSection.setManaged(false);

    Label statsLabel = new Label("Displayed Stats");
    statsLabel.getStyleClass().add("section-header");

    // List view showing currently selected stats to display
    displayedStatsListView = new ListView<>(displayedStatsItems);
    displayedStatsListView.setPlaceholder(new Label("No stats selected for display"));
    displayedStatsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    displayedStatsListView.setPrefHeight(120);

    // Dropdown for selecting parameters to add as stats
    availableStatsComboBox = new ComboBox<>();
    availableStatsComboBox.setPromptText("Select parameter to display");
    availableStatsComboBox.setMaxWidth(Double.MAX_VALUE);

    // Add button
    addStatButton = new Button("Add as Stat");
    addStatButton.setOnAction(e -> addDisplayedStat());
    addStatButton.disableProperty().bind(availableStatsComboBox.valueProperty().isNull());

    // Remove button
    removeStatButton = new Button("Remove");
    removeStatButton.setOnAction(e -> removeDisplayedStat());
    removeStatButton.disableProperty()
        .bind(displayedStatsListView.getSelectionModel().selectedItemProperty().isNull());

    // Layout for buttons
    HBox buttonBox = new HBox(10, addStatButton, removeStatButton);
    buttonBox.setAlignment(Pos.CENTER_RIGHT);

    // Instruction label
    Label instructionLabel = new Label(
        "Select which parameters should be displayed as stats for this player");
    instructionLabel.setWrapText(true);

    displayedStatsSection.getChildren().addAll(
        statsLabel,
        instructionLabel,
        new Label("Available Parameters:"),
        availableStatsComboBox,
        new Label("Selected Stats:"),
        displayedStatsListView,
        buttonBox
    );

    return displayedStatsSection;
  }

  /**
   * Adds the selected parameter to the displayed stats list.
   */
  private void addDisplayedStat() {
    if (currentObjectId == null) {
      return;
    }
    
    String selectedParam = availableStatsComboBox.getValue();
    if (selectedParam != null && !displayedStatsItems.contains(selectedParam)) {
      displayedStatsItems.add(selectedParam);
      updateDisplayedStats();
    }
  }

  /**
   * Removes the selected stat from the displayed stats list.
   */
  private void removeDisplayedStat() {
    if (currentObjectId == null) {
      return;
    }
    
    String selectedStat = displayedStatsListView.getSelectionModel().getSelectedItem();
    if (selectedStat != null) {
      displayedStatsItems.remove(selectedStat);
      updateDisplayedStats();
    }
  }
  
  /**
   * Updates the displayed stats string parameter on the current object.
   */
  private void updateDisplayedStats() {
    if (currentObjectId == null) {
      return;
    }
    
    // Convert the ObservableList to a String list and save in the appropriate format
    List<String> displayedStats = new ArrayList<>(displayedStatsItems);
    
    // Set the "displayedProperties" parameter
    editorController.setObjectStringParameter(currentObjectId, "displayedProperties", 
        String.join(",", displayedStats));
    
    LOG.debug("Updated displayed stats for player {}: {}", currentObjectId, displayedStats);
  }

  /**
   * Updates the available stats combo box with current parameters from the object.
   */
  private void updateAvailableStatsComboBox() {
    if (currentObjectId == null) {
      return;
    }
    
    // Clear existing items
    availableStatsComboBox.getItems().clear();
    
    // Get all parameters for this object
    List<String> paramOptions = new ArrayList<>();
    
    // Add string parameters
    Map<String, String> stringParams = editorController.getObjectStringParameters(currentObjectId);
    if (stringParams != null) {
      stringParams.keySet().forEach(key -> {
        if (!key.equals("displayedProperties")) { // Don't include our meta parameter
          paramOptions.add(key);
        }
      });
    }
    
    // Add double parameters
    Map<String, Double> doubleParams = editorController.getObjectDoubleParameters(currentObjectId);
    if (doubleParams != null) {
      paramOptions.addAll(doubleParams.keySet());
    }
    
    // Sort alphabetically
    Collections.sort(paramOptions);
    
    // Add to combo box
    availableStatsComboBox.getItems().addAll(paramOptions);
    
    // Load currently displayed stats
    loadDisplayedStats();
  }
  
  /**
   * Loads the currently selected displayed stats from the object's parameters.
   */
  private void loadDisplayedStats() {
    if (currentObjectId == null) {
      return;
    }
    
    // Clear current items
    displayedStatsItems.clear();
    
    // Get the displayedProperties string from parameters
    String displayedPropsString = editorController.getObjectStringParameters(currentObjectId)
        .get("displayedProperties");
    
    if (displayedPropsString != null && !displayedPropsString.isEmpty()) {
      // Split the comma-separated list and add each item
      String[] props = displayedPropsString.split(",");
      displayedStatsItems.addAll(Arrays.asList(props));
    }
  }
}