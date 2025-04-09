package oogasalad.editor.model.data.object;

/**
 * Represents a dynamic variable used in the editor, encapsulating its name, type, value, and
 * description.
 *
 * @author Jacob You
 */
public class DynamicVariable {

  private String name;
  private String type;
  private String value;
  private String description;

  /**
   * Constructs a new DynamicVariable with the specified name, type, initial value, and
   * description.
   *
   * @param name        the name of the variable
   * @param type        the type of the variable
   * @param inputValue  the initial value of the variable as a String
   * @param description a description of the variable
   */
  public DynamicVariable(String name, String type, String inputValue, String description) {
    this.name = name;
    this.type = type.toLowerCase();
    this.description = description;
    this.value = inputValue;
  }

  /**
   * Returns the name of this dynamic variable.
   *
   * @return the variable's name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the type of this dynamic variable.
   *
   * @return the variable's type
   */
  public String getType() {
    return type;
  }

  /**
   * Returns the current value of this dynamic variable.
   *
   * @return the variable's value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets a new value for this dynamic variable.
   *
   * @param inputValue the new value to be set
   */
  public void setValue(String inputValue) {
    this.value = inputValue;
  }

  /**
   * Returns the description of this dynamic variable.
   *
   * @return the variable's description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets a new description for this dynamic variable.
   *
   * @param description the new description to be set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Returns a string representation of the dynamic variable in the format: "name = value (type)".
   *
   * @return a string representation of this dynamic variable
   */
  @Override
  public String toString() {
    return name + " = " + value + " (" + type + ")";
  }
}