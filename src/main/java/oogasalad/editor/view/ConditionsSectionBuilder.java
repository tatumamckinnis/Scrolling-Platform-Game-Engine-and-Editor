package oogasalad.editor.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.object.event.ExecutorData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the UI section for managing Conditions associated with an Event. Handles condition groups,
 * adding/removing conditions by type (String) and index, and provides UI for parameter editing.
 * Uses separate functional interfaces for handlers and a separate class for display items.
 */
public class ConditionsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(ConditionsSectionBuilder.class);

  private static final double LIST_VIEW_HEIGHT = 120.0;
  private static final String KEY_CONDITIONS_HEADER = "conditionsHeader";
  private static final String KEY_ADD_GROUP_BUTTON = "addGroupButton";
  private static final String KEY_REMOVE_GROUP_BUTTON = "removeGroupButton";
  private static final String KEY_ADD_CONDITION_BUTTON = "addConditionButton";
  private static final String KEY_REMOVE_CONDITION_BUTTON = "removeConditionButton";
  private static final String KEY_PARAMETERS_HEADER = "parametersHeader";
  private static final String PROMPT_SELECT_CONDITION = "Select Condition Type";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double DEFAULT_SPACING = 8.0;

  private final ResourceBundle uiBundle;
  private final Supplier<List<String>> conditionTypeSupplier;
  private final Runnable addGroupHandler;
  private final IntConsumer removeGroupHandler;
  private final AddConditionHandler addConditionHandler;
  private final RemoveConditionHandler removeConditionHandler;
  private final EditConditionParamHandler editConditionParamHandler;

  private ComboBox<String> conditionTypeComboBox;
  private ListView<ConditionDisplayItem> conditionsListView;
  private VBox parametersPane;


  /**
   * Constructs a builder for the conditions UI section.
   *
   * @param uiBundle                  Resource bundle for UI text localization.
   * @param conditionTypeSupplier     Supplier providing a List of available condition type names
   *                                  (Strings).
   * @param addGroupHandler           Runnable executed when the "Add Group" button is clicked.
   * @param removeGroupHandler        IntConsumer executed when "Remove Group" is clicked, accepting
   *                                  the group index.
   * @param addConditionHandler       Handler (implementing {@link AddConditionHandler}) executed
   *                                  when "Add Condition" is clicked.
   * @param removeConditionHandler    Handler (implementing {@link RemoveConditionHandler}) executed
   *                                  when "Remove Condition" is clicked.
   * @param editConditionParamHandler Handler (implementing {@link EditConditionParamHandler})
   *                                  executed when a parameter value is modified.
   * @throws NullPointerException if any argument is null.
   */
  public ConditionsSectionBuilder(ResourceBundle uiBundle,
      Supplier<List<String>> conditionTypeSupplier,
      Runnable addGroupHandler,
      IntConsumer removeGroupHandler,
      AddConditionHandler addConditionHandler,
      RemoveConditionHandler removeConditionHandler,
      EditConditionParamHandler editConditionParamHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.conditionTypeSupplier = Objects.requireNonNull(conditionTypeSupplier);
    this.addGroupHandler = Objects.requireNonNull(addGroupHandler);
    this.removeGroupHandler = Objects.requireNonNull(removeGroupHandler);
    this.addConditionHandler = Objects.requireNonNull(addConditionHandler);
    this.removeConditionHandler = Objects.requireNonNull(removeConditionHandler);
    this.editConditionParamHandler = Objects.requireNonNull(editConditionParamHandler);
  }

  /**
   * Builds and returns the complete UI Node for the conditions management section. This includes
   * controls for managing condition groups, selecting and adding/removing conditions, displaying
   * the list of conditions, and an area for editing parameters of the selected condition.
   *
   * @return The constructed {@code Node} representing the conditions UI section.
   */
  public Node build() {
    VBox sectionPane = new VBox(DEFAULT_SPACING);
    sectionPane.getStyleClass().add("input-sub-section");
    sectionPane.setPadding(new Insets(DEFAULT_PADDING));

    Label header = createHeaderLabel(KEY_CONDITIONS_HEADER);
    HBox groupButtonRow = createGroupButtonRow();
    HBox conditionSelectionRow = createConditionSelectionRow();
    setupConditionsListView();
    Node parametersSection = buildParametersSection();

    sectionPane.getChildren().addAll(
        header,
        groupButtonRow,
        conditionSelectionRow,
        conditionsListView,
        parametersSection
    );
    VBox.setVgrow(conditionsListView, Priority.SOMETIMES);
    VBox.setVgrow(parametersSection.getParent(), Priority.SOMETIMES);
    LOG.debug("Conditions section UI built.");
    return sectionPane;
  }

  /**
   * Creates the horizontal layout (HBox) containing buttons for adding and removing condition
   * groups.
   *
   * @return The {@code HBox} node with group management buttons ("Add Group", "Remove Selected
   * Group").
   */
  private HBox createGroupButtonRow() {
    Button addGroupButton = createButton(KEY_ADD_GROUP_BUTTON, e -> handleAddGroupAction());
    addGroupButton.setId("addGroupButton");

    Button removeGroupButton = createButton(KEY_REMOVE_GROUP_BUTTON,
        e -> handleRemoveGroupAction());
    removeGroupButton.setId("removeGroupButton");
    removeGroupButton.getStyleClass().add("remove-button");

    return createButtonBox(DEFAULT_SPACING, addGroupButton, removeGroupButton);
  }

  /**
   * Creates the horizontal layout (HBox) containing the condition type ComboBox and the buttons for
   * adding and removing individual conditions.
   *
   * @return The {@code HBox} node for selecting condition types and adding/removing conditions.
   */
  private HBox createConditionSelectionRow() {
    HBox selectionBox = new HBox(DEFAULT_SPACING / 2);
    selectionBox.setAlignment(Pos.CENTER_LEFT);

    setupConditionTypeComboBox();

    Button addConditionButton = createButton(KEY_ADD_CONDITION_BUTTON,
        e -> handleAddConditionAction());
    addConditionButton.setId("addConditionButton");
    addConditionButton.setMaxWidth(Double.MAX_VALUE);

    Button removeConditionButton = createButton(KEY_REMOVE_CONDITION_BUTTON,
        e -> handleRemoveConditionAction());
    removeConditionButton.setId("removeConditionButton");
    removeConditionButton.getStyleClass().add("remove-button");

    selectionBox.getChildren()
        .addAll(conditionTypeComboBox, addConditionButton, removeConditionButton);
    HBox.setHgrow(conditionTypeComboBox, Priority.ALWAYS);

    return selectionBox;
  }


  /**
   * Initializes and configures the ComboBox used for selecting condition types. Populates it with
   * values obtained from the {@code conditionTypeSupplier}.
   */
  private void setupConditionTypeComboBox() {
    conditionTypeComboBox = new ComboBox<>(
        FXCollections.observableArrayList(conditionTypeSupplier.get()));
    conditionTypeComboBox.setId("conditionTypeComboBox");
    conditionTypeComboBox.setPromptText(PROMPT_SELECT_CONDITION);
    conditionTypeComboBox.setMaxWidth(Double.MAX_VALUE);
  }

  /**
   * Initializes and configures the ListView that displays the conditions. Sets the cell factory to
   * display {@link ConditionDisplayItem} objects and adds a listener to update the parameters pane
   * whenever the selection changes.
   */
  private void setupConditionsListView() {
    conditionsListView = new ListView<>(); // Uses separate class
    conditionsListView.setId("conditionsListView");
    conditionsListView.setPrefHeight(LIST_VIEW_HEIGHT);
    conditionsListView.getStyleClass().add("data-list-view");

    conditionsListView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldVal, newVal) -> {
          updateParametersPane(newVal);
        });
  }

  /**
   * Creates the UI section dedicated to displaying and editing parameters of the currently selected
   * condition. This section includes a header and a scrollable pane containing the parameter
   * editors.
   *
   * @return A {@code Node} (specifically a VBox) acting as the container for the parameter editing
   * UI.
   */
  private Node buildParametersSection() {
    VBox container = new VBox(DEFAULT_SPACING / 2);
    Label header = createHeaderLabel(KEY_PARAMETERS_HEADER);
    parametersPane = new VBox(DEFAULT_SPACING / 2);
    parametersPane.setId("conditionParametersPane");
    parametersPane.setPadding(new Insets(DEFAULT_SPACING / 2, 0, 0, 0));

    ScrollPane scrollPane = new ScrollPane(parametersPane);
    scrollPane.setFitToWidth(true);
    scrollPane.setPrefHeight(80);

    container.getChildren().addAll(header, scrollPane);
    return container;
  }

  /**
   * Handles the action triggered by the "Add Group" button by invoking the
   * {@code addGroupHandler}.
   */
  private void handleAddGroupAction() {
    addGroupHandler.run();
  }

  /**
   * Handles the action triggered by the "Remove Group" button. If a condition is selected in the
   * ListView, it invokes the {@code removeGroupHandler} with the group index of the selected
   * condition. Otherwise, logs a warning.
   */
  private void handleRemoveGroupAction() {
    ConditionDisplayItem selected = conditionsListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeGroupHandler.accept(selected.groupIndex);
    } else {
      LOG.warn("No condition selected, cannot determine group to remove.");
    }
  }

  /**
   * Handles the action triggered by the "Add Condition" button. Retrieves the selected condition
   * type from the ComboBox. Determines the target group index (either from the selected item in the
   * ListView or defaulting to 0). Invokes the {@code addConditionHandler} with the target group
   * index and selected type.
   */
  private void handleAddConditionAction() {
    String selectedType = conditionTypeComboBox.getSelectionModel().getSelectedItem();
    if (selectedType == null || selectedType.trim().isEmpty()) {
      LOG.warn("No condition type selected.");
      return;
    }

    ConditionDisplayItem selectedItem = conditionsListView.getSelectionModel().getSelectedItem();
    int targetGroupIndex = (selectedItem != null) ? selectedItem.groupIndex : 0;

    addConditionHandler.handle(targetGroupIndex, selectedType.trim());
    conditionTypeComboBox.getSelectionModel().clearSelection();
  }


  /**
   * Handles the action triggered by the "Remove Condition" button. If a condition is selected in
   * the ListView, it invokes the {@code removeConditionHandler} with the group and condition
   * indices of the selected item. Otherwise, logs a warning.
   */
  private void handleRemoveConditionAction() {
    ConditionDisplayItem selected = conditionsListView.getSelectionModel().getSelectedItem();
    if (selected != null) {
      removeConditionHandler.handle(selected.groupIndex, selected.conditionIndex);
    } else {
      LOG.warn("No condition selected for removal.");
    }
  }

  /**
   * Updates the content of the parameters editing pane based on the currently selected condition.
   * Clears the pane if no condition is selected. Otherwise, dynamically creates Label-TextField
   * pairs for each String and Double parameter found in the selected condition's
   * {@link ExecutorData}. Attaches listeners to the TextFields to invoke the
   * {@code editConditionParamHandler} upon action (Enter key). // <-- Changed description
   *
   * @param selectedItem The currently selected {@link ConditionDisplayItem}, or {@code null} if
   *                     none is selected.
   */
  private void updateParametersPane(ConditionDisplayItem selectedItem) {
    parametersPane.getChildren().clear();
    if (selectedItem == null || selectedItem.data == null) {
      return;
    }

    ExecutorData data = selectedItem.data;
    GridPane grid = new GridPane();
    grid.setHgap(DEFAULT_SPACING / 2);
    grid.setVgap(DEFAULT_SPACING / 2);
    int rowIndex = 0;

    if (data.getStringParams() != null) {
      for (Map.Entry<String, String> entry : data.getStringParams().entrySet()) {
        Label nameLabel = new Label(entry.getKey() + " (String):");
        TextField valueField = new TextField(entry.getValue());

        valueField.setOnAction(event -> {
          String newValue = valueField.getText();
          System.out.println("!!! String setOnAction: Field Value = '" + newValue
              + "', Calling handler..."); // Keep debug log for now
          editConditionParamHandler.handle(selectedItem.groupIndex, selectedItem.conditionIndex,
              entry.getKey(), newValue);
        });

        grid.add(nameLabel, 0, rowIndex);
        grid.add(valueField, 1, rowIndex++);
        GridPane.setHgrow(valueField, Priority.ALWAYS);
      }
    }

    if (data.getDoubleParams() != null) {
      for (Map.Entry<String, Double> entry : data.getDoubleParams().entrySet()) {
        Label nameLabel = new Label(entry.getKey() + " (Double):");
        TextField valueField = new TextField(String.valueOf(entry.getValue()));
        valueField.setOnAction(event -> {
          String newValText = valueField.getText();
          System.out.println(
              "!!! Double setOnAction: Field Text = '" + newValText + "'"); // Keep debug log
          try {
            Double doubleVal = Double.parseDouble(newValText);
            System.out.println("!!! Double setOnAction: Parsed OK (" + doubleVal
                + "), Calling handler..."); // Keep debug log
            editConditionParamHandler.handle(selectedItem.groupIndex, selectedItem.conditionIndex,
                entry.getKey(), doubleVal);
          } catch (NumberFormatException e) {
            System.out.println(
                "!!! Double setOnAction: Caught NFE for '" + newValText + "'"); // Keep debug log
            LOG.warn("Invalid double format for param '{}' on ActionEvent: {}", entry.getKey(),
                newValText);
            valueField.setText(String.valueOf(entry.getValue())); // Reset field
            System.out.println(
                "!!! Double setOnAction: Field reset to " + entry.getValue()); // Keep debug log
          }
        });

        grid.add(nameLabel, 0, rowIndex);
        grid.add(valueField, 1, rowIndex++);
        GridPane.setHgrow(valueField, Priority.ALWAYS);
      }
    }
    parametersPane.getChildren().add(grid);
    if (selectedItem != null && selectedItem.data != null) {
      LOG.trace("Parameters pane updated for item Group {}, Index {}", selectedItem.groupIndex,
          selectedItem.conditionIndex);
    }
  }


  /**
   * Updates the conditions ListView to display the provided condition groups. Converts the nested
   * list structure (List of groups, each containing a List of ExecutorData) into a flat
   * ObservableList of {@link ConditionDisplayItem} suitable for the ListView. Clears the parameter
   * pane after updating the list, as the selection might become invalid.
   *
   * @param conditionGroups A {@code List<List<ExecutorData>>} representing the condition groups and
   *                        their conditions. Can be null or empty.
   */
  public void updateConditionsListView(List<List<ExecutorData>> conditionGroups) {
    ObservableList<ConditionDisplayItem> displayItems = FXCollections.observableArrayList();
    if (conditionGroups != null) {
      for (int i = 0; i < conditionGroups.size(); i++) {
        List<ExecutorData> group = conditionGroups.get(i);
        if (group != null) {
          for (int j = 0; j < group.size(); j++) {
            ExecutorData conditionData = group.get(j);
            if (conditionData != null) {
              displayItems.add(
                  new ConditionDisplayItem(i, j, conditionData)); // Uses separate class
            }
          }
        }
      }
    }
    conditionsListView.setItems(displayItems);
    updateParametersPane(null);
    LOG.trace("Conditions list view updated with {} items.", displayItems.size());
  }

  /**
   * Creates a styled header label using text retrieved from the resource bundle based on the
   * provided key.
   *
   * @param bundleKey The key corresponding to the header text in the resource bundle.
   * @return A styled {@code Label} configured as a section header.
   */
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }


  /**
   * Creates a styled button with text from the resource bundle and assigns the provided action
   * handler.
   *
   * @param bundleKey The key in the resource bundle for the button's text.
   * @param handler   The event handler to be executed when the button is clicked.
   * @return A configured and styled {@code Button}.
   */
  private Button createButton(String bundleKey,
      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(bundleKey));
    button.setOnAction(handler);
    button.getStyleClass().add("action-button");
    return button;
  }

  /**
   * Creates a horizontal layout (HBox) to hold one or more buttons, applying specified spacing and
   * aligning the buttons to the center-left.
   *
   * @param spacing The horizontal spacing between buttons within the HBox.
   * @param buttons The {@code Button} nodes to add to the HBox.
   * @return A configured {@code HBox} containing the buttons.
   */
  private HBox createButtonBox(double spacing, Button... buttons) {
    HBox buttonBox = new HBox(spacing);
    buttonBox.setAlignment(Pos.CENTER_LEFT);
    buttonBox.getChildren().addAll(buttons);
    return buttonBox;
  }
}
