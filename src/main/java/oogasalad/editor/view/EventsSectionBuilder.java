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
 * Builds the UI section for managing Events in the Input Tab.
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
   * @param addEventHandler        The consumer to handle adding a new event. Accepts the event ID.
   * @param removeEventHandler     The runnable to handle removing the selected event.
   * @param selectionChangeHandler The consumer to handle changes in the selected event. Accepts the
   *                               new selection.
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
   * Builds the complete UI section for managing events.
   *
   * @return The root {@code Node} of the events section.
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
   * Retrieves the {@code ListView} used to display the list of events.
   *
   * @return The {@code ListView} for events.
   */
  public ListView<String> getEventListView() {
    return eventListView;
  }

  /**
   * Retrieves the {@code TextField} used for entering new event IDs.
   *
   * @return The {@code TextField} for event IDs.
   */
  public TextField getEventIdField() {
    return eventIdField;
  }

  /**
   * Creates the horizontal layout containing the label and text field for inputting event IDs.
   *
   * @return An {@code HBox} containing the event ID input elements.
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
   * Creates the horizontal layout containing the buttons for adding and removing events.
   *
   * @return An {@code HBox} containing the add and remove event buttons.
   */
  private HBox createEventButtonRow() {
    Button addButton = createButton(KEY_ADD_EVENT_BUTTON, e -> {
      String eventId = eventIdField.getText();
      if (eventId != null && !eventId.trim().isEmpty()) {
        addEventHandler.accept(eventId.trim());
      } else {
        LOG.warn("Attempted to add empty event ID.");
        // Consider showing error via factory's showErrorAlert if needed
      }
    });
    addButton.setId("addEventButton");
    Button removeButton = createButton(KEY_REMOVE_EVENT_BUTTON, e -> removeEventHandler.run());
    removeButton.setId("removeEventButton");
    removeButton.getStyleClass().add("remove-button");

    return createCenteredButtonBox(addButton, removeButton);
  }

  /**
   * Creates a styled header label using the text from the resource bundle.
   *
   * @param bundleKey The key to retrieve the header text from the resource bundle.
   * @return A styled {@code Label} for the section header.
   */
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a styled {@code ListView} with a specified preferred height.
   *
   * @param preferredHeight The preferred height of the list view.
   * @param <T>             The type of elements in the list view.
   * @return A styled {@code ListView}.
   */
  private <T> ListView<T> createListView(double preferredHeight) {
    ListView<T> listView = new ListView<>();
    listView.setPrefHeight(preferredHeight);
    listView.getStyleClass().add("data-list-view");
    return listView;
  }

  /**
   * Creates a styled {@code Button} with text from the resource bundle and an action handler.
   *
   * @param bundleKey The key to retrieve the button text from the resource bundle.
   * @param handler   The event handler to be executed when the button is clicked.
   * @return A styled {@code Button}.
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
   * Creates a centered horizontal layout containing the given buttons.
   *
   * @param buttons The buttons to be included in the layout.
   * @return A centered {@code HBox} containing the buttons.
   */
  private HBox createCenteredButtonBox(Button... buttons) {
    HBox buttonBox = new HBox(DEFAULT_SPACING);
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(buttons);
    HBox.setHgrow(buttonBox, Priority.ALWAYS);
    return buttonBox;
  }
}