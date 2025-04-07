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

  // Constants needed by this builder
  private static final double LIST_VIEW_HEIGHT = 150.0;
  // private static final double EVENTS_SECTION_WIDTH = 180.0; // REMOVED Fixed Width
  private static final String KEY_EVENTS_HEADER = "eventsHeader";
  private static final String KEY_EVENT_ID_LABEL = "eventIdLabel";
  private static final String KEY_ADD_EVENT_BUTTON = "addEventButton";
  private static final String KEY_REMOVE_EVENT_BUTTON = "removeEventButton";

  // Using constants potentially defined in Factory or shared constants class
  private static final double DEFAULT_PADDING = 12.0; // Adjusted
  private static final double DEFAULT_SPACING = 12.0; // Adjusted

  private final ResourceBundle uiBundle;
  private final Consumer<String> addEventHandler;
  private final Runnable removeEventHandler;
  private final Consumer<String> selectionChangeHandler;

  private TextField eventIdField;
  private ListView<String> eventListView;

  public EventsSectionBuilder(ResourceBundle uiBundle,
      Consumer<String> addEventHandler,
      Runnable removeEventHandler,
      Consumer<String> selectionChangeHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.addEventHandler = Objects.requireNonNull(addEventHandler);
    this.removeEventHandler = Objects.requireNonNull(removeEventHandler);
    this.selectionChangeHandler = Objects.requireNonNull(selectionChangeHandler);
  }

  public Node build() {
    VBox section = new VBox(DEFAULT_SPACING); // Use updated spacing
    section.setPadding(new Insets(DEFAULT_PADDING)); // Use updated padding
    // section.setPrefWidth(EVENTS_SECTION_WIDTH); // REMOVED THIS LINE to allow horizontal growth
    section.getStyleClass().add("input-section"); // Add relevant style class

    Label header = createHeaderLabel(KEY_EVENTS_HEADER);
    HBox inputRow = createEventInputRow();
    eventListView = createListView(LIST_VIEW_HEIGHT);
    HBox buttonRow = createEventButtonRow();

    eventListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      selectionChangeHandler.accept(newVal);
    });

    // Allow list view to grow vertically if needed, although height is preferred
    VBox.setVgrow(eventListView, Priority.SOMETIMES);

    section.getChildren().addAll(header, inputRow, eventListView, buttonRow);
    LOG.debug("Events section UI built.");
    return section;
  }

  public ListView<String> getEventListView() {
    return eventListView;
  }

  public TextField getEventIdField() {
    return eventIdField;
  }

  private HBox createEventInputRow() {
    HBox inputBox = new HBox(DEFAULT_SPACING / 2); // Half spacing for tight input row
    inputBox.setAlignment(Pos.CENTER_LEFT);
    Label label = new Label(uiBundle.getString(KEY_EVENT_ID_LABEL) + ":"); // Add colon for clarity
    eventIdField = new TextField();
    eventIdField.setPromptText(uiBundle.getString(KEY_EVENT_ID_LABEL));
    HBox.setHgrow(eventIdField, Priority.ALWAYS); // Allow field to grow
    inputBox.getChildren().addAll(label, eventIdField);
    return inputBox;
  }

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
    Button removeButton = createButton(KEY_REMOVE_EVENT_BUTTON, e -> removeEventHandler.run());
    removeButton.getStyleClass().add("remove-button"); // Add specific style class for potential red color

    return createCenteredButtonBox(addButton, removeButton);
  }

  // --- Helper methods ---
  private Label createHeaderLabel(String bundleKey) {
    Label label = new Label(uiBundle.getString(bundleKey));
    label.getStyleClass().add("section-header");
    // Ensure header spans width if needed
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  private <T> ListView<T> createListView(double preferredHeight) {
    ListView<T> listView = new ListView<>();
    listView.setPrefHeight(preferredHeight);
    // Consider setting a min width if sections become too narrow on resize
    // listView.setMinWidth(150);
    listView.getStyleClass().add("data-list-view");
    return listView;
  }

  private Button createButton(String bundleKey, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(bundleKey));
    button.setOnAction(handler);
    // Apply general style, specific styles added in calling method if needed
    button.getStyleClass().add("action-button");
    button.setMaxWidth(Double.MAX_VALUE); // Allow buttons to grow slightly if needed
    return button;
  }

  private HBox createCenteredButtonBox(Button... buttons) {
    HBox buttonBox = new HBox(DEFAULT_SPACING); // Use updated spacing
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(buttons);
    // Allow button box to take full width, buttons are centered within
    HBox.setHgrow(buttonBox, Priority.ALWAYS);
    return buttonBox;
  }
  // --- End Helper methods ---
}