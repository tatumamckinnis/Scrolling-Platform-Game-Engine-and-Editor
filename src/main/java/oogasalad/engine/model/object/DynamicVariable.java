package oogasalad.engine.model.object;

/**
 * Represents a dynamic variable used to define customizable properties of a game object.
 *
 * <p>This abstract class serves as the base for different types of dynamic variables (e.g.,
 * numeric, boolean, string). Variables are expected to be castable to specific types due to the
 * controlled nature of the editor.
 *
 * @author Alana Zinkin
 */
public abstract class DynamicVariable {

  /**
   * The name/key of the variable
   */
  private String name;

  /**
   * The data type of the variable (e.g., "int", "double", "boolean", "String")
   */
  private String type;

  /**
   * The string representation of the variable's value
   */
  private String value;

  /**
   * A short description of what this variable represents
   */
  private String description;
}
