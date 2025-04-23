package oogasalad.editor.view;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Builds the UI section for managing Events in the Input Tab.
 * Handles displaying, adding, and removing events by their string identifiers.
 * Configuration values like identifiers and layout constants are externalized.
 *
 * @author Tatum McKinnis
 */
public class EventsSectionBuilder {

  private static final Logger LOG = LogManager.getLogger(EventsSectionBuilder.class);
  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/events_section_builder_identifiers.properties";

  private final ResourceBundle uiBundle;
  private final Properties identifierProps;
  private final Consumer<String> addEventHandler;
  private final Runnable removeEventHandler;
  private final Consumer<String> selectionChangeHandler;

  private TextField eventIdField;
  private ListView<String> eventListView;

  /**
   * Constructs an {@code EventsSectionBuilder} with the necessary dependencies.
   * Loads identifiers from an external properties file.
   *
   * @param uiBundle               The resource bundle containing UI labels and messages.
   * @param addEventHandler        The consumer to handle adding a new event. Accepts the event ID (String).
   * @param removeEventHandler     The runnable to handle removing the selected event (relies on ListView selection).
   * @param selectionChangeHandler The consumer to handle changes in the selected event. Accepts the selected event ID (String).
   * @throws NullPointerException if any of the required handler/bundle arguments are {@code null}.
   * @throws RuntimeException     if the identifiers properties file cannot be loaded.
   */
  public EventsSectionBuilder(ResourceBundle uiBundle,
      Consumer<String> addEventHandler,
      Runnable removeEventHandler,
      Consumer<String> selectionChangeHandler) {
    this.uiBundle = Objects.requireNonNull(uiBundle);
    this.addEventHandler = Objects.requireNonNull(addEventHandler);
    this.removeEventHandler = Objects.requireNonNull(removeEventHandler);
    this.selectionChangeHandler = Objects.requireNonNull(selectionChangeHandler);
    this.identifierProps = loadIdentifierProperties();
  }

  /**
   * Loads the identifier strings (keys, CSS classes, IDs) from the properties file.
   * @return A Properties object containing the loaded identifiers.
   * @throws RuntimeException If the properties file cannot be found or read.
   */
  private Properties loadIdentifierProperties() {
    Properties props = new Properties();
    try (InputStream input = EventsSectionBuilder.class.getResourceAsStream(IDENTIFIERS_PROPERTIES_PATH)) {
      if (input == null) {
        LOG.error("CRITICAL: Unable to find identifiers properties file: {}", IDENTIFIERS_PROPERTIES_PATH);
        throw new RuntimeException("Missing required identifiers properties file: " + IDENTIFIERS_PROPERTIES_PATH);
      }
      props.load(input);
    } catch (IOException ex) {
      LOG.error("CRITICAL: Error loading identifiers properties file: {}", IDENTIFIERS_PROPERTIES_PATH, ex);
      throw new RuntimeException("Error loading identifiers properties file", ex);
    }
    return props;
  }

  /**
   * Retrieves an identifier value from the loaded identifier properties.
   * @param key The key for the identifier.
   * @return The identifier string.
   * @throws RuntimeException If the key is not found.
   */
  private String getId(String key) {
    String value = identifierProps.getProperty(key);
    if (value == null || value.trim().isEmpty()) {
      LOG.error("Missing identifier in properties file for key: {}", key);
      throw new RuntimeException("Missing identifier in properties file for key: " + key);
    }
    return value;
  }

  /**
   * Builds the complete UI section for managing events. Includes a header, an input row for new
   * event IDs, a list view to display existing events, and buttons to add/remove events.
   * Layout properties like spacing and padding are controlled via CSS.
   *
   * @return The root {@code Node} of the events section UI.
   */
  public Node build() {
    VBox section = new VBox();
    section.setId(getId("id.sectionVbox"));
    section.getStyleClass().add(getId("style.inputSection"));

    Label header = createHeaderLabel(getId("key.eventsHeader"));
    HBox inputRow = createEventInputRow();
    eventListView = createListView();
    eventListView.setId(getId("id.eventListView"));
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
   * Retrieves the {@code ListView} used to display the list of event IDs (Strings).
   * Allows external components to populate or interact with the list.
   *
   * @return The {@code ListView<String>} for displaying event IDs.
   */
  public ListView<String> getEventListView() {
    return eventListView;
  }

  /**
   * Retrieves the {@code TextField} used for entering new event IDs.
   * Allows external components to potentially interact with the input field.
   *
   * @return The {@code TextField} for event ID input.
   */
  public TextField getEventIdField() {
    return eventIdField;
  }

  /**
   * Creates the horizontal layout (HBox) containing the label and text field for inputting new
   * event IDs. Spacing is controlled via CSS.
   *
   * @return An {@code HBox} containing the event ID label and text field.
   */
  private HBox createEventInputRow() {
    HBox inputBox = new HBox();
    inputBox.setId(getId("id.inputRowHbox"));
    inputBox.setAlignment(Pos.CENTER_LEFT);
    Label label = new Label(uiBundle.getString(getId("key.eventIdLabel")) + ":");
    eventIdField = new TextField();
    eventIdField.setId(getId("id.eventIdField"));
    eventIdField.setPromptText(uiBundle.getString(getId("key.eventIdLabel")));
    HBox.setHgrow(eventIdField, Priority.ALWAYS);
    inputBox.getChildren().addAll(label, eventIdField);
    return inputBox;
  }

  /**
   * Creates the horizontal layout (HBox) containing the "Add Event" and "Remove Event" buttons.
   * Spacing and alignment are controlled via CSS.
   *
   * @return An {@code HBox} containing the add and remove event buttons.
   */
  private HBox createEventButtonRow() {
    Button addButton = createButton(getId("key.addEventButton"), e -> {
      String eventId = eventIdField.getText();
      if (eventId != null && !eventId.trim().isEmpty()) {
        addEventHandler.accept(eventId.trim());
        eventIdField.clear();
      } else {
        LOG.warn(uiBundle.getString(getId("key.warnEmptyEventId")));
      }
    });
    addButton.setId(getId("id.addEventButton"));

    Button removeButton = createButton(getId("key.removeEventButton"), e -> removeEventHandler.run());
    removeButton.setId(getId("id.removeEventButton"));
    removeButton.getStyleClass().add(getId("style.removeButton"));

    return createCenteredButtonBox(addButton, removeButton);
  }

  /**
   * Creates a styled header label using text retrieved from the resource bundle based on the
   * provided identifier key.
   *
   * @param identifierKey The key corresponding to the header text identifier in the properties file.
   * @return A styled {@code Label} configured as a section header.
   */
  private Label createHeaderLabel(String identifierKey) {
    Label label = new Label(uiBundle.getString(identifierKey));
    label.getStyleClass().add(getId("style.sectionHeader"));
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  /**
   * Creates a generic, styled {@code ListView}. Preferred height is controlled via CSS.
   *
   * @param <T> The type of items the ListView will hold.
   * @return A configured and styled {@code ListView<T>}.
   */
  private <T> ListView<T> createListView() {
    ListView<T> listView = new ListView<>();
    listView.getStyleClass().add(getId("style.dataListView"));
    return listView;
  }

  /**
   * Creates a styled {@code Button} with text from the resource bundle (using the identifier key)
   * and assigns the provided action handler.
   *
   * @param identifierKey The key in the identifier properties file for the button's resource key.
   * @param handler       The event handler to be executed when the button is clicked.
   * @return A configured and styled {@code Button}.
   */
  private Button createButton(String identifierKey,
      javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
    Button button = new Button(uiBundle.getString(identifierKey));
    button.setOnAction(handler);
    button.getStyleClass().add(getId("style.actionButton"));
    button.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(button, Priority.ALWAYS);
    return button;
  }

  /**
   * Creates a centered horizontal layout (HBox) to hold one or more buttons.
   * Spacing is controlled via CSS. Configures buttons to grow equally.
   *
   * @param buttons The {@code Button} nodes to add to the HBox.
   * @return A configured {@code HBox} containing the centered buttons.
   */
  private HBox createCenteredButtonBox(Button... buttons) {
    HBox buttonBox = new HBox();
    buttonBox.setId(getId("id.buttonRowHbox"));
    buttonBox.setAlignment(Pos.CENTER);
    buttonBox.getChildren().addAll(buttons);
    return buttonBox;
  }
}