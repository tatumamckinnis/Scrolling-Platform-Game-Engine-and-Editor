package oogasalad.editor.model.data.object;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DynamicVariableContainer {
  private Map<String, DynamicVariable> variables = new HashMap<>();

  public void addVariable(DynamicVariable var) {
    variables.put(var.getName(), var);
  }

  public DynamicVariable getVariable(String name) {
    return variables.get(name);
  }

  public Collection<DynamicVariable> getAllVariables() {
    return Collections.unmodifiableCollection(variables.values());
  }
}
