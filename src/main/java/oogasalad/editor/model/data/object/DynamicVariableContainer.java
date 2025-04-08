package oogasalad.editor.model.data.object;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A container for managing dynamic variables used in the editor. This class provides methods to add
 * and retrieve {@link DynamicVariable} objects by their name, as well as retrieving an unmodifiable
 * collection of all stored variables.
 */
public class DynamicVariableContainer {

  private Map<String, DynamicVariable> variables = new HashMap<>();

  /**
   * Adds a new dynamic variable to the container.
   *
   * @param var the {@link DynamicVariable} to add
   */
  public void addVariable(DynamicVariable var) {
    variables.put(var.getName(), var);
  }

  /**
   * Retrieves the dynamic variable with the specified name.
   *
   * @param name the name of the variable to retrieve
   * @return the corresponding {@link DynamicVariable} if found, otherwise returns null
   */
  public DynamicVariable getVariable(String name) {
    return variables.get(name);
  }

  /**
   * Returns an unmodifiable collection of all dynamic variables contained in this container.
   *
   * @return an unmodifiable collection of {@link DynamicVariable} objects
   */
  public Collection<DynamicVariable> getAllVariables() {
    return Collections.unmodifiableCollection(variables.values());
  }
}
