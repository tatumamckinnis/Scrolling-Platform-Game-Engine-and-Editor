package oogasalad.editor.model.data.object.event;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for the ExecutorData class.
 * @author Jacob You
 */
class ExecutorDataTest {

  private ExecutorData executorData;
  private Map<String, String> stringMap;
  private Map<String, Double> doubleMap;

  /**
   * Sets up a fresh ExecutorData before each test.
   */
  @BeforeEach
  void setUp() {
    stringMap = new HashMap<>();
    doubleMap = new HashMap<>();
    executorData = new ExecutorData("DamageExec", stringMap, doubleMap);
  }

  /**
   * Tests constructor assigns fields correctly.
   */
  @Test
  void constructor_ValidInput_FieldsAssigned() {
    assertEquals("DamageExec", executorData.getExecutorName());
    assertSame(stringMap, executorData.getStringParams());
    assertSame(doubleMap, executorData.getDoubleParams());
  }

  /**
   * Tests setStringParam inserts a key–value pair.
   */
  @Test
  void setStringParam_ValidArgs_InsertsPair() {
    executorData.setStringParam("key", "val");
    assertEquals("val", executorData.getStringParams().get("key"));
  }

  /**
   * Tests setStringParam with null value performs no insertion.
   */
  @Test
  void setStringParam_NullValue_NoChange() {
    executorData.setStringParam("key", null);
    assertFalse(executorData.getStringParams().containsKey("key"));
  }

  /**
   * Tests setStringParam with null name performs no insertion.
   */
  @Test
  void setStringParam_NullName_NoChange() {
    executorData.setStringParam(null, "val");
    assertTrue(executorData.getStringParams().isEmpty());
  }

  /**
   * Tests setDoubleParam inserts a key–value pair.
   */
  @Test
  void setDoubleParam_ValidArgs_InsertsPair() {
    executorData.setDoubleParam("amount", 42.0);
    assertEquals(42.0, executorData.getDoubleParams().get("amount"));
  }

  /**
   * Tests setDoubleParam with null name performs no insertion.
   */
  @Test
  void setDoubleParam_NullName_NoChange() {
    executorData.setDoubleParam(null, 99.0);
    assertTrue(executorData.getDoubleParams().isEmpty());
  }
}
