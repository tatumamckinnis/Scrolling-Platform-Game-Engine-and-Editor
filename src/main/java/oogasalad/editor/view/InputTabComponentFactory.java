package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import oogasalad.editor.controller.InputDataManager;
import oogasalad.editor.model.data.event_enum.ConditionType;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.event.EditorEvent;

/**
 * The InputTabComponentFactory creates UI components for the input tab in the editor.
 * This tab allows users to define events with conditions and outcomes for game objects.
 * For example, in the DinoGame, users can create events like "jump when space key is pressed".
 * This implementation integrates with the DynamicVariable system to allow parameter selection.
 *
 * @author Tatum McKinnis
 */
public class InputTabComponentFactory {
  private static final String INPUT_TAB_PROPERTIES_FILEPATH = "/oogasalad/screens/inputTab.properties";
  private static final Properties inputTabProperties = new Properties();

  private InputDataManager inputAPI;
  private UUID currentObjectId;
  private DynamicVariableContainer dynamicVariables;

  private ListView<String> eventListView;
  private ComboBox<ConditionType> conditionComboBox;
  private ComboBox<OutcomeType> outcomeComboBox;
  private ListView<String> conditionsListView;
  private ListView<String> outcomesListView;
  private TextField eventIdField;
  private ComboBox<String> parameterComboBox;

  /**
   * Initialize the InputTabComponentFactory with properties and API
   *
   * @param inputAPI the API to interact with input data
   * @param dynamicVariables the container for dynamic variables
   */
  public InputTabComponentFactory(InputDataManager inputAPI, DynamicVariableContainer dynamicVariables) {
    this.inputAPI = inputAPI;
    this.dynamicVariables = dynamicVariables;

    try {
      InputStream stream = getClass().getResourceAsStream(INPUT_TAB_PROPERTIES_FILEPATH);
      if (stream != null) {
        inputTabProperties.load(stream);
      } else {
        System.err.println("Could not load input tab properties file");
      }
    } catch (IOException e) {
      System.err.println("Error loading input tab properties: " + e.getMessage());
    }
  }

  /**
   * Set the current ID to a specific ID
   *
   * @param currentObjectId The id to set current to
   */
  public void setCurrentObjectId(UUID currentObjectId) {
    this.currentObjectId = currentObjectId;
  }

  /**
   * Create the main panel for the input tab
   *
   * @return Panel containing all input tab components
   */
  public Pane createInputTabPanel() {
    BorderPane mainPane = new BorderPane();
    mainPane.setPadding(new Insets(10));

    // Center section - Events, Conditions, and Outcomes
    BorderPane centerPane = new BorderPane();

    // Left side - Events list
    VBox eventsSection = createEventsSection();
    centerPane.setLeft(eventsSection);

    // Center/Right side - Conditions and Outcomes
    HBox conditionsOutcomesSection = createConditionsOutcomesSection();
    centerPane.setCenter(conditionsOutcomesSection);

    mainPane.setCenter(centerPane);

    return mainPane;
  }

  /**
   * Create the events section with list view and add/remove buttons
   *
   * @return VBox containing the events components
   */
  private VBox createEventsSection() {
    VBox eventsBox = new VBox(10);
    eventsBox.setPadding(new Insets(10));
    eventsBox.setPrefWidth(200);

    Label eventsLabel = new Label("Events");
    eventsLabel.getStyleClass().add("section-header");

    // Text field for event ID
    HBox eventIdBox = new HBox(5);
    Label eventIdLabel = new Label("Event ID:");
    eventIdField = new TextField();
    eventIdBox.getChildren().addAll(eventIdLabel, eventIdField);
    HBox.setHgrow(eventIdField, Priority.ALWAYS);

    // List view for events
    eventListView = new ListView<>();
    eventListView.setPrefHeight(200);

    // Add/Remove buttons
    HBox buttonBox = new HBox(10);
    buttonBox.setAlignment(Pos.CENTER);

    Button addEventButton = new Button("Add Event");
    Button removeEventButton = new Button("Remove Event");

    addEventButton.setOnAction(e -> addEvent());
    removeEventButton.setOnAction(e -> removeSelectedEvent());

    buttonBox.getChildren().addAll(addEventButton, removeEventButton);

    eventsBox.getChildren().addAll(eventsLabel, eventIdBox, eventListView, buttonBox);
    return eventsBox;
  }

  /**
   * Create the conditions and outcomes section with list views and combo boxes
   *
   * @return HBox containing conditions and outcomes components
   */
  private HBox createConditionsOutcomesSection() {
    HBox condOutBox = new HBox(20);
    condOutBox.setPadding(new Insets(10));

    // Conditions section
    VBox conditionsBox = new VBox(10);
    Label conditionsLabel = new Label("Conditions");
    conditionsLabel.getStyleClass().add("section-header");

    conditionComboBox = new ComboBox<>(FXCollections.observableArrayList(ConditionType.values()));
    conditionsListView = new ListView<>();
    conditionsListView.setPrefHeight(200);

    Button addConditionButton = new Button("Add Condition");
    Button removeConditionButton = new Button("Remove Condition");

    addConditionButton.setOnAction(e -> addCondition());
    removeConditionButton.setOnAction(e -> removeSelectedCondition());

    HBox condButtonBox = new HBox(10);
    condButtonBox.setAlignment(Pos.CENTER);
    condButtonBox.getChildren().addAll(addConditionButton, removeConditionButton);

    conditionsBox.getChildren().addAll(conditionsLabel, conditionComboBox, conditionsListView, condButtonBox);

    // Outcomes section
    VBox outcomesBox = new VBox(10);
    Label outcomesLabel = new Label("Outcomes");
    outcomesLabel.getStyleClass().add("section-header");

    outcomeComboBox = new ComboBox<>(FXCollections.observableArrayList(OutcomeType.values()));
    outcomesListView = new ListView<>();
    outcomesListView.setPrefHeight(200);

    // Parameter field for outcomes using dynamic variables
    HBox paramBox = new HBox(5);
    Label paramLabel = new Label("Parameter:");
    parameterComboBox = new ComboBox<>();
    updateParameterComboBox(); // Initialize with available variables

    Button createParameterButton = new Button("+");
    createParameterButton.setOnAction(e -> openAddDynamicVariableDialog());

    paramBox.getChildren().addAll(paramLabel, parameterComboBox, createParameterButton);
    HBox.setHgrow(parameterComboBox, Priority.ALWAYS);

    Button addOutcomeButton = new Button("Add Outcome");
    Button removeOutcomeButton = new Button("Remove Outcome");

    addOutcomeButton.setOnAction(e -> addOutcome());
    removeOutcomeButton.setOnAction(e -> removeSelectedOutcome());

    HBox outButtonBox = new HBox(10);
    outButtonBox.setAlignment(Pos.CENTER);
    outButtonBox.getChildren().addAll(addOutcomeButton, removeOutcomeButton);

    outcomesBox.getChildren().addAll(outcomesLabel, outcomeComboBox, paramBox, outcomesListView, outButtonBox);

    condOutBox.getChildren().addAll(conditionsBox, outcomesBox);
    return condOutBox;
  }

  /**
   * Add a new event to the current game object
   */
  private void addEvent() {
    if (currentObjectId == null || eventIdField.getText().isEmpty()) {
      return;
    }

    String eventId = eventIdField.getText();
    inputAPI.addEvent(currentObjectId, eventId);
    refreshEventsList();
    eventIdField.clear();
  }

  /**
   * Remove the selected event from the current game object
   */
  private void removeSelectedEvent() {
    if (currentObjectId == null || eventListView.getSelectionModel().isEmpty()) {
      return;
    }

    String selectedEventId = eventListView.getSelectionModel().getSelectedItem();
    inputAPI.removeEvent(currentObjectId, selectedEventId);
    refreshEventsList();
  }

  /**
   * Add a condition to the selected event
   */
  private void addCondition() {
    if (currentObjectId == null ||
        eventListView.getSelectionModel().isEmpty() ||
        conditionComboBox.getSelectionModel().isEmpty()) {
      return;
    }

    String eventId = eventListView.getSelectionModel().getSelectedItem();
    ConditionType condition = conditionComboBox.getSelectionModel().getSelectedItem();

    inputAPI.addEventCondition(currentObjectId, eventId, condition);
    refreshConditionsList(eventId);
  }

  /**
   * Remove the selected condition from the current event
   */
  private void removeSelectedCondition() {
    if (currentObjectId == null ||
        eventListView.getSelectionModel().isEmpty() ||
        conditionsListView.getSelectionModel().isEmpty()) {
      return;
    }

    String eventId = eventListView.getSelectionModel().getSelectedItem();
    String conditionStr = conditionsListView.getSelectionModel().getSelectedItem();
    ConditionType condition = ConditionType.valueOf(conditionStr);

    inputAPI.removeEventCondition(currentObjectId, eventId, condition);
    refreshConditionsList(eventId);
  }

  /**
   * Add an outcome to the selected event with a parameter from dynamic variables
   */
  private void addOutcome() {
    if (currentObjectId == null ||
        eventListView.getSelectionModel().isEmpty() ||
        outcomeComboBox.getSelectionModel().isEmpty()) {
      return;
    }

    String eventId = eventListView.getSelectionModel().getSelectedItem();
    OutcomeType outcome = outcomeComboBox.getSelectionModel().getSelectedItem();

    // Get the selected parameter from dynamic variables
    String parameterName = parameterComboBox.getValue();

    // Here we would need to extend the API to handle parameters for outcomes
    // For now, we'll just add the outcome without parameter support in the API
    inputAPI.addEventOutcome(currentObjectId, eventId, outcome);

    // In a full implementation, we would store the parameter with the outcome
    // inputAPI.setInputEventOutcomeParameter(currentObjectId, eventId, outcome, parameterName);

    refreshOutcomesList(eventId);
  }

  /**
   * Remove the selected outcome from the current event
   */
  private void removeSelectedOutcome() {
    if (currentObjectId == null ||
        eventListView.getSelectionModel().isEmpty() ||
        outcomesListView.getSelectionModel().isEmpty()) {
      return;
    }

    String eventId = eventListView.getSelectionModel().getSelectedItem();
    String outcomeStr = outcomesListView.getSelectionModel().getSelectedItem();
    OutcomeType outcome = OutcomeType.valueOf(outcomeStr.split(" ")[0]);

    inputAPI.removeEventOutcome(currentObjectId, eventId, outcome);
    refreshOutcomesList(eventId);
  }

  /**
   * Refresh the events list for the current game object
   */
  /**
   * Refresh the events list for the current game object
   */
  private void refreshEventsList() {
    eventListView.getItems().clear();

    if (currentObjectId != null) {
      Map<String, EditorEvent> events = inputAPI.getEvents(currentObjectId);
      if (events != null) {
        eventListView.getItems().addAll(events.keySet());
      }
    }

    // Set up selection listener for events
    eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldEvent, newEvent) -> {
      if (newEvent != null) {
        refreshConditionsList(newEvent);
        refreshOutcomesList(newEvent);
      }
    });
  }

  /**
   * Refresh the conditions list for the given event
   *
   * @param eventId ID of the event to show conditions for
   */
  private void refreshConditionsList(String eventId) {
    conditionsListView.getItems().clear();

    if (currentObjectId != null && eventId != null) {
      var conditions = inputAPI.getEventConditions(currentObjectId, eventId);
      if (conditions != null) {
        conditions.forEach(condition ->
            conditionsListView.getItems().add(condition.toString()));
      }
    }
  }

  /**
   * Refresh the outcomes list for the given event
   *
   * @param eventId ID of the event to show outcomes for
   */
  private void refreshOutcomesList(String eventId) {
    outcomesListView.getItems().clear();

    if (currentObjectId != null && eventId != null) {
      var outcomes = inputAPI.getEventOutcomes(currentObjectId, eventId);
      if (outcomes != null) {
        outcomes.forEach(outcome -> {
          String parameter = inputAPI.getEventOutcomeParameter(
              currentObjectId, eventId, outcome);

          if (parameter != null && !parameter.isEmpty()) {
            outcomesListView.getItems().add(outcome.toString() + " (" + parameter + ")");
          } else {
            outcomesListView.getItems().add(outcome.toString());
          }
        });
      }
    }
  }

  /**
   * Update the parameter combo box with the current dynamic variables
   */
  private void updateParameterComboBox() {
    parameterComboBox.getItems().clear();

    if (dynamicVariables != null) {
      dynamicVariables.getAllVariables().forEach(var ->
          parameterComboBox.getItems().add(var.toString()));
    }

    if (!parameterComboBox.getItems().isEmpty()) {
      parameterComboBox.setValue(parameterComboBox.getItems().get(0));
    }
  }

  /**
   * Open a dialog to add a new dynamic variable
   */
  private void openAddDynamicVariableDialog() {
    if (dynamicVariables == null) {
      return;
    }

    Dialog<DynamicVariable> dialog = new Dialog<>();
    dialog.setTitle("Add Dynamic Variable");

    // Set the button types
    ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    // Create a grid for the input fields
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField nameField = new TextField();
    nameField.setPromptText("Name");

    ComboBox<String> typeComboBox = new ComboBox<>(
        FXCollections.observableArrayList("int", "double", "boolean", "string"));
    typeComboBox.setValue("double");

    TextField valueField = new TextField();
    valueField.setPromptText("Value");

    TextField descriptionField = new TextField();
    descriptionField.setPromptText("Description");

    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Type:"), 0, 1);
    grid.add(typeComboBox, 1, 1);
    grid.add(new Label("Value:"), 0, 2);
    grid.add(valueField, 1, 2);
    grid.add(new Label("Description:"), 0, 3);
    grid.add(descriptionField, 1, 3);

    dialog.getDialogPane().setContent(grid);

    // Convert the result when the Add button is pressed
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        try {
          return new DynamicVariable(
              nameField.getText(),
              typeComboBox.getValue(),
              valueField.getText(),
              descriptionField.getText()
          );
        } catch (IllegalArgumentException ex) {
          // Show an error alert if input is invalid
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("Invalid Input");
          alert.setHeaderText(null);
          alert.setContentText(ex.getMessage());
          alert.showAndWait();
        }
      }
      return null;
    });

    dialog.showAndWait().ifPresent(dynamicVar -> {
      dynamicVariables.addVariable(dynamicVar);
      updateParameterComboBox();
    });
  }
}
