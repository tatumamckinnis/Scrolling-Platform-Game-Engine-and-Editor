package oogasalad.editor.view;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.components.EditorGameView;
import oogasalad.editor.view.components.PrefabPalettePane;
import oogasalad.editor.view.factories.EditorComponentFactory;
import oogasalad.editor.view.factories.InputTabComponentFactory;
import oogasalad.editor.view.panes.chat.ChatBotPane;
import oogasalad.editor.view.panes.properties.PropertiesTabComponentFactory;
import oogasalad.editor.view.panes.sprite_properties.SpriteTabComponentFactory;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;


/**
 * Tests for EditorComponentFactory. Uses Mockito for dependencies and TestFX for JavaFX
 * initialization. Attempts to use mockConstruction to intercept internal dependency creation.
 */
@ExtendWith(ApplicationExtension.class)
class EditorComponentFactoryTest {

  private static final String TEST_IDENTIFIERS_PATH = "/oogasalad/config/editor/resources/editor_component_factory_identifiers.properties";
  private static final String TEST_EDITOR_PROPS_PATH = "/oogasalad/config/engine/view/editorScene.properties";
  private static final String TEST_UI_BUNDLE_NAME = "EditorUI";
  private static final String TEST_CSS_PATH = "/oogasalad/css/editor/editor.css";

  @Mock
  private EditorController mockEditorController;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private InputTabComponentFactory mockInputTabFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PropertiesTabComponentFactory mockPropertiesTabFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SpriteTabComponentFactory mockSpriteTabFactory;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private PrefabPalettePane mockPrefabPalettePane;

  @Mock
  private Pane mockInputPane;
  @Mock
  private ScrollPane mockPropertiesPane;
  @Mock
  private ScrollPane mockSpritePane;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private EditorGameView mockGameView;
  @Mock
  private ChatBotPane mockChatBotPane;


  @Captor
  private ArgumentCaptor<EditorViewListener> listenerCaptor;

  private Properties testIdentifierProps;
  private Properties testEditorProps;
  private ResourceBundle testUiBundle;

  private EditorComponentFactory factory;
  private static MockedStatic<EditorResourceLoader> mockedLoader;
  private static MockedStatic<LogManager> mockedLogManager;
  private static MockedStatic<Platform> mockedPlatform;

  private AutoCloseable closeablemocks;

  @BeforeAll
  static void setupBeforeAll() {
    org.apache.logging.log4j.Logger mockLogger = mock(org.apache.logging.log4j.Logger.class);
    mockedLogManager = mockStatic(org.apache.logging.log4j.LogManager.class);
    mockedLogManager.when(() -> org.apache.logging.log4j.LogManager.getLogger(any(Class.class)))
        .thenReturn(mockLogger);
    mockedLogManager.when(() -> org.apache.logging.log4j.LogManager.getLogger(anyString()))
        .thenReturn(mockLogger);

    mockedLoader = mockStatic(EditorResourceLoader.class);
  }

  @AfterAll
  static void tearDownAfterAll() {
    if (mockedLoader != null) mockedLoader.close();
    if (mockedLogManager != null) mockedLogManager.close();
    if (mockedPlatform != null) mockedPlatform.close();
  }

  @Start
  private void start(Stage stage) {

  }

  @BeforeEach
  void setUp() throws IOException {
    closeablemocks = MockitoAnnotations.openMocks(this);


    testIdentifierProps = new Properties();
    try (InputStream input = EditorComponentFactory.class.getResourceAsStream(TEST_IDENTIFIERS_PATH)) {
      assertNotNull(input, "Test setup failed: Could not find identifier properties file: " + TEST_IDENTIFIERS_PATH);
      testIdentifierProps.load(input);
    } catch (IOException e) {
      fail("Test setup failed: Error loading identifier properties file", e);
    }


    testEditorProps = new Properties();
    testEditorProps.setProperty("editor.width", "1300");
    testEditorProps.setProperty("editor.height", "900");

    testEditorProps.setProperty("editor.map.cellSize", "16");
    testEditorProps.setProperty("editor.map.zoomScale", "1.0");
    testEditorProps.setProperty("layout.left.split.divider", "0.7");
    testEditorProps.setProperty("layout.asset.pane.height", "200");
    testEditorProps.setProperty("layout.chat.pane.height", "200");
    testEditorProps.setProperty("editor.tool.entity.type", "ENTITY");
    testEditorProps.setProperty("editor.tool.entity.prefix", "Entity_");

    testUiBundle = new java.util.ListResourceBundle() {
      protected Object[][] getContents() {
        return new Object[][]{
            {"mapTitle", "Test Map"}, {"propertiesTitle", "Test Properties"},
            {"addEntityTool", "Add Entity"}, {"selectTool", "Select"}, {"deleteTool", "Delete"},
            {"clearAllTool", "Clear"}, {"propertiesTab", "Props"}, {"inputTab", "Input"},
            {"spritePropertyTab", "Sprites"}, {"prefabsTabTitle", "Prefabs"},
            {"spritesTabTitle", "Sprite Assets"}, {"errorGameViewNeeded", "GameView Missing"},
            {"logToolSelected", "Tool sel: %s"}, {"logToolDeselected", "Tool desel: %s"},
            {"ChatBotTabTitle", "ChatBot"}

        };
      }
    };


    mockedLoader.when(() -> EditorResourceLoader.loadProperties(eq(testIdentifierProps.getProperty("editor.properties.path"))))
        .thenReturn(testEditorProps);
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(eq(testIdentifierProps.getProperty("ui.bundle.name"))))
        .thenReturn(testUiBundle);


    lenient().when(mockInputTabFactory.createInputTabPanel()).thenReturn(mockInputPane);
    lenient().when(mockPropertiesTabFactory.createPropertiesPane()).thenReturn(mockPropertiesPane);
    lenient().when(mockSpriteTabFactory.createSpritePane()).thenReturn(mockSpritePane);


    lenient().when(mockPrefabPalettePane.selectedPrefabProperty()).thenReturn(new SimpleObjectProperty<>());




    try (
        MockedConstruction<InputTabComponentFactory> mockedInputFactory = mockConstruction(
            InputTabComponentFactory.class,
            (mock, context) -> {

              when(mock.createInputTabPanel()).thenReturn(mockInputPane);




              System.out.println("Intercepted InputTabComponentFactory creation");
            });
        MockedConstruction<PropertiesTabComponentFactory> mockedPropsFactory = mockConstruction(
            PropertiesTabComponentFactory.class,
            (mock, context) -> {
              when(mock.createPropertiesPane()).thenReturn(mockPropertiesPane);
              System.out.println("Intercepted PropertiesTabComponentFactory creation");
            });
        MockedConstruction<SpriteTabComponentFactory> mockedSpriteFactory = mockConstruction(
            SpriteTabComponentFactory.class,
            (mock, context) -> {
              when(mock.createSpritePane()).thenReturn(mockSpritePane);
              System.out.println("Intercepted SpriteTabComponentFactory creation");
            });
        MockedConstruction<PrefabPalettePane> mockedPalette = mockConstruction(
            PrefabPalettePane.class,
            (mock, context) -> {
              when(mock.selectedPrefabProperty()).thenReturn(new SimpleObjectProperty<>());
              System.out.println("Intercepted PrefabPalettePane creation");
            });


        MockedConstruction<EditorGameView> mockedGameViewCons = mockConstruction(
            EditorGameView.class,
            (mock, context) -> {
              System.out.println("Intercepted EditorGameView creation");

            });

        MockedConstruction<ChatBotPane> mockedChatPane = mockConstruction(
            ChatBotPane.class,
            (mock, context) -> {
              System.out.println("Intercepted ChatBotPane creation");
            });

    ) {


      factory = new EditorComponentFactory(mockEditorController);





      verify(mockEditorController, atLeast(4)).registerViewListener(listenerCaptor.capture());
      List<EditorViewListener> capturedListeners = listenerCaptor.getAllValues();
      assertTrue(capturedListeners.stream().anyMatch(l -> l instanceof InputTabComponentFactory));
      assertTrue(capturedListeners.stream().anyMatch(l -> l instanceof PropertiesTabComponentFactory));
      assertTrue(capturedListeners.stream().anyMatch(l -> l instanceof SpriteTabComponentFactory));
      assertTrue(capturedListeners.stream().anyMatch(l -> l instanceof PrefabPalettePane));



    } catch (Exception e) {

      fail("Exception during mock construction or factory instantiation: " + e.getMessage(), e);
    }

    assertNotNull(factory, "Factory should be created");
  }

  @AfterEach
  void tearDown() throws Exception {
    if (closeablemocks != null) {
      closeablemocks.close();
    }
  }




  @Test
  void constructor_Success() {

    assertNotNull(factory);
    mockedLoader.verify(() -> EditorResourceLoader.loadProperties(anyString()), atLeastOnce());
    mockedLoader.verify(() -> EditorResourceLoader.loadResourceBundle(anyString()), atLeastOnce());

  }

  @Test
  void constructor_NullController_ThrowsException() {

    mockedLoader.reset();
    mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString())).thenReturn(testEditorProps);
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString())).thenReturn(testUiBundle);


    assertThrows(NullPointerException.class, () -> new EditorComponentFactory(null),
        "Constructor should throw NullPointerException for null controller.");
  }

  @Test
  void constructor_ResourceLoadFailure_Properties_ThrowsException() {
    mockedLoader.reset();
    mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString()))
        .thenThrow(new RuntimeException("Test: Failed to load props"));
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
        .thenReturn(testUiBundle);

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> new EditorComponentFactory(mockEditorController));

    assertTrue(exception.getMessage().contains("Fatal: Failed to load essential editor resources."),
        "Exception message mismatch: " + exception.getMessage());

  }

  @Test
  void constructor_ResourceLoadFailure_Bundle_ThrowsException() {
    mockedLoader.reset();
    mockedLoader.when(() -> EditorResourceLoader.loadProperties(anyString())).thenReturn(testEditorProps);
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
        .thenThrow(new MissingResourceException("Test: Bundle missing", "class", "key"));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> new EditorComponentFactory(mockEditorController));
    assertTrue(exception.getMessage().contains("Fatal: Failed to load essential editor resources."),
        "Exception message mismatch: " + exception.getMessage());
  }

  @Test
  void getId_ValidKey_ReturnsValue() {
    assertNotNull(factory, "Factory not initialized for getId test");
    assertEquals(testIdentifierProps.getProperty("editor.properties.path"), factory.getId("editor.properties.path"));
    assertEquals(testIdentifierProps.getProperty("id.editor.root"), factory.getId("id.editor.root"));
  }

  @Test
  void getId_MissingKey_ThrowsException() {
    assertNotNull(factory, "Factory not initialized for getId test");
    RuntimeException exception = assertThrows(RuntimeException.class, () -> factory.getId("non.existent.key"));
    assertTrue(exception.getMessage().contains("Missing identifier in properties file for key: non.existent.key"));
  }

  @Test
  void getIntProperty_Valid() {
    assertNotNull(factory, "Factory not initialized for getIntProperty test");

    assertEquals(1300, factory.getIntProperty(factory.getId("prop.editor.width"), 999));
  }

  @Test
  void getIntProperty_InvalidFormat_ReturnsDefault() {
    assertNotNull(factory, "Factory not initialized for getIntProperty test");


    testEditorProps.setProperty(factory.getId("prop.editor.height"), "not-an-int");

    assertEquals(500, factory.getIntProperty("some.other.key.int", 500));

  }

  @Test
  void getIntProperty_MissingKey_ReturnsDefault() {
    assertNotNull(factory, "Factory not initialized for getIntProperty test");
    assertEquals(42, factory.getIntProperty("missing.key.int", 42));
  }

  @Test
  void getDefaultInt_Valid() {
    assertNotNull(factory, "Factory not initialized for getDefaultInt test");
    assertEquals(1200, factory.getDefaultInt("default.editor.width"));
  }

  @Test
  void getDoubleProperty_Valid() {
    assertNotNull(factory, "Factory not initialized for getDoubleProperty test");
    assertEquals(1.0, factory.getDoubleProperty(factory.getId("prop.zoom.scale"), 9.9));
  }

  @Test
  void getDoubleProperty_InvalidFormat_ReturnsDefault() {
    assertNotNull(factory, "Factory not initialized for getDoubleProperty test");
    testEditorProps.setProperty(factory.getId("prop.zoom.scale"), "not-a-double");
    assertEquals(5.5, factory.getDoubleProperty("some.other.key.double", 5.5));
  }

  @Test
  void getDoubleProperty_MissingKey_ReturnsDefault() {
    assertNotNull(factory, "Factory not initialized for getDoubleProperty test");
    assertEquals(3.14, factory.getDoubleProperty("missing.key.double", 3.14));
  }

  @Test
  void getDefaultDouble_Valid() {
    assertNotNull(factory, "Factory not initialized for getDefaultDouble test");
    assertEquals(0.7, factory.getDefaultDouble("layout.left.split.divider"));
  }





  private <T extends Node> T findNodeById(Node parent, String id, Class<T> nodeClass) {
    assertNotNull(parent, "Parent node for lookup cannot be null");
    Node found = parent.lookup("#" + id);
    assertNotNull(found, "Node with ID '" + id + "' not found in parent: " + parent);
    assertTrue(nodeClass.isInstance(found), "Node with ID '" + id + "' is type " + found.getClass().getSimpleName() + ", expected " + nodeClass.getSimpleName());
    return nodeClass.cast(found);
  }

}