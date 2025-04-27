package oogasalad.editor.view.eventui;

/**
 * Functional interface for handling updates to a parameter's value.
 *
 * <p>Implementations of this interface define how to update a parameter
 * when its name and new value (as text) are provided, typically from a UI input field.</p>
 *
 * @author Tatum McKinnis
 */
@FunctionalInterface
interface ParameterUpdateHandler {

  /**
   * Updates the parameter with the given name to the specified new value.
   *
   * @param paramName    the name of the parameter to update
   * @param newValueText the new value for the parameter as a string
   */
  void update(String paramName, String newValueText);
}

