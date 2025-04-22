package oogasalad.editor.view.panes.spriteCreation;

import java.util.Collections;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.FrameData;

/**
 * Dialog to define a new {@link AnimationData} with a name, frame length, and a chosen sequence of
 * frames.
 *
 * @author Jacob You
 */
public class AnimationDialog extends Dialog<AnimationData> {

  public AnimationDialog(List<FrameData> availableFrames) {
    setTitle("New Animation");
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    TextField nameField = createNameField();
    TextField lengthField = createLengthField();

    ListView<FrameData> availList = createFrameList(availableFrames);
    ListView<FrameData> chosenList = createChosenList();

    HBox moveButtons = createMoveButtons(availList, chosenList);

    GridPane grid = createContentGrid(nameField, lengthField, availList, chosenList, moveButtons);
    getDialogPane().setContent(grid);

    configureOkButton(nameField, lengthField, chosenList);
    configureResultConverter(nameField, lengthField, chosenList);
  }

  private TextField createNameField() {
    TextField tf = new TextField();
    tf.setPromptText("Animation Name");
    return tf;
  }

  private TextField createLengthField() {
    TextField tf = new TextField();
    tf.setPromptText("Frame Length");
    tf.setTextFormatter(new TextFormatter<>(c ->
        c.getControlNewText().matches("\\d*\\.?\\d*") ? c : null));
    return tf;
  }

  private ListView<FrameData> createFrameList(List<FrameData> frames) {
    ListView<FrameData> list = new ListView<>(FXCollections.observableArrayList(frames));
    list.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(FrameData item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "" : item.name());
      }
    });
    list.setPrefHeight(200);
    return list;
  }

  private ListView<FrameData> createChosenList() {
    ListView<FrameData> list = new ListView<>();
    list.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(FrameData item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? "" : item.name());
      }
    });
    list.setPrefHeight(200);
    return list;
  }

  private HBox createMoveButtons(ListView<FrameData> avail, ListView<FrameData> chosen) {
    Button btnAdd    = new Button("▶");
    Button btnRemove = new Button("◀");
    Button btnUp     = new Button("▲");
    Button btnDown   = new Button("▼");

    btnAdd.setOnAction(e -> {
      FrameData f = avail.getSelectionModel().getSelectedItem();
      if (f != null) chosen.getItems().add(f);
    });
    btnRemove.setOnAction(e -> {
      int i = chosen.getSelectionModel().getSelectedIndex();
      if (i >= 0) chosen.getItems().remove(i);
    });
    btnUp.setOnAction(e -> moveSelected(chosen, -1));
    btnDown.setOnAction(e -> moveSelected(chosen, +1));

    HBox box = new HBox(5, btnAdd, btnRemove, btnUp, btnDown);
    box.setPadding(new Insets(5));
    return box;
  }

  private void moveSelected(ListView<FrameData> list, int offset) {
    int i = list.getSelectionModel().getSelectedIndex();
    int target = i + offset;
    if (i >= 0 && target >= 0 && target < list.getItems().size()) {
      Collections.swap(list.getItems(), i, target);
      list.getSelectionModel().select(target);
    }
  }

  private GridPane createContentGrid(
      TextField nameField,
      TextField lengthField,
      ListView<FrameData> availList,
      ListView<FrameData> chosenList,
      HBox moveButtons) {

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(8);
    grid.add(new Label("Name:"),   0, 0);
    grid.add(nameField,            1, 0);
    grid.add(new Label("Length:"), 0, 1);
    grid.add(lengthField,          1, 1);
    grid.add(new Label("Frames:"), 0, 2);
    grid.add(new HBox(5, availList, moveButtons, chosenList), 1, 2);
    return grid;
  }

  private void configureOkButton(
      TextField nameField,
      TextField lengthField,
      ListView<FrameData> chosenList) {

    Button okBtn = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okBtn.disableProperty().bind(
        nameField.textProperty().isEmpty()
            .or(lengthField.textProperty().isEmpty())
            .or(Bindings.createBooleanBinding(() -> {
              try {
                return Double.parseDouble(lengthField.getText()) <= 0;
              } catch (Exception ex) {
                return true;
              }
            }, lengthField.textProperty()))
            .or(Bindings.isEmpty(chosenList.getItems()))
    );
  }

  private void configureResultConverter(
      TextField nameField,
      TextField lengthField,
      ListView<FrameData> chosenList) {

    setResultConverter(button -> {
      if (button == ButtonType.OK) {
        double len = Double.parseDouble(lengthField.getText());
        List<String> seq = chosenList.getItems().stream()
            .map(FrameData::name)
            .toList();
        return new AnimationData(nameField.getText(), len, seq);
      }
      return null;
    });
  }
}
