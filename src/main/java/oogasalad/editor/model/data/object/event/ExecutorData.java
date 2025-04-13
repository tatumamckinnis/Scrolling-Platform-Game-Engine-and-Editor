package oogasalad.editor.model.data.object.event;

import java.util.Map;

/**
 * A class representing a singular exeuctor (condition/outcome) for an event. Holds the name of the
 * executor type and string and double parameters.
 */
public class ExecutorData {

  private String executor;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;

  public ExecutorData(String outcome, Map<String, String> stringParams,
      Map<String, Double> doubleParams) {
    this.executor = outcome;
    this.stringParams = stringParams;
    this.doubleParams = doubleParams;
  }

  public String getExecutorName() {
    return executor;
  }

  public Map<String, String> getStringParams() {
    return stringParams;
  }

  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  public void setStringParam(String paramName, String paramValue) {
    if (paramValue == null) {
      return;
    }
    if (stringParams.containsKey(paramName)) {
      stringParams.put(paramName, paramValue);
    }
  }

  public void setDoubleParam(String paramName, Double paramValue) {
    if (doubleParams.containsKey(paramName)) {
      doubleParams.put(paramName, paramValue);
    }
  }
}
