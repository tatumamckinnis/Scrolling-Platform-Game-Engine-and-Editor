package oogasalad.editor.model.data.object.event;

import java.util.Map;

/**
 * Represents a single executor for an event, which can be either a condition or an outcome. This
 * class stores the type of the executor (as a name), along with its associated parameters which may
 * be string-based or double-based.
 *
 * <p>Executors are used in the editor to define event logic by specifying what actions or
 * checks should occur when an event is triggered.</p>
 *
 * @author Jacob You
 */
public class ExecutorData {

  private String executor;
  private Map<String, String> stringParams;
  private Map<String, Double> doubleParams;

  /**
   * Constructs an ExecutorData object with the given executor name and parameters.
   *
   * @param outcome      the name of the executor (e.g., "Move", "Spawn", "HealthCheck")
   * @param stringParams a map of string-based parameter names to values
   * @param doubleParams a map of numeric parameter names to values
   */
  public ExecutorData(String outcome, Map<String, String> stringParams,
      Map<String, Double> doubleParams) {
    this.executor = outcome;
    this.stringParams = stringParams;
    this.doubleParams = doubleParams;
  }

  /**
   * Returns the name/type of the executor.
   *
   * @return the executor name
   */
  public String getExecutorName() {
    return executor;
  }

  /**
   * Returns the map of string parameters for the executor.
   *
   * @return map of string parameter names to values
   */
  public Map<String, String> getStringParams() {
    return stringParams;
  }

  /**
   * Returns the map of double (numeric) parameters for the executor.
   *
   * @return map of double parameter names to values
   */
  public Map<String, Double> getDoubleParams() {
    return doubleParams;
  }

  /**
   * Sets or updates a string parameter for the executor. If either the parameter name or value is
   * null, the method does nothing.
   *
   * @param paramName  the name of the parameter
   * @param paramValue the new value to associate with the parameter
   */
  public void setStringParam(String paramName, String paramValue) {
    if (paramValue == null || paramName == null) {
      return;
    }
    stringParams.put(paramName, paramValue);
  }

  /**
   * Sets or updates a double (numeric) parameter for the executor. If the parameter name is null,
   * the method does nothing.
   *
   * @param paramName  the name of the parameter
   * @param paramValue the new value to associate with the parameter
   */
  public void setDoubleParam(String paramName, Double paramValue) {
    if (paramName == null) {
      return;
    }
    doubleParams.put(paramName, paramValue);
  }
}
