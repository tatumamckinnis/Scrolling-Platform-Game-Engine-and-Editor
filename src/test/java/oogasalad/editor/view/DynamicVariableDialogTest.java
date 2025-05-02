package oogasalad.editor.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.view.dialogs.DynamicVariableDialog;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.testfx.matcher.base.NodeMatchers.*;
import org.testfx.matcher.control.TextInputControlMatchers;

import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;

@ExtendWith({ApplicationExtension.class})
class DynamicVariableDialogTest {

  private ResourceBundle mockUiBundle;

  private DynamicVariableDialog dialog;
  private CompletableFuture<Optional<DynamicVariable>> dialogResultFuture;


  private static final String KEY_DIALOG_ADD_VAR_TITLE = "dialogAddVarTitle";
  private static final String KEY_DIALOG_ADD_BUTTON = "dialogAddButton";
  private static final String KEY_DIALOG_VAR_NAME = "dialogVarName";
  private static final String KEY_DIALOG_VAR_TYPE = "dialogVarType";
  private static final String KEY_DIALOG_VAR_VALUE = "dialogVarValue";
  private static final String KEY_DIALOG_VAR_DESC = "dialogVarDesc";
  private static final String KEY_ERROR_INVALID_INPUT_TITLE = "errorInvalidInputTitle";


  private static final String ADD_BUTTON_TEXT = "Add";

  @Start
  private void start(Stage stage) {

    mockUiBundle = Mockito.mock(ResourceBundle.class);
    when(mockUiBundle.getString(KEY_DIALOG_ADD_VAR_TITLE)).thenReturn("Test Add Variable");
    when(mockUiBundle.getString(KEY_DIALOG_ADD_BUTTON)).thenReturn(ADD_BUTTON_TEXT);
    when(mockUiBundle.getString(KEY_DIALOG_VAR_NAME)).thenReturn("Variable Name");
    when(mockUiBundle.getString(KEY_DIALOG_VAR_TYPE)).thenReturn("Type");
    when(mockUiBundle.getString(KEY_DIALOG_VAR_VALUE)).thenReturn("Initial Value");
    when(mockUiBundle.getString(KEY_DIALOG_VAR_DESC)).thenReturn("Description");
    when(mockUiBundle.getString(KEY_ERROR_INVALID_INPUT_TITLE)).thenReturn("Invalid Input");




    dialog = new DynamicVariableDialog(mockUiBundle);


    stage.setScene(new Scene(new StackPane(), 100, 100));
    stage.show();


    dialogResultFuture = new CompletableFuture<>();
    Platform.runLater(() -> {
      dialog.showAndWait().ifPresentOrElse(
          result -> dialogResultFuture.complete(Optional.of(result)),
          () -> dialogResultFuture.complete(Optional.empty())
      );
    });
  }

  @Test
  void testDialogElementsExistAndAddButtonInitiallyDisabled(FxRobot robot) {

    verifyThat("#varNameField", TextInputControlMatchers.hasText(""));
    verifyThat("#varTypeCombo", (ComboBox<String> c) -> c.getValue() != null);
    verifyThat("#varValueField", TextInputControlMatchers.hasText(""));
    verifyThat("#varDescField", TextInputControlMatchers.hasText(""));

    verifyThat(ADD_BUTTON_TEXT, isDisabled());
  }

  @Test
  void testAddButtonEnablesAfterFillingRequiredFields(FxRobot robot) {

    verifyThat(ADD_BUTTON_TEXT, isDisabled());

    robot.clickOn("#varNameField").write("myVar");
    robot.clickOn("#varTypeCombo").clickOn("string");
    robot.clickOn("#varValueField").write("initial");

    verifyThat(ADD_BUTTON_TEXT, isEnabled());
  }

  @Test
  void testCreateVariableSuccessfully(FxRobot robot) throws Exception {

    robot.clickOn("#varNameField").write("score");
    robot.clickOn("#varTypeCombo").clickOn("int");
    robot.clickOn("#varValueField").write("100");
    robot.clickOn("#varDescField").write("Player score");

    robot.clickOn(ADD_BUTTON_TEXT);


    Optional<DynamicVariable> result = waitForResult(dialogResultFuture);


    assertTrue(result.isPresent(), "Dialog should return a variable");
    DynamicVariable variable = result.get();
    assertEquals("score", variable.getName());
    assertEquals("int", variable.getType());
    assertEquals("100", variable.getValue());
    assertEquals("Player score", variable.getDescription());
  }

  @Test
  void testCancelButtonReturnsEmptyOptional(FxRobot robot) throws Exception {

    robot.clickOn("Cancel");

    Optional<DynamicVariable> result = waitForResult(dialogResultFuture);

    assertTrue(result.isEmpty(), "Dialog should return empty optional on Cancel");
  }

  @Test
  void testAddButtonDisabledIfNameCleared(FxRobot robot) {

    robot.clickOn("#varNameField").write("myVar");
    robot.clickOn("#varTypeCombo").clickOn("string");
    robot.clickOn("#varValueField").write("initial");
    verifyThat(ADD_BUTTON_TEXT, isEnabled());


    robot.clickOn("#varNameField").eraseText(5);

    verifyThat(ADD_BUTTON_TEXT, isDisabled());
  }


  private <T> T waitForResult(CompletableFuture<T> future) throws Exception {
    try {

      return future.get(5, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      fail("Dialog did not return a result within the timeout period.");
      return null;
    }
  }
}