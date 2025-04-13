package oogasalad.editor.view;

import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Objects;

/**
 * Builds the UI section for managing Events in the Input Tab. Handles displaying, adding, and
 * removing events by their string identifiers.
 *
 * @author Tatum McKinnis
 */
public class EventsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(EventsSectionBuilder.class);

  private static final double LIST_VIEW_HEIGHT = 150.0;
  private static final String KEY_EVENTS_HEADER = "eventsHeader";
  private static final String KEY_EVENT_ID_LABEL = "eventIdLabel";
  private static final String KEY_ADD_EVENT_BUTTON = "addEventButton";
  private static final String KEY_REMOVE_EVENT_BUTTON = "removeEventButton";

  private static final double DEFAULT_PADDING = 12.0;
  private static final double DEFAULT_SPACING = 12.0;

  private final ResourceBundle uiBundle;
  private final Consumer<String> addEventHandler;
  private final Runnable removeEventHandler;
  private final Consumer<String> selectionChangeHandler;

  private TextField eventIdField;
  private ListView<String> eventListView;

  /**
   * Constructs an {@code EventsSectionBuilder} with the necessary dependencies.
   *
   * @param uiBundle               The resource bundle containing UI labels and messages.
   * @param addEventHandler        The consumer to handle adding a new event. Accepts the event ID
   *                               (String).
   * @param removeEventHandler     The runnable to handle removing the selected event (relies on
   *                               ListView selection).
   * @param selectionChangeHandler The consumer to handle changes in the selected event. Accepts the
   *                               selected event ID (String).
   * @throws NullPointerException if any of the provided arguments are {@code null}.
   */
  public EventsSectionBuilder(ResourceBundle uiBundle,
      Consumer<String> addEventHandler,
      Runnable removeEventHandler,
      Consumer<String> selectionChangeHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.addEventHandler = Objects.requireNonNull(addEventHandler);
    this.removeEventHandler = Objects.requireNonNull(removeEventHandler);
    this.selectionChangeHandler = Objects.requireNonNull(selectionChangeHandler);
  }

  /**
   * Builds the complete UI section for managing events. Includes a header, an input row for new
   * event IDs, a list view to display existing events, and buttons to add/remove events.
   *
   * @return The root {@code Node} of the events section UI.
   */
  public Node build() {
    VBox section = new VBox(DEFAULT_SPACING);
    section.setPadding(new Insets(DEFAULT_PADDING));
    section.getStyleClass().add("input-section");

    Label header = createHeaderLabel(KEY_EVENTS_HEADER);
    HBox inputRow = createEventInputRow();
    eventListView = createListView(LIST_VIEW_HEIGHT);
    eventListView.setId("eventListView");
    HBox buttonRow = createEventButtonRow();

    eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      selectionChangeHandler.accept(newVal);
    });

    VBox.setVgrow(eventListView, Priority.SOMETIMES);

    section.getChildren().addAll(header, inputRow, eventListView, buttonRow);
    LOG.debug("Events section UI built.");
    return section;
  }

  /**
   * Retrieves the {@code ListView} used to display the list of event IDs (Strings). This allows
   * external components to populate or interact with the list.
   *
   * @return The {@code ListView<String>} for displaying event IDs.
   */
  public ListView<String> getEventListView() {
    return eventListView;
  }

  /**
   * Retrieves the {@code TextField} used for entering new event IDs. This allows external
   * components to potentially interact with the input field.
   *
   * @return The {@code TextField} for event ID input.
   */
  public TextField getEventIdField() {
    return eventIdField;
  }

  /**
   * Creates the horizontal layout (HBox) containing the label and text field for inputting new
   * event IDs.
   *
   * @return An {@code HBox} containing the event ID label and text field.
   */
  private HBox createEventInputRow() {
    HBox inputBox = new HBox(DEFAULT_SPACING / 2);
    inputBox.setAlignment(Pos.CENTER_LEFT);
    Label label = new Label(uiBundle.getString(KEY_EVENT_ID_LABEL) + ":");
    eventIdField = new TextField();
    eventIdField.setId("eventIdField");
    eventIdField.setPromptText(uiBundle.getString(KEY_EVENT_ID_LABEL));
    HBox.setHgrow(eventIdField, Priority.ALWAYS);
    inputBox.getChildren().addAll(label, eventIdField);
    return inputBox;
  }

  /**
   * Creates the horizontal layout (HBox) containing the "Add Event" and "Remove Event" buttons.
   *
   * @return An {@code HBox} containing the add and remove event buttons, typically centered.
   */
  private HBox createEventButtonRow() {
    Button addButton = createButton(KEY_ADD_EVENT_BUTTON, e -> {
      String eventId = eventIdField.getText();
      if (eventId != null && !eventId.trim().isEmpty()) {
        addEventHandler.accept(eventId.trim());
        eventIdField.clear();
      } else {
        LOG.warn("Attempted to add empty event ID.");
      }
    });
    addButton.setId("addEventButton");

    Button removeButton = createButton(KEY_REMOVE_EVENT_BUTTON, e -> removeEventHandler.run());
    removeButton.setId("removeEventButton");
    removeButton.getStyleClass().add("remove-button");

    return createCenteredButtonBox(addButton, removeButton);
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
   * Creates a generic, styled {@code ListView} with a specified preferred height.
   *
   * @param <T>             The type of items the ListView will hold.
   * @param preferredHeight The preferred height for the ListView, controlling its initial size.
   * @return A configured and styled {@code ListView<T>}.
   */
  private <T> ListView<T> createListView(double preferredHeight) {
    ListView<T> listView = new ListView<>();
    listView.setPrefHeight(preferredHeight);
    listView.getStyleClass().add("data-list-view");
    return listView;
  }

  /**
   * Creates a styled {@code Button} with text from the resource bundle and assigns the provided
   * action handler.
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
    button.setMaxWidth(Double.MAX_VALUE);
    return button;
  }

  /**
   * Creates a centered horizontal layout (HBox) to hold one or more buttons, applying default
   * spacing. The HBox is configured to grow horizontally if space is available.
   *
   * @param buttons The {@code Button} nodes to add to the HBox.
   * @return A configured {@code HBox} containing the centered buttons.
   */
  private HBox createCenteredButtonBox(Button... buttons) {
    HBox buttonBox = new HBox(DEFAULT_SPACING);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(buttons);
    HBox.setHgrow(buttonBox, Priority.ALWAYS);
    return buttonBox;
  }
}