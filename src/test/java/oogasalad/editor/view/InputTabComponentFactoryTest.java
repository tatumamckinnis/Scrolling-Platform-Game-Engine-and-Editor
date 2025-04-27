package oogasalad.editor.view;

import java.util.ResourceBundle;
import javafx.stage.Stage;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.factories.InputTabComponentFactory;
import oogasalad.editor.view.resources.EditorResourceLoader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.Start;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link InputTabComponentFactory} focusing on constructor logic,
 * resource loading, and listener method execution paths for coverage, avoiding complex
 * UI mocking.
 */
class InputTabComponentFactoryTest {

  private static final String UI_BUNDLE_BASE_NAME = "oogasalad.config.editor.resources.InputTabUI_en";
  private static final String IDENTIFIERS_PROPERTIES_PATH = "/oogasalad/config/editor/resources/input_tab_component_factory_identifiers.properties";

  @Mock
  private EditorController mockEditorController;

  private InputTabComponentFactory factory;
  private static ResourceBundle testBundle;
  private AutoCloseable mocksClosable;
  private static MockedStatic<EditorResourceLoader> mockedLoader;

  /**
   * Initializes JavaFX platform and static mocks once.
   */
  @BeforeAll
  static void setupBeforeAll() {
    mockedLoader = mockStatic(EditorResourceLoader.class);
    testBundle = ResourceBundle.getBundle(UI_BUNDLE_BASE_NAME);
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
        .thenReturn(testBundle);
  }

  /**
   * Closes static mocks after all tests.
   */
  @AfterAll
  static void tearDownAfterAll() {
    if (mockedLoader != null) {
      mockedLoader.close();
    }
  }

  /**
   * Initializes JavaFX stage (required by ApplicationExtension).
   * @param stage Primary stage provided by TestFX.
   */
  @Start
  private void start(Stage stage) {
  }

  /**
   * Sets up mocks before each test.
   */
  @BeforeEach
  void setUp() throws Exception {
    mocksClosable = MockitoAnnotations.openMocks(this);
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
        .thenReturn(testBundle);
  }

  /**
   * Closes Mockito mocks after each test.
   */
  @AfterEach
  void tearDown() throws Exception {
    if (mocksClosable != null) {
      mocksClosable.close();
    }
  }

  /**
   * Tests successful constructor execution.
   */
  @Test
  void testConstructorSuccess() {
    factory = new InputTabComponentFactory(mockEditorController);
    assertNotNull(factory);
    verify(mockEditorController, never()).addEvent(any(), any());
  }

  /**
   * Tests constructor failure with null controller.
   */
  @Test
  void testConstructorNullControllerThrowsException() {
    assertThrows(NullPointerException.class, () -> new InputTabComponentFactory(null));
  }

  /**
   * Tests constructor failure when resource bundle loading fails.
   */
  @Test
  void testConstructorBundleLoadFailureThrowsException() {
    mockedLoader.reset();
    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
        .thenThrow(new RuntimeException("Test Bundle Load Failure"));

    assertThrows(RuntimeException.class, () -> new InputTabComponentFactory(mockEditorController));

    mockedLoader.when(() -> EditorResourceLoader.loadResourceBundle(anyString()))
        .thenReturn(testBundle);
  }

  /**
   * Tests the getId method for valid and invalid keys.
   * Assumes the real properties file is accessible via classpath.
   */
  @Test
  void testGetId() {
    factory = new InputTabComponentFactory(mockEditorController);
    assertNotNull(factory.getId("ui.bundle.name"));
    assertThrows(RuntimeException.class, () -> factory.getId("invalid.key.does.not.exist"));
  }
}
