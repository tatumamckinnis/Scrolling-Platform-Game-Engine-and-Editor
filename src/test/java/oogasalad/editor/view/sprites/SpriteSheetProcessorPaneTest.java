package oogasalad.editor.view.sprites;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.panes.spriteCreation.SpriteRegion;
import oogasalad.editor.view.panes.spriteCreation.SpriteSheetProcessorPane;
import oogasalad.editor.view.panes.spriteCreation.SpriteSheetProcessorPane.SpriteSheetMode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Unit tests for SpriteSheetProcessorPane.
 * Author: Jacob
 */
@ExtendWith(ApplicationExtension.class)
public class SpriteSheetProcessorPaneTest {

  @Mock
  private EditorController controller;

  private SpriteSheetProcessorPane pane;
  private AutoCloseable mocks;

  @Start
  public void start(Stage stage) {
    mocks = MockitoAnnotations.openMocks(this);
    pane = new SpriteSheetProcessorPane(controller, stage);
    stage.setScene(new Scene(pane, 900, 600));
    stage.show();
  }

  @AfterEach
  public void tearDown() throws Exception {
    mocks.close();
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  public void loadSheetSaveAndAddBoxButtons_WhenPaneInitialized_ShouldBePresent(FxRobot robot) {
    assertNotNull(robot.lookup("Load Sheet").queryButton());
    assertNotNull(robot.lookup("Save").queryButton());
    assertNotNull(robot.lookup("Add Box").queryButton());
    assertNotNull(robot.lookup(".choice-box").queryAs(ChoiceBox.class));
  }

  @Test
  public void modeSwitching_SelectedModes_ShouldShowCorrectPane(FxRobot robot) throws Exception {
    Field tilePaneField = SpriteSheetProcessorPane.class.getDeclaredField("tilePane");
    tilePaneField.setAccessible(true);
    GridPane tilePane = (GridPane) tilePaneField.get(pane);

    Field colsPaneField = SpriteSheetProcessorPane.class.getDeclaredField("colsRowsPane");
    colsPaneField.setAccessible(true);
    GridPane colsPane = (GridPane) colsPaneField.get(pane);

    Field manualPaneField = SpriteSheetProcessorPane.class.getDeclaredField("manualPane");
    manualPaneField.setAccessible(true);
    GridPane manualPane = (GridPane) manualPaneField.get(pane);

    assertTrue(tilePane.isVisible());
    assertFalse(colsPane.isVisible());
    assertFalse(manualPane.isVisible());

    robot.clickOn(".choice-box").clickOn("COLS_ROWS");
    WaitForAsyncUtils.waitForFxEvents();
    assertFalse(tilePane.isVisible());
    assertTrue(colsPane.isVisible());
    assertFalse(manualPane.isVisible());

    robot.clickOn(".choice-box").clickOn("MANUAL");
    WaitForAsyncUtils.waitForFxEvents();
    assertFalse(tilePane.isVisible());
    assertFalse(colsPane.isVisible());
    assertTrue(manualPane.isVisible());
  }

  @Test
  public void addManualRegion_WhenNoImageLoaded_ShouldNotAddRegion(FxRobot robot) {
    robot.clickOn(".choice-box").clickOn("MANUAL");
    WaitForAsyncUtils.waitForFxEvents();
    @SuppressWarnings("unchecked")
    TableView<SpriteRegion> table = pane.lookupAll(".table-view").stream()
        .map(n -> (TableView<SpriteRegion>) n).collect(Collectors.toList()).get(0);
    robot.clickOn("Add Box");
    WaitForAsyncUtils.waitForFxEvents();
    assertTrue(table.getItems().isEmpty());
  }

  @Test
  public void addManualRegion_WithImageAndValidInputs_ShouldAddOneRegion(FxRobot robot) throws Exception {
    robot.clickOn(".choice-box").clickOn("MANUAL");
    WaitForAsyncUtils.waitForFxEvents();

    Field sheetViewField = SpriteSheetProcessorPane.class.getDeclaredField("sheetView");
    sheetViewField.setAccessible(true);
    ImageView sheetView = (ImageView) sheetViewField.get(pane);
    Platform.runLater(() -> sheetView.setImage(new WritableImage(20, 20)));
    WaitForAsyncUtils.waitForFxEvents();

    Field xField = SpriteSheetProcessorPane.class.getDeclaredField("manualX");
    Field yField = SpriteSheetProcessorPane.class.getDeclaredField("manualY");
    Field wField = SpriteSheetProcessorPane.class.getDeclaredField("manualWidth");
    Field hField = SpriteSheetProcessorPane.class.getDeclaredField("manualHeight");
    xField.setAccessible(true);
    yField.setAccessible(true);
    wField.setAccessible(true);
    hField.setAccessible(true);

    TextField x = (TextField) xField.get(pane);
    TextField y = (TextField) yField.get(pane);
    TextField w = (TextField) wField.get(pane);
    TextField h = (TextField) hField.get(pane);

    Platform.runLater(() -> {
      x.setText("2");
      y.setText("3");
      w.setText("5");
      h.setText("5");
    });
    WaitForAsyncUtils.waitForFxEvents();

    robot.clickOn("Add Box");
    WaitForAsyncUtils.waitForFxEvents();

    @SuppressWarnings("unchecked")
    TableView<SpriteRegion> table = pane.lookupAll(".table-view").stream()
        .map(n -> (TableView<SpriteRegion>) n).collect(Collectors.toList()).get(0);
    assertEquals(1, table.getItems().size());
    SpriteRegion region = table.getItems().get(0);
    Rectangle2D bounds = region.getBounds();
    assertEquals("sprite_0", region.getName());
    assertEquals(2, bounds.getMinX());
    assertEquals(3, bounds.getMinY());
    assertEquals(5, bounds.getWidth());
    assertEquals(5, bounds.getHeight());
  }

  @Test
  public void tileSizeStrategy_On64x64Image_ShouldCreateFourRegions() throws Exception {
    Field sheetViewField = SpriteSheetProcessorPane.class.getDeclaredField("sheetView");
    sheetViewField.setAccessible(true);
    ImageView sheetView = (ImageView) sheetViewField.get(pane);
    Platform.runLater(() -> sheetView.setImage(new WritableImage(64, 64)));
    WaitForAsyncUtils.waitForFxEvents();

    Method recompute = SpriteSheetProcessorPane.class.getDeclaredMethod("recomputeRegions");
    recompute.setAccessible(true);
    Platform.runLater(() -> {
      try { recompute.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();

    Field regionsField = SpriteSheetProcessorPane.class.getDeclaredField("regions");
    regionsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ObservableList<SpriteRegion> regions = (ObservableList<SpriteRegion>) regionsField.get(pane);
    assertEquals(4, regions.size());
    Set<String> names = regions.stream().map(SpriteRegion::getName).collect(toSet());
    assertTrue(names.containsAll(Set.of("r0_c0","r0_c1","r1_c0","r1_c1")));
  }

  @Test
  public void colsRowsStrategy_On50x40With5x4_ShouldCreateTwentyRegions() throws Exception {
    Field sheetViewField = SpriteSheetProcessorPane.class.getDeclaredField("sheetView");
    sheetViewField.setAccessible(true);
    ImageView sheetView = (ImageView) sheetViewField.get(pane);
    Platform.runLater(() -> sheetView.setImage(new WritableImage(50, 40)));
    WaitForAsyncUtils.waitForFxEvents();

    Field modeBoxField = SpriteSheetProcessorPane.class.getDeclaredField("modeBox");
    modeBoxField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ChoiceBox<SpriteSheetMode> modeBox = (ChoiceBox<SpriteSheetMode>) modeBoxField.get(pane);
    Platform.runLater(() -> modeBox.setValue(SpriteSheetMode.COLS_ROWS));
    WaitForAsyncUtils.waitForFxEvents();

    Field colsField = SpriteSheetProcessorPane.class.getDeclaredField("numCols");
    Field rowsField = SpriteSheetProcessorPane.class.getDeclaredField("numRows");
    colsField.setAccessible(true);
    rowsField.setAccessible(true);
    TextField cols = (TextField) colsField.get(pane);
    TextField rows = (TextField) rowsField.get(pane);

    Platform.runLater(() -> {
      cols.setText("5");
      rows.setText("4");
    });
    WaitForAsyncUtils.waitForFxEvents();

    Method recompute = SpriteSheetProcessorPane.class.getDeclaredMethod("recomputeRegions");
    recompute.setAccessible(true);
    Platform.runLater(() -> {
      try { recompute.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();

    Field regionsField = SpriteSheetProcessorPane.class.getDeclaredField("regions");
    regionsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ObservableList<SpriteRegion> regions = (ObservableList<SpriteRegion>) regionsField.get(pane);
    assertEquals(20, regions.size());
  }

  @Test
  public void saveAtlas_WhenNoImageOrRegions_ShouldNotThrow() throws Exception {
    Method saveAtlas = SpriteSheetProcessorPane.class.getDeclaredMethod("saveAtlas");
    saveAtlas.setAccessible(true);
    Platform.runLater(() -> {
      try { saveAtlas.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();

    Field sheetViewField = SpriteSheetProcessorPane.class.getDeclaredField("sheetView");
    sheetViewField.setAccessible(true);
    ImageView sheetView = (ImageView) sheetViewField.get(pane);
    Platform.runLater(() -> sheetView.setImage(new WritableImage(1, 1)));
    WaitForAsyncUtils.waitForFxEvents();

    Platform.runLater(() -> {
      try { saveAtlas.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  public void parseTextField_WithValidAndInvalid_ShouldBehaveAsExpected() throws Exception {
    Method parse = SpriteSheetProcessorPane.class.getDeclaredMethod("parse", TextField.class);
    parse.setAccessible(true);
    TextField valid = new TextField("42");
    int value = (int) parse.invoke(pane, valid);
    assertEquals(42, value);
    TextField invalid = new TextField("notInt");
    InvocationTargetException ex = assertThrows(InvocationTargetException.class, () -> parse.invoke(pane, invalid));
    assertTrue(ex.getCause() instanceof NumberFormatException);
  }

  @Test
  public void addClampedRegion_WithOverflowBounds_ShouldClampToImage() throws Exception {
    Field regionsField = SpriteSheetProcessorPane.class.getDeclaredField("regions");
    regionsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ObservableList<SpriteRegion> regions = (ObservableList<SpriteRegion>) regionsField.get(pane);
    regions.clear();

    Method clamp = SpriteSheetProcessorPane.class.getDeclaredMethod(
        "addClampedRegion", String.class, double.class, double.class, double.class, double.class,
        double.class, double.class);
    clamp.setAccessible(true);
    clamp.invoke(pane, "foo", 8.0, 8.0, 10.0, 10.0, 10.0, 10.0);

    assertEquals(1, regions.size());
    SpriteRegion r = regions.get(0);
    Rectangle2D b = r.getBounds();
    assertEquals(2, (int) b.getWidth());
    assertEquals(2, (int) b.getHeight());
  }

  @Test
  public void redrawOverlay_WhenManualModeAndImage_SetStrokeDoesNotThrow(FxRobot robot) throws Exception {
    Method redraw = SpriteSheetProcessorPane.class.getDeclaredMethod("redrawOverlay");
    redraw.setAccessible(true);

    Platform.runLater(() -> {
      try { redraw.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();

    Field sheetViewField = SpriteSheetProcessorPane.class.getDeclaredField("sheetView");
    sheetViewField.setAccessible(true);
    ImageView sheetView = (ImageView) sheetViewField.get(pane);
    Platform.runLater(() -> sheetView.setImage(new WritableImage(5, 5)));
    WaitForAsyncUtils.waitForFxEvents();

    Field modeBoxField = SpriteSheetProcessorPane.class.getDeclaredField("modeBox");
    modeBoxField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ChoiceBox<SpriteSheetMode> modeBox = (ChoiceBox<SpriteSheetMode>) modeBoxField.get(pane);
    Platform.runLater(() -> modeBox.setValue(SpriteSheetMode.MANUAL));
    WaitForAsyncUtils.waitForFxEvents();

    for (String fName : List.of("manualX", "manualY", "manualWidth", "manualHeight")) {
      Field f = SpriteSheetProcessorPane.class.getDeclaredField(fName);
      f.setAccessible(true);
      ((TextField) f.get(pane)).setText("1");
    }
    WaitForAsyncUtils.waitForFxEvents();

    Platform.runLater(() -> {
      try { redraw.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();
  }

  @Test
  public void colsRowsRemainder_WhenGrid3x3On10x10_LastColumnGetsRemainder() throws Exception {
    Field sheetViewField = SpriteSheetProcessorPane.class.getDeclaredField("sheetView");
    sheetViewField.setAccessible(true);
    ImageView sheetView = (ImageView) sheetViewField.get(pane);
    Platform.runLater(() -> sheetView.setImage(new WritableImage(10, 10)));
    WaitForAsyncUtils.waitForFxEvents();

    Field modeBoxField = SpriteSheetProcessorPane.class.getDeclaredField("modeBox");
    modeBoxField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ChoiceBox<SpriteSheetMode> modeBox = (ChoiceBox<SpriteSheetMode>) modeBoxField.get(pane);
    Platform.runLater(() -> modeBox.setValue(SpriteSheetMode.COLS_ROWS));
    WaitForAsyncUtils.waitForFxEvents();

    Field colsField = SpriteSheetProcessorPane.class.getDeclaredField("numCols");
    Field rowsField = SpriteSheetProcessorPane.class.getDeclaredField("numRows");
    colsField.setAccessible(true);
    rowsField.setAccessible(true);
    TextField cols = (TextField) colsField.get(pane);
    TextField rows = (TextField) rowsField.get(pane);

    Platform.runLater(() -> {
      cols.setText("3");
      rows.setText("3");
    });
    WaitForAsyncUtils.waitForFxEvents();

    Method recompute = SpriteSheetProcessorPane.class.getDeclaredMethod("recomputeRegions");
    recompute.setAccessible(true);
    Platform.runLater(() -> {
      try { recompute.invoke(pane); } catch (Exception e) { throw new RuntimeException(e); }
    });
    WaitForAsyncUtils.waitForFxEvents();

    Field regionsField = SpriteSheetProcessorPane.class.getDeclaredField("regions");
    regionsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    ObservableList<SpriteRegion> regions = (ObservableList<SpriteRegion>) regionsField.get(pane);

    assertEquals(9, regions.size());
    boolean hasWide = regions.stream()
        .filter(r -> r.getName().endsWith("_c2"))
        .anyMatch(r -> (int) r.getBounds().getWidth() == 4);
    assertTrue(hasWide);
  }
}
