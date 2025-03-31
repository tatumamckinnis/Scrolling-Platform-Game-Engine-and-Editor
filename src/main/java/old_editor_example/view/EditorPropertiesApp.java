package old_editor_example.view;

import old_editor_example.CollisionData;
import old_editor_example.EditorObject;
import old_editor_example.IdentityData;
import old_editor_example.SpriteData;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import old_editor_example.save.SaveController;

public class EditorPropertiesApp extends Application {

  private EditorObject editorObject;
  private SaveController saveController = new SaveController();

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    // Create a sample EditorObject with initial data.
    IdentityData identity = new IdentityData("My Object", "Enemy");
    CollisionData collisionData = new CollisionData(10, 20, 100, 50, "RECTANGLE");
    SpriteData spriteData = new SpriteData("assets/default.png", 50, 50, 64, 64);
    editorObject = new EditorObject(identity, collisionData, spriteData);

    // Create the properties UI inside a TabPane.
    TabPane tabPane = new TabPane();
    Tab propertiesTab = new Tab("Properties");
    propertiesTab.setClosable(false);
    propertiesTab.setContent(createPropertiesPane());
    tabPane.getTabs().add(propertiesTab);

    Scene scene = new Scene(tabPane, 400, 500);
    primaryStage.setScene(scene);
    primaryStage.setTitle("Editor Frontend - Single Object");
    primaryStage.show();
  }

  private VBox createPropertiesPane() {
    DynamicVariableEditorPane dynamicVarEditor = new DynamicVariableEditorPane(editorObject.getDynamicVariables());
    InputMappingEditorPane inputMappingEditorPane = new InputMappingEditorPane(editorObject.getInputData());

    GridPane grid = new GridPane();
    grid.setPadding(new Insets(10));
    grid.setHgap(10);
    grid.setVgap(10);

    int row = 0;

    Label nameLabel = new Label("Name:");
    TextField nameField = new TextField(editorObject.getIdentity().getName());
    grid.add(nameLabel, 0, row);
    grid.add(nameField, 1, row);
    nameField.setOnAction(e -> editorObject.getIdentity().setName(nameField.getText()));
    row++;

    Label groupLabel = new Label("Group:");
    TextField groupField = new TextField(editorObject.getIdentity().getGroup());
    grid.add(groupLabel, 0, row);
    grid.add(groupField, 1, row);
    groupField.setOnAction(e -> editorObject.getIdentity().setGroup(groupField.getText()));
    row++;

    // Sprite Data Section
    Label spriteHeader = new Label("Sprite Data:");
    grid.add(spriteHeader, 0, row, 2, 1);
    row++;

    Label spriteXLabel = new Label("Sprite X:");
    TextField spriteXField = new TextField(String.valueOf(editorObject.getSpriteData().getX()));
    grid.add(spriteXLabel, 0, row);
    grid.add(spriteXField, 1, row);
    spriteXField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(spriteXField.getText());
        editorObject.getSpriteData().setX(val);
      } catch (NumberFormatException ex) {
        spriteXField.setText(String.valueOf(editorObject.getSpriteData().getX()));
      }
    });
    row++;

    Label spriteYLabel = new Label("Sprite Y:");
    TextField spriteYField = new TextField(String.valueOf(editorObject.getSpriteData().getY()));
    grid.add(spriteYLabel, 0, row);
    grid.add(spriteYField, 1, row);
    spriteYField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(spriteYField.getText());
        editorObject.getSpriteData().setY(val);
      } catch (NumberFormatException ex) {
        spriteYField.setText(String.valueOf(editorObject.getSpriteData().getY()));
      }
    });
    row++;

    Label spritePathLabel = new Label("Sprite Path:");
    TextField spritePathField = new TextField(editorObject.getSpriteData().getSpritePath());
    grid.add(spritePathLabel, 0, row);
    grid.add(spritePathField, 1, row);
    spritePathField.setOnAction(e -> editorObject.getSpriteData().setSpritePath(spritePathField.getText()));
    row++;

    Label spriteWidthLabel = new Label("Sprite Width:");
    TextField spriteWidthField = new TextField(String.valueOf(editorObject.getSpriteData().getWidth()));
    grid.add(spriteWidthLabel, 0, row);
    grid.add(spriteWidthField, 1, row);
    spriteWidthField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(spriteWidthField.getText());
        editorObject.getSpriteData().setWidth(val);
      } catch (NumberFormatException ex) {
        spriteWidthField.setText(String.valueOf(editorObject.getSpriteData().getWidth()));
      }
    });
    row++;

    Label spriteHeightLabel = new Label("Sprite Height:");
    TextField spriteHeightField = new TextField(String.valueOf(editorObject.getSpriteData().getHeight()));
    grid.add(spriteHeightLabel, 0, row);
    grid.add(spriteHeightField, 1, row);
    spriteHeightField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(spriteHeightField.getText());
        editorObject.getSpriteData().setHeight(val);
      } catch (NumberFormatException ex) {
        spriteHeightField.setText(String.valueOf(editorObject.getSpriteData().getHeight()));
      }
    });
    row++;

    // Collision Data Section
    Label collisionHeader = new Label("Collision Data:");
    grid.add(collisionHeader, 0, row, 2, 1);
    row++;

    Label collisionXLabel = new Label("Collision X:");
    TextField collisionXField = new TextField(String.valueOf(editorObject.getCollisionData().getX()));
    grid.add(collisionXLabel, 0, row);
    grid.add(collisionXField, 1, row);
    collisionXField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(collisionXField.getText());
        editorObject.getCollisionData().setX(val);
      } catch (NumberFormatException ex) {
        collisionXField.setText(String.valueOf(editorObject.getCollisionData().getX()));
      }
    });
    row++;

    Label collisionYLabel = new Label("Collision Y:");
    TextField collisionYField = new TextField(String.valueOf(editorObject.getCollisionData().getY()));
    grid.add(collisionYLabel, 0, row);
    grid.add(collisionYField, 1, row);
    collisionYField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(collisionYField.getText());
        editorObject.getCollisionData().setY(val);
      } catch (NumberFormatException ex) {
        collisionYField.setText(String.valueOf(editorObject.getCollisionData().getY()));
      }
    });
    row++;

    Label collisionWidthLabel = new Label("Collision Width:");
    TextField collisionWidthField = new TextField(String.valueOf(editorObject.getCollisionData().getWidth()));
    grid.add(collisionWidthLabel, 0, row);
    grid.add(collisionWidthField, 1, row);
    collisionWidthField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(collisionWidthField.getText());
        editorObject.getCollisionData().setWidth(val);
      } catch (NumberFormatException ex) {
        collisionWidthField.setText(String.valueOf(editorObject.getCollisionData().getWidth()));
      }
    });
    row++;

    Label collisionHeightLabel = new Label("Collision Height:");
    TextField collisionHeightField = new TextField(String.valueOf(editorObject.getCollisionData().getHeight()));
    grid.add(collisionHeightLabel, 0, row);
    grid.add(collisionHeightField, 1, row);
    collisionHeightField.setOnAction(e -> {
      try {
        double val = Double.parseDouble(collisionHeightField.getText());
        editorObject.getCollisionData().setHeight(val);
      } catch (NumberFormatException ex) {
        collisionHeightField.setText(String.valueOf(editorObject.getCollisionData().getHeight()));
      }
    });
    row++;

    Label collisionShapeLabel = new Label("Collision Shape:");
    TextField collisionShapeField = new TextField(editorObject.getCollisionData().getShape());
    grid.add(collisionShapeLabel, 0, row);
    grid.add(collisionShapeField, 1, row);
    collisionShapeField.setOnAction(e -> editorObject.getCollisionData().setShape(collisionShapeField.getText()));
    row++;

    // Wrap the grid in a ScrollPane to allow scrolling.
    ScrollPane scrollPane = new ScrollPane();
    scrollPane.setContent(grid);
    scrollPane.setFitToWidth(true);

    // Add the Save button at the bottom in a VBox.
    Button saveButton = new Button("Save");
    saveButton.setOnAction(e -> {
      // Call the SaveController to "save" the object.
      saveController.save(editorObject);
    });

    VBox vbox = new VBox();
    vbox.getChildren().addAll(scrollPane, dynamicVarEditor, inputMappingEditorPane, saveButton);
    vbox.setSpacing(10);
    vbox.setPadding(new Insets(10));

    return vbox;
  }
}
