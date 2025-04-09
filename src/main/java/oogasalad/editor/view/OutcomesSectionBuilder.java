package oogasalad.editor.view;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import oogasalad.editor.model.data.event_enum.OutcomeType;
import oogasalad.editor.model.data.object.DynamicVariable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Builds the UI section specifically for managing Outcomes associated with an Event within the
 * Input Tab. This includes selecting an OutcomeType, optionally associating a parameter (a
 * {@link DynamicVariable} by name), adding/removing outcomes, and providing a way to trigger the
 * creation of new parameters. Relies on handler functions passed during construction to delegate
 * actions.
 */
public class OutcomesSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(OutcomesSectionBuilder.class);

  private static final double LIST_VIEW_HEIGHT = 150.0;
  private static final String KEY_OUTCOMES_HEADER = "outcomesHeader";
  private static final String KEY_PARAMETER_LABEL = "parameterLabel";
  private static final String KEY_CREATE_PARAM_BUTTON = "createParamButton";
  private static final String KEY_ADD_OUTCOME_BUTTON = "addOutcomeButton";
  private static final String KEY_REMOVE_OUTCOME_BUTTON = "removeOutcomeButton";
  private static final String PROMPT_SELECT_OUTCOME = "Select Outcome Type";
  private static final String PROMPT_SELECT_PARAMETER = "Select Parameter (Optional)";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double DEFAULT_SPACING = 12.0;

  private final ResourceBundle uiBundle;
  private final BiConsumer<OutcomeType, String> addOutcomeHandler;
  private final Consumer<OutcomeType> removeOutcomeHandler;
  private final Runnable createParameterHandler;

  private ComboBox<OutcomeType> outcomeComboBox;
  private ComboBox<String> parameterComboBox;
  private ListView<String> outcomesListView;

  /**
   * Constructs a builder for the outcomes UI section.
   *
   * @param uiBundle               The resource bundle for localizing UI text. Must not be null.
   * @param addOutcomeHandler      A BiConsumer function called when 'Add' is clicked, passing the
   *                               selected OutcomeType and the selected parameter name (String, may
   *                               be null). Must not be null.
   * @param removeOutcomeHandler   A Consumer function called when 'Remove' is clicked, passing the
   *                               OutcomeType derived from the selected item in the list view. Must
   *                               not be null.
   * @param createParameterHandler A Runnable function called when the 'Create Parameter' (+) button
   *                               is clicked. Must not be null.
   * @throws NullPointerException if any argument is null.
   */
  public OutcomesSectionBuilder(ResourceBundle uiBundle,
      BiConsumer<OutcomeType, String> addOutcomeHandler,
      Consumer<OutcomeType> removeOutcomeHandler,
      Runnable createParameterHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.addOutcomeHandler = Objects.requireNonNull(addOutcomeHandler);
    this.removeOutcomeHandler = Objects.requireNonNull(removeOutcomeHandler);
    this.createParameterHandler = Objects.requireNonNull(createParameterHandler);
  }

  /**
   * Creates and lays out the UI components for the outcomes section. This includes a header, a
   * ComboBox for selecting outcome types, a row for selecting an optional parameter (with a button
   * to create new parameters), Add/Remove buttons, and a ListView to display the currently added
   * outcomes with their parameters.
   *
   * @return A Node (specifically a VBox) containing all the UI elements for this section.
   */
  public Node build() {
    VBox pane = new VBox(DEFAULT_SPACING);
    pane.getStyleClass().add("input-sub-section");
    pane.setPadding(new Insets(DEFAULT_PADDING));

    Label header = createHeaderLabel(KEY_OUTCOMES_HEADER);
    outcomeComboBox = new ComboBox<>(FXCollections.observableArrayList(OutcomeType.values()));
    outcomeComboBox.setPromptText(PROMPT_SELECT_OUTCOME);
    outcomeComboBox.setMaxWidth(Double.MAX_VALUE);
    outcomeComboBox.setId("outcomeTypeComboBox");

    Node parameterRow = createParameterSelectionRow();
    outcomesListView = createListView(LIST_VIEW_HEIGHT);
    outcomesListView.setId("outcomesListView");

    Button addButton = createButton(KEY_ADD_OUTCOME_BUTTON, e -> {
      OutcomeType selectedOutcome = outcomeComboBox.getSelectionModel().getSelectedItem();
      String selectedParameter = parameterComboBox.getValue();
      if (selectedOutcome != null) {
        addOutcomeHandler.accept(selectedOutcome, selectedParameter);
      } else {
        LOG.warn("Attempted to add null outcome type.");

      }
    });
    addButton.setId("addOutcomeButton");

    Button removeButton = createButton(KEY_REMOVE_OUTCOME_BUTTON, e -> {
      String selectedOutcomeStrWithParam = outcomesListView.getSelectionModel().getSelectedItem();
      if (selectedOutcomeStrWithParam != null) {
        try {
          String outcomeStr = selectedOutcomeStrWithParam.split(" \\(")[0];
          OutcomeType outcome = OutcomeType.valueOf(outcomeStr);
          removeOutcomeHandler.accept(outcome);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException ex) {
          LOG.error("Could not parse selected outcome for removal: {}", selectedOutcomeStrWithParam,
              ex);

        }
      } else {
        LOG.warn("Attempted to remove null outcome.");

      }
    });
    removeButton.setId("removeOutcomeButton");
    removeButton.getStyleClass().add("remove-button");

    HBox buttonRow = createCenteredButtonBox(addButton, removeButton);

    pane.getChildren().addAll(header, outcomeComboBox, parameterRow, outcomesListView, buttonRow);
    VBox.setVgrow(outcomesListView, Priority.ALWAYS);
    LOG.debug("Outcomes section UI built.");
    return pane;
  }

  /**
   * Gets the ListView component used to display the outcomes (with parameters). Allows the parent
   * component to populate or clear the list.
   *
   * @return The ListView<String> instance.
   */
  public ListView<String> getOutcomesListView() {
    return outcomesListView;
  }

  /**
   * Gets the ComboBox component used to select parameters (Dynamic Variable names). Allows the
   * parent component to update the available items.
   *
   * @return The ComboBox<String> instance for parameters.
   */
  public ComboBox<String> getParameterComboBox() {
    return parameterComboBox;
  }

  /**
   * Updates the items available in the parameter ComboBox based on a list of DynamicVariables.
   * Clears existing items and adds the names of the provided variables.
   *
   * @param variables A List of {@link DynamicVariable} objects whose names should be displayed in
   *                  the parameter ComboBox. Can be null or empty.
   */
  public void updateParameterComboBox(List<DynamicVariable> variables) {
    parameterComboBox.getItems().clear();
    if (variables != null && !variables.isEmpty()) {
      List<String> varNames = variables.stream()
          .map(DynamicVariable::getName)
          .filter(Objects::nonNull)
          .distinct()
          .sorted()
          .collect(Collectors.toList());
      parameterComboBox.getItems().addAll(varNames);
      LOG.debug("Updated parameter combo box with {} variables.", varNames.size());
    } else {
      LOG.debug("Parameter combo box updated with empty list or null variables.");
    }
    parameterComboBox.setPromptText(PROMPT_SELECT_PARAMETER);
    parameterComboBox.getSelectionModel().clearSelection();
  }


  /**
   * Creates the HBox layout containing the parameter label, the parameter selection ComboBox, and
   * the 'Create Parameter' (+) button.
   *
   * @return A Node (specifically an HBox) representing the parameter selection row.
   */
  private Node createParameterSelectionRow() {
    HBox paramBox = new HBox(DEFAULT_SPACING / 2);
    paramBox.setAlignment(Pos.CENTER_LEFT);

    Label label = new Label(uiBundle.getString(KEY_PARAMETER_LABEL) + ":");
    parameterComboBox = new ComboBox<>();
    parameterComboBox.setId("parameterComboBox");
    parameterComboBox.setPromptText(PROMPT_SELECT_PARAMETER);
    parameterComboBox.setMaxWidth(Double.MAX_VALUE);

    Button createButton = createButton(KEY_CREATE_PARAM_BUTTON, e -> createParameterHandler.run());
    createButton.setId("addVariableButton");
    paramBox.getChildren().addAll(label, parameterComboBox, createButton);
    HBox.setHgrow(parameterComboBox, Priority.ALWAYS);
    return paramBox;
  }

  /**
   * Creates a styled header label using text from the resource bundle.
   *
   * @param bundleKey The key in the resource bundle corresponding to the label text.
   * @return A configured Label node.
   */
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a generic, styled ListView with a specified preferred height.
   *
   * @param <T>             The type of items the ListView will hold.
   * @param preferredHeight The preferred height for the ListView.
   * @return A configured ListView<T> node.
   */
  private <T> ListView<T> createListView(double preferredHeight) {
    ListView<T> listView = new ListView<>();
    listView.setPrefHeight(preferredHeight);
    listView.getStyleClass().add("data-list-view");
    return listView;
  }

  /**
   * Creates a styled button with text from the resource bundle and assigns an action handler.
   * Handles specific styling and text ('+') for the 'Create Parameter' button.
   *
   * @param bundleKey The key in the resource bundle for the button text (or identifier).
   * @param handler   The event handler to be executed when the button is clicked.
   * @return A configured Button node.
   */
  private Button createButton(String bundleKey,
      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    String buttonText =
        bundleKey.equals(KEY_CREATE_PARAM_BUTTON) ? "+" : uiBundle.getString(bundleKey);
    Button button = new Button(buttonText);
    button.setOnAction(handler);
    button.getStyleClass().add("action-button");

    if (bundleKey.equals(KEY_CREATE_PARAM_BUTTON)) {
      button.getStyleClass().add("small-button");
      button.setMaxWidth(Region.USE_PREF_SIZE);
    } else {
      button.setMaxWidth(Double.MAX_VALUE);
    }
    return button;
  }

  /**
   * Creates an HBox to hold buttons, centering them and applying default spacing. Allows the HBox
   * to grow horizontally to fill available space.
   *
   * @param buttons The Button nodes to add to the HBox.
   * @return A configured HBox node containing the buttons.
   */
  private HBox createCenteredButtonBox(Button... buttons) {
    HBox buttonBox = new HBox(DEFAULT_SPACING);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(buttons);
    HBox.setHgrow(buttonBox, Priority.ALWAYS);
    return buttonBox;
  }
}