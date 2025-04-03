package old_editor_example.view;

import oogasalad.editor.model.data.object_data.DynamicVariableContainer;
import oogasalad.editor.model.data.object_data.DynamicVariable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DynamicVariableEditorPane extends VBox {
  private DynamicVariableContainer container;
  private ListView<String> variableListView;

  public DynamicVariableEditorPane(DynamicVariableContainer container) {
    this.container = container;
    this.variableListView = new ListView<>();
    Button addButton = new Button("+ Add Dynamic Variable");
    addButton.setOnAction(e -> openAddVariableDialog());
    setSpacing(10);
    setPadding(new Insets(10));
    getChildren().addAll(new Label("Dynamic Variables:"), variableListView, addButton);
    refreshVariableList();
  }

  private void openAddVariableDialog() {
    Dialog<DynamicVariable> dialog = new Dialog<>();
    dialog.setTitle("Add Dynamic Variable");

    // Set the button types.
    ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

    // Create a grid for the input fields.
    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);
    grid.setPadding(new Insets(20, 150, 10, 10));

    TextField nameField = new TextField();
    nameField.setPromptText("Name");
    TextField typeField = new TextField();
    typeField.setPromptText("Type (int, double, boolean, string)");
    TextField valueField = new TextField();
    valueField.setPromptText("Value");
    TextField descriptionField = new TextField();
    descriptionField.setPromptText("Description");

    grid.add(new Label("Name:"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Type:"), 0, 1);
    grid.add(typeField, 1, 1);
    grid.add(new Label("Value:"), 0, 2);
    grid.add(valueField, 1, 2);
    grid.add(new Label("Description:"), 0, 3);
    grid.add(descriptionField, 1, 3);

    dialog.getDialogPane().setContent(grid);

    // Convert the result when the Add button is pressed.
    dialog.setResultConverter(dialogButton -> {
      if (dialogButton == addButtonType) {
        try {
          return new DynamicVariable(
              nameField.getText(),
              typeField.getText(),
              valueField.getText(),
              descriptionField.getText()
          );
        } catch (IllegalArgumentException ex) {
          // Show an error alert if input is invalid.
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
      container.addVariable(dynamicVar);
      refreshVariableList();
    });
  }

  private void refreshVariableList() {
    variableListView.getItems().clear();
    for (DynamicVariable var : container.getAllVariables()) {
      variableListView.getItems().add(var.toString());
    }
  }
}
