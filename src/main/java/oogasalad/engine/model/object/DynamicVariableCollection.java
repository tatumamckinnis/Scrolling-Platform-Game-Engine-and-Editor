package oogasalad.engine.model.object;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * DynamicVariableContainer
 * <p>
 * A unified container that holds both predefined and user-added dynamic properties. Each property
 * is mapped from a String key to a DynamicVariable object.
 * <p>
 * Use Cases: - Store health, score, power-up status, timers, etc. - Allow dynamic addition of new
 * gameplay variables during runtime - Retrieve or update variables using key-based access
 */
public class DynamicVariableCollection {

  // Core mapping from variable name → dynamic variable
  private final Map<String, DynamicVariable> myVariables;

  /**
   * Constructs a new, empty container for dynamic variables.
   */
  public DynamicVariableCollection() {
    myVariables = new HashMap<>();
  }

  /**
   * Adds or overrides a variable in the container.
   *
   * @param key      the name of the variable
   * @param variable the DynamicVariable to associate with the key
   */
  public void put(String key, DynamicVariable variable) {
    myVariables.put(key, variable);
  }

  /**
   * Retrieves a variable by its key.
   *
   * @param key the variable name
   * @return the DynamicVariable object, or null if not found
   */
  public DynamicVariable get(String key) {
    return myVariables.get(key);
  }

  /**
   * Checks if a variable with the given key exists.
   *
   * @param key the variable name
   * @return true if the key exists, false otherwise
   */
  public boolean contains(String key) {
    return myVariables.containsKey(key);
  }

  /**
   * Removes a variable from the container.
   *
   * @param key the variable name
   * @return the removed variable, or null if it wasn't present
   */
  public DynamicVariable remove(String key) {
    return myVariables.remove(key);
  }

  /**
   * Returns an unmodifiable view of all key-variable mappings.
   *
   * @return an unmodifiable map of current variables
   */
  public Map<String, DynamicVariable> getAllVariables() {
    return Collections.unmodifiableMap(myVariables);
  }

}

