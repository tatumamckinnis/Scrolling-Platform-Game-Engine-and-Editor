package oogasalad.editor.view.sprites;

import static org.junit.jupiter.api.Assertions.*;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.panes.spriteCreation.SpriteAssetPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * TestFX tests for SpriteAssetPane (checks UI elements and action wiring).
 * Author: Jacob
 */
@ExtendWith(ApplicationExtension.class)
class SpriteAssetPaneTest {

  @Mock
  private EditorController controllerMock;

  private SpriteAssetPane pane;
  private Stage primary;

  @BeforeAll
  static void enableHeadless() {
    System.setProperty("testfx.headless", "true");
    System.setProperty("glass.platform", "Monocle");
    System.setProperty("monocle.platform", "Headless");
  }

  @Start
  void start(Stage stage) {
    primary = stage;
    primary.setScene(new Scene(new javafx.scene.layout.Pane(), 400, 300));
    primary.show();
  }

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    pane = new SpriteAssetPane(controllerMock, primary);
    Platform.runLater(() -> ((javafx.scene.layout.Pane) primary.getScene().getRoot()).getChildren().add(pane));
    WaitForAsyncUtils.waitForFxEvents();
  }

  /**
   * Tests buttons exist with correct text and style classes.
   */
  @Test
  void uiElements_ArePresentAndStyled(FxRobot robot) {
    Button importBtn = robot.lookup("Import Sheet").queryButton();
    Button newBtn = robot.lookup("New Sprite").queryButton();
    assertTrue(importBtn.getStyleClass().contains("small-button"));
    assertTrue(newBtn.getStyleClass().contains("small-button"));
  }

  /**
   * Tests “New Sprite” button opens a modal Stage.
   */
  @Test
  void newSpriteButton_Click_OpensModalWindow(FxRobot robot) {
    int before = Window.getWindows().size();
    robot.clickOn("New Sprite");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(before + 1, Window.getWindows().size());
  }

  /**
   * Tests “Import Sheet” button opens the processor dialog.
   */
  @Test
  void importSheetButton_Click_OpensProcessorWindow(FxRobot robot) {
    int before = Window.getWindows().size();
    robot.clickOn("Import Sheet");
    WaitForAsyncUtils.waitForFxEvents();
    assertEquals(before + 1, Window.getWindows().size());
  }
}
