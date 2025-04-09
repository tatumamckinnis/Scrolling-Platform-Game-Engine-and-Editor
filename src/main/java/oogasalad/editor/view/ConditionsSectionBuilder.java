package oogasalad.editor.view;

import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import oogasalad.editor.model.data.event_enum.ConditionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Objects;

/**
 * Builds the UI section specifically for managing Conditions associated with an Event within the
 * Input Tab. This class encapsulates the creation and layout of controls like ComboBoxes for
 * selection, ListViews for display, and Buttons for adding/removing conditions. It relies on
 * handler functions passed during construction to delegate actions back to the parent component
 * (InputTabComponentFactory).
 */
public class ConditionsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(ConditionsSectionBuilder.class);

  private static final double LIST_VIEW_HEIGHT = 150.0;
  private static final String KEY_CONDITIONS_HEADER = "conditionsHeader";
  private static final String KEY_ADD_CONDITION_BUTTON = "addConditionButton";
  private static final String KEY_REMOVE_CONDITION_BUTTON = "removeConditionButton";
  private static final String PROMPT_SELECT_CONDITION = "Select Condition Type";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double DEFAULT_SPACING = 12.0;


  private final ResourceBundle uiBundle;
  private final Consumer<ConditionType> addConditionHandler;
  private final Consumer<ConditionType> removeConditionHandler;

  private ComboBox<ConditionType> conditionComboBox;
  private ListView<String> conditionsListView;


  /**
   * Constructs a builder for the conditions UI section.
   *
   * @param uiBundle               The resource bundle for localizing UI text (e.g., labels, button
   *                               text). Must not be null.
   * @param addConditionHandler    A consumer function to be called when the 'Add' button is
   *                               clicked, passing the selected ConditionType. Must not be null.
   * @param removeConditionHandler A consumer function to be called when the 'Remove' button is
   *                               clicked, passing the ConditionType corresponding to the selected
   *                               item in the list view. Must not be null.
   * @throws NullPointerException if any argument is null.
   */
  public ConditionsSectionBuilder(ResourceBundle uiBundle,
      Consumer<ConditionType> addConditionHandler,
      Consumer<ConditionType> removeConditionHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.addConditionHandler = Objects.requireNonNull(addConditionHandler);
    this.removeConditionHandler = Objects.requireNonNull(removeConditionHandler);
  }

  /**
   * Creates and lays out the UI components for the conditions section. This includes a header
   * label, a ComboBox to select condition types, Add/Remove buttons, and a ListView to display the
   * currently added conditions.
   *
   * @return A Node (specifically a VBox) containing all the UI elements for this section.
   */
  public Node build() {
    VBox pane = new VBox(DEFAULT_SPACING);
    pane.getStyleClass().add("input-sub-section");
    pane.setPadding(new Insets(DEFAULT_PADDING));

    Label header = createHeaderLabel(KEY_CONDITIONS_HEADER);
    conditionComboBox = new ComboBox<>(FXCollections.observableArrayList(ConditionType.values()));
    conditionComboBox.setId("conditionComboBox");
    conditionComboBox.setPromptText(PROMPT_SELECT_CONDITION);
    conditionComboBox.setMaxWidth(Double.MAX_VALUE);

    conditionsListView = createListView(LIST_VIEW_HEIGHT);
    conditionsListView.setId("conditionsListView");

    Button addButton = createButton(KEY_ADD_CONDITION_BUTTON, e -> {
      ConditionType selected = conditionComboBox.getSelectionModel().getSelectedItem();
      if (selected != null) {
        addConditionHandler.accept(selected);
      } else {
        LOG.warn("Attempted to add null condition type.");
      }
    });
    addButton.setId("addConditionButton");

    Button removeButton = createButton(KEY_REMOVE_CONDITION_BUTTON, e -> {
      String selectedStr = conditionsListView.getSelectionModel().getSelectedItem();
      if (selectedStr != null) {
        try {
          ConditionType condition = ConditionType.valueOf(selectedStr);
          removeConditionHandler.accept(condition);
        } catch (IllegalArgumentException ex) {
          LOG.error("Could not parse selected condition for removal: {}", selectedStr, ex);
        }
      } else {
        LOG.warn("Attempted to remove null condition.");
      }
    });
    removeButton.setId("removeConditionButton");
    removeButton.getStyleClass().add("remove-button");

    HBox buttonRow = createCenteredButtonBox(addButton, removeButton);

    pane.getChildren().addAll(header, conditionComboBox, conditionsListView, buttonRow);
    VBox.setVgrow(conditionsListView, Priority.ALWAYS);
    LOG.debug("Conditions section UI built.");
    return pane;
  }

  /**
   * Gets the ListView component used to display the conditions. This allows the parent component
   * (InputTabComponentFactory) to populate or clear the list.
   *
   * @return The ListView<String> instance displaying condition names.
   */
  public ListView<String> getConditionsListView() {
    return conditionsListView;
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
   *
   * @param bundleKey The key in the resource bundle for the button text.
   * @param handler   The event handler to be executed when the button is clicked.
   * @return A configured Button node.
   */
  private Button createButton(String bundleKey,
      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(bundleKey));
    button.setOnAction(handler);
    button.getStyleClass().add("action-button");
    button.setMaxWidth(Double.MAX_VALUE);
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