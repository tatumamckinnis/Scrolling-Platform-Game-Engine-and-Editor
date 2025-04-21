// oogasalad_team03/src/test/java/oogasalad/editor/view/EditorComponentFactoryTest.java
package oogasalad.editor.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.panes.properties.PropertiesTabComponentFactory;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.editor.view.tools.ObjectInteractionTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.MockedStatic;
import org.testfx.framework.junit5.ApplicationExtension;


@ExtendWith(ApplicationExtension.class)
class EditorComponentFactoryTest {

  // --- Constants (same as before) ---
  private static final String EDITOR_UI_BUNDLE_NAME = "EditorUI";
  private static final String INPUT_TAB_UI_BUNDLE_NAME = "InputTabUI";
  private static final String EDITOR_PROPERTIES_PATH = "/oogasalad/screens/editorScene.properties";
  private static final String CSS_PATH = "/oogasalad/css/editor/editor.css";
  private static final String KEY_MAP_TITLE = "mapTitle";
  private static final String KEY_ADD_ENTITY_TOOL = "addEntityTool";
  private static final String KEY_SELECT_TOOL = "selectTool";
  private static final String KEY_PROPERTIES_TITLE = "propertiesTitle";
  private static final String KEY_PROPERTIES_TAB = "propertiesTab";
  private static final String KEY_INPUT_TAB = "inputTab";
  private static final String PROP_EDITOR_WIDTH = "editor.width";
  private static final String PROP_EDITOR_HEIGHT = "editor.height";
  private static final String PROP_MAP_WIDTH = "editor.map.width";
  private static final String PROP_COMPONENT_WIDTH = "editor.component.width";
  private static final String PROP_CELL_SIZE = "editor.map.cellSize";
  private static final String PROP_ZOOM_SCALE = "editor.map.zoomScale";
  private static final String PROP_ZOOM_STEP = "editor.map.zoomStep";
  private static final String PROP_ENTITY_TYPE = "editor.tool.entity.type";
  private static final String PROP_ENTITY_PREFIX = "editor.tool.entity.prefix";


  // --- Mocks (same as before) ---
  @Mock private EditorController mockEditorController;
  @Mock private InputTabComponentFactory mockInputTabFactory; // Mock instance to be injected
  @Mock private PropertiesTabComponentFactory mockPropertiesTabFactory; // Mock instance to be injected
  @Mock private EditorGameView mockGameView;
  @Mock private ResourceBundle mockUiBundle;
  @Mock private Properties mockEditorProperties;

  // --- Captors (same as before) ---
  @Captor private ArgumentCaptor<EditorViewListener> listenerCaptor;
  @Captor private ArgumentCaptor<ObjectInteractionTool> toolCaptor;

  // --- Test Setup ---
  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    lenient().doNothing().when(mockEditorController).registerViewListener(any(EditorViewListener.class));
    // Stub the methods of the mock factories that will be called by the SUT
    lenient().when(mockInputTabFactory.createInputTabPanel()).thenReturn(new Pane());
    lenient().when(mockPropertiesTabFactory.createPropertiesPane()).thenReturn(new ScrollPane(new VBox()));
    lenient().doNothing().when(mockGameView).updateCurrentTool(any());
    lenient().when(mockUiBundle.getString(anyString())).thenReturn("Mock UI String");
    lenient().when(mockEditorProperties.getProperty(anyString())).thenReturn(null);
    lenient().when(mockEditorProperties.getProperty(anyString(), anyString())).thenAnswer(invocation -> invocation.getArgument(1));
  }

  /** Sets up detailed stubbing for ResourceBundle and Properties mocks. */
  private void setupMockResourceDetails() {
    when(mockUiBundle.getString(KEY_MAP_TITLE)).thenReturn("Map");
    when(mockUiBundle.getString(KEY_ADD_ENTITY_TOOL)).thenReturn("Add Entity");
    when(mockUiBundle.getString(KEY_SELECT_TOOL)).thenReturn("Select");
    when(mockUiBundle.getString(KEY_PROPERTIES_TITLE)).thenReturn("Components");
    when(mockUiBundle.getString(KEY_PROPERTIES_TAB)).thenReturn("Properties");
    when(mockUiBundle.getString(KEY_INPUT_TAB)).thenReturn("Input");

    when(mockEditorProperties.getProperty(PROP_EDITOR_WIDTH, "1200")).thenReturn("1200");
    when(mockEditorProperties.getProperty(PROP_EDITOR_WIDTH)).thenReturn("1200");
    when(mockEditorProperties.getProperty(PROP_EDITOR_HEIGHT, "800")).thenReturn("800");
    when(mockEditorProperties.getProperty(PROP_EDITOR_HEIGHT)).thenReturn("800");
    when(mockEditorProperties.getProperty(PROP_MAP_WIDTH, "800")).thenReturn("800");
    when(mockEditorProperties.getProperty(PROP_MAP_WIDTH)).thenReturn("800");
    when(mockEditorProperties.getProperty(PROP_COMPONENT_WIDTH, "400")).thenReturn("400");
    when(mockEditorProperties.getProperty(PROP_COMPONENT_WIDTH)).thenReturn("400");
    when(mockEditorProperties.getProperty(PROP_CELL_SIZE, "32")).thenReturn("32");
    when(mockEditorProperties.getProperty(PROP_CELL_SIZE)).thenReturn("32");
    when(mockEditorProperties.getProperty(PROP_ZOOM_SCALE, "1.0")).thenReturn("1.0");
    when(mockEditorProperties.getProperty(PROP_ZOOM_SCALE)).thenReturn("1.0");
    when(mockEditorProperties.getProperty(PROP_ZOOM_STEP, "0.05")).thenReturn("0.05");
    when(mockEditorProperties.getProperty(PROP_ZOOM_STEP)).thenReturn("0.05");
    when(mockEditorProperties.getProperty(eq(PROP_ENTITY_TYPE), anyString())).thenReturn("MOCK_ENTITY");
    when(mockEditorProperties.getProperty(PROP_ENTITY_TYPE)).thenReturn("MOCK_ENTITY");
    when(mockEditorProperties.getProperty(eq(PROP_ENTITY_PREFIX), anyString())).thenReturn("MOCK_PREFIX_");
    when(mockEditorProperties.getProperty(PROP_ENTITY_PREFIX)).thenReturn("MOCK_PREFIX_");
  }

  /** Helper to run setup and assertions on the FX thread within the static mock scope. */
  private Scene runTestOnFxThread(Consumer<Scene> sceneAssertions) throws Exception {
    AtomicReference<Scene> sceneRef = new AtomicReference<>();
    AtomicReference<Throwable> exceptionRef = new AtomicReference<>();

    // Use try-with-resources for static mocks
    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      mockedLoader.when(() -> EditorResourceLoader.loadProperties(EDITOR_PROPERTIES_PATH)).thenReturn(mockEditorProperties);
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(EDITOR_UI_BUNDLE_NAME)).thenReturn(mockUiBundle);
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(INPUT_TAB_UI_BUNDLE_NAME)).thenReturn(mockUiBundle);
      setupMockResourceDetails();

      // Create factory instance (using anonymous class only for GameView injection now)
      EditorComponentFactory factory = new EditorComponentFactory(mockEditorController) {
        Field gameViewField; // Cache field
        {
          try {
            gameViewField = EditorComponentFactory.class.getDeclaredField("gameView");
            gameViewField.setAccessible(true);
          } catch (NoSuchFieldException e) { throw new RuntimeException(e); }
        }
        // createGameView is called during createMapPane -> createEditorScene
        void createGameView() {
          try { gameViewField.set(this, mockGameView); } catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }
        // REMOVED useless overrides for createInputTabFactory/createPropertiesTabFactory
      };

      // **Inject Mock Factories via Reflection AFTER constructor**
      try {
        Field inputFactoryField = EditorComponentFactory.class.getDeclaredField("inputTabFactory");
        inputFactoryField.setAccessible(true);
        inputFactoryField.set(factory, mockInputTabFactory); // Replace real with mock

        Field propsFactoryField = EditorComponentFactory.class.getDeclaredField("propertiesTabFactory");
        propsFactoryField.setAccessible(true);
        propsFactoryField.set(factory, mockPropertiesTabFactory); // Replace real with mock
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new RuntimeException("Failed to inject mock factories via reflection", e);
      }

      // Use Platform.runLater and wait
      CountDownLatch latch = new CountDownLatch(1);
      Platform.runLater(() -> {
        try {
          Scene scene = factory.createEditorScene(); // Now uses injected mock factories
          sceneRef.set(scene);
          if (sceneAssertions != null) {
            sceneAssertions.accept(scene);
          }
        } catch (Throwable e) {
          exceptionRef.set(e);
        } finally {
          latch.countDown();
        }
      });

      if (!latch.await(10, TimeUnit.SECONDS)) {
        fail("FX thread scene creation/assertion timed out.");
      }
      if (exceptionRef.get() != null) {
        if (exceptionRef.get() instanceof Error) throw (Error) exceptionRef.get();
        throw new RuntimeException("Exception on FX thread", exceptionRef.get());
      }
      assertNotNull(sceneRef.get(), "Scene was not created on FX thread.");
      return sceneRef.get();

    } // Static mocks closed here
  }


  // --- Tests ---

  @Test
  void testConstructorSuccess() throws Exception {
    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString())).thenReturn(mockEditorProperties);
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString())).thenReturn(mockUiBundle);
      MockitoAnnotations.openMocks(this);
      // No anonymous class needed here as we only test constructor
      EditorComponentFactory factory = new EditorComponentFactory(mockEditorController);
      assertNotNull(factory);
      // Constructor still registers REAL factories, so cannot verify mock registration here
    }
  }

  @Test
  void testConstructorNullController() {
    assertThrows(NullPointerException.class, () -> new EditorComponentFactory(null));
  }

  @Test
  void testConstructorResourceLoadFailure() {
    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString()))
          .thenThrow(new RuntimeException("Failed to load properties"));
      MockitoAnnotations.openMocks(this);
      assertThrows(RuntimeException.class, () -> new EditorComponentFactory(mockEditorController));
    }
  }

  @Test
  void testCreateEditorSceneStructure() throws Exception {
    runTestOnFxThread(scene -> {
      assertNotNull(scene, "Scene should not be null");
      assertTrue(scene.getRoot() instanceof SplitPane, "Root should be SplitPane");
      assertEquals("editor-root", scene.getRoot().getId());
      SplitPane root = (SplitPane) scene.getRoot();
      assertEquals(2, root.getItems().size());
      assertTrue(root.getItems().get(0) instanceof SplitPane);
      SplitPane leftSplit = (SplitPane) root.getItems().get(0);
      assertEquals(Orientation.VERTICAL, leftSplit.getOrientation());
      assertEquals(2, leftSplit.getItems().size());
      assertEquals("map-pane", leftSplit.getItems().get(0).getId());
      assertEquals("prefab-pane", leftSplit.getItems().get(1).getId());
      assertEquals("component-pane", root.getItems().get(1).getId());
    });
  }

  @Test
  void testCreateEditorSceneTabs() throws Exception {
    runTestOnFxThread(scene -> {
      // Use the improved finder
      TabPane tabPane = findNodeRecursively(scene.getRoot(), "#component-tab-pane", TabPane.class)
          .orElseThrow(() -> new AssertionError("TabPane '#component-tab-pane' not found"));
      assertEquals(2, tabPane.getTabs().size());

      Tab propertiesTab = tabPane.getTabs().get(0);
      assertEquals("Properties", propertiesTab.getText()); // From mockUiBundle
      assertEquals("properties-tab", propertiesTab.getId());
      assertTrue(propertiesTab.getContent() instanceof ScrollPane); // Content from mockPropertiesTabFactory stub
      assertFalse(propertiesTab.isClosable());
      // Verify the *mock* factory's method was called because it was injected
      verify(mockPropertiesTabFactory, atLeastOnce()).createPropertiesPane();

      Tab inputTab = tabPane.getTabs().get(1);
      assertEquals("Input", inputTab.getText()); // From mockUiBundle
      assertEquals("input-tab", inputTab.getId());
      assertTrue(inputTab.getContent() instanceof Pane); // Content from mockInputTabFactory stub
      assertFalse(inputTab.isClosable());
      // Verify the *mock* factory's method was called because it was injected
      verify(mockInputTabFactory, atLeastOnce()).createInputTabPanel();
    });
  }


  @Test
  void testGetIntProperty() {
    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      MockitoAnnotations.openMocks(this);
      mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString())).thenReturn(mockEditorProperties);
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString())).thenReturn(mockUiBundle);
      when(mockUiBundle.getString(anyString())).thenReturn("");

      EditorComponentFactory factory = new EditorComponentFactory(mockEditorController) {
        // Anonymous class only needed if testing methods that use injected factories/views
      };

      // Test VALID: Stub *then* assert
      String validKey = "validIntKey";
      when(mockEditorProperties.getProperty(validKey)).thenReturn("123");
      assertEquals(123, factory.getIntProperty(validKey, 0));

      // Test MISSING:
      String missingKey = "missingIntKey";
      when(mockEditorProperties.getProperty(missingKey)).thenReturn(null);
      assertEquals(99, factory.getIntProperty(missingKey, 99));

      // Test INVALID:
      String invalidKey = "invalidIntKey";
      when(mockEditorProperties.getProperty(invalidKey)).thenReturn("abc");
      assertEquals(50, factory.getIntProperty(invalidKey, 50));

      // Test EMPTY:
      String emptyKey = "emptyIntKey";
      when(mockEditorProperties.getProperty(emptyKey)).thenReturn("");
      assertEquals(25, factory.getIntProperty(emptyKey, 25));
    }
  }

  @Test
  void testGetDoubleProperty() {
    try (MockedStatic<EditorResourceLoader> mockedLoader = mockStatic(EditorResourceLoader.class)) {
      MockitoAnnotations.openMocks(this);
      mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString())).thenReturn(mockEditorProperties);
      mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString())).thenReturn(mockUiBundle);
      when(mockUiBundle.getString(anyString())).thenReturn("");

      EditorComponentFactory factory = new EditorComponentFactory(mockEditorController);

      // Test VALID: Stub then assert
      String validKey = "validDoubleKey";
      when(mockEditorProperties.getProperty(validKey)).thenReturn("123.45");
      assertEquals(123.45, factory.getDoubleProperty(validKey, 0.0));

      // Test MISSING:
      String missingKey = "missingDoubleKey";
      when(mockEditorProperties.getProperty(missingKey)).thenReturn(null);
      assertEquals(99.9, factory.getDoubleProperty(missingKey, 99.9));

      // Test INVALID:
      String invalidKey = "invalidDoubleKey";
      when(mockEditorProperties.getProperty(invalidKey)).thenReturn("xyz");
      assertEquals(50.5, factory.getDoubleProperty(invalidKey, 50.5));

      // Test EMPTY:
      String emptyKey = "emptyDoubleKey";
      when(mockEditorProperties.getProperty(emptyKey)).thenReturn("");
      assertEquals(25.2, factory.getDoubleProperty(emptyKey, 25.2));
    }
  }

  // --- Helper methods for manual node finding (IMPROVED) ---
  private <T extends Node> Optional<T> findNodeRecursively(Node parent, String selector, Class<T> type) {
    if (parent == null) return Optional.empty();
    boolean idMatch = selector.startsWith("#") && Objects.equals(parent.getId(), selector.substring(1));
    if (idMatch && type.isInstance(parent)) return Optional.of(type.cast(parent));
    if (!selector.startsWith("#") && type.isInstance(parent)) return Optional.of(type.cast(parent));

    List<Node> childrenToExplore = new ArrayList<>();
    if (parent instanceof Parent) {
      List<Node> children = ((Parent) parent).getChildrenUnmodifiable();
      if (children != null) childrenToExplore.addAll(children);
    }
    if (parent instanceof ScrollPane) {
      Node content = ((ScrollPane) parent).getContent();
      if (content != null) childrenToExplore.add(content);
    } else if (parent instanceof BorderPane) {
      BorderPane bp = (BorderPane) parent;
      if (bp.getCenter() != null) childrenToExplore.add(bp.getCenter());
      if (bp.getTop() != null) childrenToExplore.add(bp.getTop());
      if (bp.getBottom() != null) childrenToExplore.add(bp.getBottom());
      if (bp.getLeft() != null) childrenToExplore.add(bp.getLeft());
      if (bp.getRight() != null) childrenToExplore.add(bp.getRight());
    } else if (parent instanceof SplitPane) {
      SplitPane sp = (SplitPane) parent;
      if (sp.getItems() != null) childrenToExplore.addAll(sp.getItems());
    } else if (parent instanceof TabPane) { // Check content of selected tab? Or all tabs?
      TabPane tp = (TabPane) parent;
      // Explore content of all tabs
      for (Tab tab : tp.getTabs()) {
        if (tab != null && tab.getContent() != null) {
          childrenToExplore.add(tab.getContent());
        }
      }
    }

    for (Node child : childrenToExplore) {
      Optional<T> found = findNodeRecursively(child, selector, type);
      if (found.isPresent()) return found;
    }
    return Optional.empty();
  }


  private Optional<ToggleButton> findButtonByText(Parent container, String text) {
    if (container == null) return Optional.empty();
    for (Node node : getChildren(container)) { // Use helper to get children robustly
      if (node instanceof ToggleButton && Objects.equals(((ToggleButton) node).getText(), text)) {
        return Optional.of((ToggleButton) node);
      }
      if (node instanceof Parent) {
        Optional<ToggleButton> found = findButtonByText((Parent) node, text);
        if (found.isPresent()) return found;
      }
    }
    return Optional.empty();
  }

  // Helper to get children from various parent types, handling nulls
  private List<Node> getChildren(Parent parent) {
    List<Node> children = new ArrayList<>();
    if (parent == null) return children;

    try { // Standard Parent
      if (parent.getChildrenUnmodifiable() != null) {
        children.addAll(parent.getChildrenUnmodifiable());
      }
    } catch (Exception e) { /* ignore, might not be standard Parent */ }

    // Specific Layouts
    if (parent instanceof ScrollPane && ((ScrollPane) parent).getContent() != null) {
      children.add(((ScrollPane) parent).getContent());
    } else if (parent instanceof BorderPane) {
      BorderPane bp = (BorderPane) parent;
      if (bp.getCenter() != null) children.add(bp.getCenter());
      if (bp.getTop() != null) children.add(bp.getTop());
      if (bp.getBottom() != null) children.add(bp.getBottom());
      if (bp.getLeft() != null) children.add(bp.getLeft());
      if (bp.getRight() != null) children.add(bp.getRight());
    } else if (parent instanceof SplitPane) {
      SplitPane sp = (SplitPane) parent;
      if (sp.getItems() != null) children.addAll(sp.getItems());
    } else if (parent instanceof TabPane) {
      TabPane tp = (TabPane) parent;
      for (Tab tab : tp.getTabs()) {
        if (tab != null && tab.getContent() != null) children.add(tab.getContent());
      }
    }
    return children;
  }
}