package old_editor_example.view;

import old_editor_example.InputData;
import old_editor_example.KeyPressType;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Map;

public class InputMappingEditorPane extends VBox {
  private InputData inputData;
  private ListView<String> mappingListView;

  public InputMappingEditorPane(InputData inputData) {
    this.inputData = inputData;
    this.mappingListView = new ListView<>();
    Button addMappingButton = new Button("+ Add Input Mapping");
    addMappingButton.setOnAction(e -> openAddMappingDialog());

    setSpacing(10);
    setPadding(new Insets(10));
    getChildren().addAll(new Label("Input Mappings:"), mappingListView, addMappingButton);
    refreshMappingList();
  }

  private void openAddMappingDialog() {
    Dialog<Void> dialog = new Dialog<>();
    dialog.setTitle("Add Input Mapping");
    ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    // ComboBox for Key (using a subset of KeyCode names)
    ComboBox<String> keyComboBox = new ComboBox<>(FXCollections.observableArrayList(
        "A", "B", "C", "D", "SPACE", "LEFT", "RIGHT", "UP", "DOWN"
    ));
    keyComboBox.setPromptText("Select Key");

    // ComboBox for KeyPressType
    ComboBox<KeyPressType> keyPressTypeComboBox = new ComboBox<>(FXCollections.observableArrayList(KeyPressType.values()));
    keyPressTypeComboBox.setPromptText("Select Key Press Type");

    // TextField for Event Chain ID
    TextField eventChainIdField = new TextField();
    eventChainIdField.setPromptText("Event Chain ID");

    grid.add(new Label("Key:"), 0, 0);
    grid.add(keyComboBox, 1, 0);
    grid.add(new Label("Press Type:"), 0, 1);
    grid.add(keyPressTypeComboBox, 1, 1);
    grid.add(new Label("Event Chain ID:"), 0, 2);
    grid.add(eventChainIdField, 1, 2);

    dialog.getDialogPane().setContent(grid);

    // When the user presses "Add", capture the input and add the mapping.
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        String key = keyComboBox.getValue();
        KeyPressType pressType = keyPressTypeComboBox.getValue();
        String eventChainId = eventChainIdField.getText();
        if (key != null && pressType != null && eventChainId != null && !eventChainId.trim().isEmpty()) {
          inputData.addInputMapping(pressType, key, eventChainId.trim());
          refreshMappingList();
        }
      }
      return null;
    });

    dialog.showAndWait();
  }

  private void refreshMappingList() {
    mappingListView.getItems().clear();
    for (KeyPressType type : inputData.getInputMapping().keySet()) {
      for (Map.Entry<String, String> entry : inputData.getInputMapping().get(type).entrySet()) {
        mappingListView.getItems().add(type.name() + " - " + entry.getKey() + " => " + entry.getValue());
      }
    }
  }
}
