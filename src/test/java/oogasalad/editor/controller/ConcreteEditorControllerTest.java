package oogasalad.editor.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.editor.controller.level.EditorDataAPI;
import oogasalad.editor.controller.object.IdentityDataManager; // Correct import
import oogasalad.editor.controller.listeners.EditorListenerNotifier;
import oogasalad.editor.model.data.EditorLevelData; // Import EditorLevelData
import oogasalad.editor.model.data.object.DynamicVariable;
import oogasalad.editor.model.data.object.DynamicVariableContainer;
import oogasalad.editor.model.data.object.EditorObject;
import oogasalad.editor.model.data.object.event.EditorEvent;
import oogasalad.editor.model.data.object.event.ExecutorData;
import oogasalad.editor.view.EditorViewListener;
import oogasalad.exceptions.EditorSaveException;
import oogasalad.fileparser.records.BlueprintData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link ConcreteEditorController}.
 * Focuses on testing controller logic by mocking direct dependencies
 * and verifying interactions, avoiding complex UI or internal handler mocking.
 */
class ConcreteEditorControllerTest {

  @Mock
  private EditorDataAPI mockEditorDataAPI;
  @Mock
  private EditorListenerNotifier mockListenerNotifier;
  @Mock
  private EditorViewListener mockViewListener;
  @Mock
  private EditorObject mockEditorObject;
  @Mock
  private DynamicVariableContainer mockVariableContainer;
  @Mock
  private IdentityDataManager mockIdentityDataManager; // Mock the concrete class
  @Mock
  private EditorLevelData mockLevelData; // Mock EditorLevelData

  @Captor
  private ArgumentCaptor<UUID> uuidCaptor;
  @Captor
  private ArgumentCaptor<String> stringCaptor;
  @Captor
  private ArgumentCaptor<Double> doubleCaptor;
  @Captor
  private ArgumentCaptor<Integer> intCaptor;
  @Captor
  private ArgumentCaptor<DynamicVariable> dynamicVariableCaptor;
  @Captor
  private ArgumentCaptor<EditorObject> editorObjectCaptor;
  @Captor
  private ArgumentCaptor<Boolean> booleanCaptor;


  private ConcreteEditorController controller;
  private AutoCloseable mocksClosable;
  private UUID testObjectId;
  private String testEventId;

  @BeforeEach
  void setUp() {
    mocksClosable = MockitoAnnotations.openMocks(this);
    // Mock nested API calls needed by the controller
    when(mockEditorDataAPI.getDynamicVariableContainer()).thenReturn(mockVariableContainer);
    when(mockEditorDataAPI.getIdentityDataAPI()).thenReturn(mockIdentityDataManager);
    // *** ADDED: Mock getLevel() to return the mockLevelData ***
    when(mockEditorDataAPI.getLevel()).thenReturn(mockLevelData);
    // Mock methods needed by internal handlers that might be called indirectly
    when(mockEditorDataAPI.createEditorObject()).thenReturn(UUID.randomUUID());
    when(mockEditorDataAPI.getEditorObject(any())).thenReturn(mockEditorObject); // Return mock object
    when(mockEditorObject.getId()).thenReturn(UUID.randomUUID()); // Give mock object an ID

    controller = new ConcreteEditorController(mockEditorDataAPI, mockListenerNotifier);
    testObjectId = UUID.randomUUID();
    testEventId = "TestEvent";
  }

  @AfterEach
  void tearDown() throws Exception {
    if (mocksClosable != null) {
      mocksClosable.close();
    }
  }

  @Test
  void testConstructorSuccess() {
    assertNotNull(controller);
    assertNotNull(controller.getEditorDataAPI());
    assertNull(controller.getCurrentSelectedObjectId());
    // Verify getLevel was called during internal handler construction
    verify(mockEditorDataAPI, atLeastOnce()).getLevel();
  }

  @Test
  void testConstructorNullArgsThrowsException() {
    assertThrows(NullPointerException.class, () -> new ConcreteEditorController(null, mockListenerNotifier));
    assertThrows(NullPointerException.class, () -> new ConcreteEditorController(mockEditorDataAPI, null));
  }

  @Test
  void testRegisterUnregisterListener() {
    controller.registerViewListener(mockViewListener);
    verify(mockListenerNotifier).registerViewListener(mockViewListener);

    controller.unregisterViewListener(mockViewListener);
    verify(mockListenerNotifier).unregisterViewListener(mockViewListener);
  }

  @Test
  void testSetActiveTool() {
    controller.setActiveTool("newTool");
    // Test primarily for coverage
  }

  @Test
  void testNotifyObjectSelected() {
    controller.notifyObjectSelected(testObjectId);
    assertEquals(testObjectId, controller.getCurrentSelectedObjectId());
    verify(mockListenerNotifier).notifySelectionChanged(uuidCaptor.capture());
    assertEquals(testObjectId, uuidCaptor.getValue());

    reset(mockListenerNotifier);
    controller.notifyObjectSelected(testObjectId);
    verify(mockListenerNotifier, never()).notifySelectionChanged(any());
  }

  @Test
  void testNotifyObjectDeselected() {
    controller.notifyObjectSelected(testObjectId);
    controller.notifyObjectDeselected();
    assertNull(controller.getCurrentSelectedObjectId());
    verify(mockListenerNotifier, times(2)).notifySelectionChanged(uuidCaptor.capture());
    assertNull(uuidCaptor.getValue());
  }

  @Test
  void testNotifyPrefabsChanged() {
    controller.notifyPrefabsChanged();
    verify(mockListenerNotifier).notifyPrefabsChanged();
  }

  @Test
  void testNotifyErrorOccurred() {
    String errorMessage = "Test Error";
    controller.notifyErrorOccurred(errorMessage);
    verify(mockListenerNotifier).notifyErrorOccurred(stringCaptor.capture());
    assertEquals(errorMessage, stringCaptor.getValue());
  }


  @Test
  void testRequestObjectRemovalSuccess() {
    when(mockEditorDataAPI.removeEditorObject(eq(testObjectId))).thenReturn(true);
    controller.notifyObjectSelected(testObjectId); // Select it first
    controller.requestObjectRemoval(testObjectId);

    verify(mockEditorDataAPI).removeEditorObject(testObjectId);
    verify(mockListenerNotifier).notifyObjectRemoved(testObjectId);
    verify(mockListenerNotifier).notifySelectionChanged(null); // Should deselect
    assertNull(controller.getCurrentSelectedObjectId());
  }

  @Test
  void testRequestObjectRemovalNotFound() {
    when(mockEditorDataAPI.removeEditorObject(eq(testObjectId))).thenReturn(false);
    controller.requestObjectRemoval(testObjectId);

    verify(mockEditorDataAPI).removeEditorObject(testObjectId);
    verify(mockListenerNotifier, never()).notifyObjectRemoved(any());
    verify(mockListenerNotifier).notifyErrorOccurred(contains("could not be removed"));
  }

  @Test
  void testRequestObjectUpdateSuccess() {
    when(mockEditorObject.getId()).thenReturn(testObjectId);
    when(mockEditorDataAPI.updateEditorObject(eq(mockEditorObject))).thenReturn(true);

    controller.requestObjectUpdate(mockEditorObject);

    verify(mockEditorDataAPI).updateEditorObject(mockEditorObject);
    verify(mockListenerNotifier).notifyObjectUpdated(testObjectId);
  }

  @Test
  void testGetEditorObject() {
    when(mockEditorDataAPI.getEditorObject(eq(testObjectId))).thenReturn(mockEditorObject);
    EditorObject result = controller.getEditorObject(testObjectId);
    assertSame(mockEditorObject, result);
    verify(mockEditorDataAPI).getEditorObject(testObjectId);
  }

  @Test
  void testGetObjectIDAt() {
    // Mock the underlying API call used by the internal handler
    when(mockEditorDataAPI.getObjectDataMap()).thenReturn(Collections.emptyMap());
    UUID result = controller.getObjectIDAt(100.0, 200.0);
    assertNull(result); // Expect null if no object found at coords
    verify(mockEditorDataAPI).getObjectDataMap(); // Verify the underlying data access
  }

  // --- Event Handling Tests (Verify Notifications Only) ---


  @Test
  void testGetEventsForObject() {
    // Verify it returns non-null without mocking the non-existent API method
    Map<String, EditorEvent> actualEvents = controller.getEventsForObject(testObjectId);
    assertNotNull(actualEvents);
  }




  @Test
  void testGetEventConditions() {
    // Verify it returns non-null
    List<List<ExecutorData>> actual = controller.getEventConditions(testObjectId, testEventId);
    assertNotNull(actual);
  }




  @Test
  void testGetEventOutcomes() {
    // Verify it returns non-null
    List<ExecutorData> actual = controller.getEventOutcomes(testObjectId, testEventId);
    assertNotNull(actual);
  }

  // --- Parameter and Variable Tests ---

  @Test
  void testAddDynamicVariable() {
    DynamicVariable variable = new DynamicVariable("score", "int", "0", "desc");
    controller.addDynamicVariable(variable);
    verify(mockVariableContainer).addVariable(dynamicVariableCaptor.capture());
    assertSame(variable, dynamicVariableCaptor.getValue());
    verify(mockListenerNotifier).notifyDynamicVariablesChanged();
  }


  @Test
  void testSetObjectStringParameter() {
    String key = "tag";
    String value = "Player";
    controller.setObjectStringParameter(testObjectId, key, value);
    verify(mockIdentityDataManager).setStringParameter(testObjectId, key, value);
    verify(mockListenerNotifier).notifyObjectUpdated(testObjectId);
  }

  @Test
  void testSetObjectDoubleParameter() {
    String key = "health";
    Double value = 100.0;
    controller.setObjectDoubleParameter(testObjectId, key, value);
    verify(mockIdentityDataManager).setDoubleParameter(testObjectId, key, value);
    verify(mockListenerNotifier).notifyObjectUpdated(testObjectId);
  }

  @Test
  void testRemoveObjectParameter() {
    String key = "oldValue";
    controller.removeObjectParameter(testObjectId, key);
    verify(mockIdentityDataManager).removeParameter(testObjectId, key);
    verify(mockListenerNotifier).notifyObjectUpdated(testObjectId);
  }

  @Test
  void testGetObjectStringParameters() {
    Map<String, String> expected = Map.of("tag", "Enemy");
    when(mockIdentityDataManager.getStringParameters(testObjectId)).thenReturn(expected);
    Map<String, String> actual = controller.getObjectStringParameters(testObjectId);
    assertSame(expected, actual);
    verify(mockIdentityDataManager).getStringParameters(testObjectId);
  }

  @Test
  void testGetObjectDoubleParameters() {
    Map<String, Double> expected = Map.of("speed", 5.0);
    when(mockIdentityDataManager.getDoubleParameters(testObjectId)).thenReturn(expected);
    Map<String, Double> actual = controller.getObjectDoubleParameters(testObjectId);
    assertSame(expected, actual);
    verify(mockIdentityDataManager).getDoubleParameters(testObjectId);
  }

  // --- Save/Load and State Tests ---

  @Test
  void testSaveLevelData() throws EditorSaveException {
    String fileName = "testSave.xml";
    controller.saveLevelData(fileName);
    verify(mockEditorDataAPI).saveLevelData(fileName);
  }

  @Test
  void testLoadLevelData() throws Exception {
    String fileName = "testLoad.xml";
    controller.loadLevelData(fileName);
    verify(mockEditorDataAPI).loadLevelData(fileName);
  }

  @Test
  void testSetGetCellSize() {
    int newSize = 64;
    controller.setCellSize(newSize);
    assertEquals(newSize, controller.getCellSize());
    verify(mockListenerNotifier).notifyCellSizeChanged(intCaptor.capture());
    assertEquals(newSize, intCaptor.getValue());

    controller.setCellSize(0); // Test invalid size
    assertEquals(newSize, controller.getCellSize()); // Should remain unchanged
    verify(mockListenerNotifier, times(1)).notifyCellSizeChanged(anyInt()); // Should not notify again
  }

  @Test
  void testSetIsSnapToGrid() {
    controller.setSnapToGrid(false);
    assertFalse(controller.isSnapToGrid());
    verify(mockListenerNotifier).notifySnapToGridChanged(booleanCaptor.capture());
    assertFalse(booleanCaptor.getValue());

    controller.setSnapToGrid(true);
    assertTrue(controller.isSnapToGrid());
    verify(mockListenerNotifier, times(2)).notifySnapToGridChanged(booleanCaptor.capture());
    assertTrue(booleanCaptor.getValue());
  }
}
