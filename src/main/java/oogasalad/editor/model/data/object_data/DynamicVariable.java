package oogasalad.editor.model.data.object_data;

public class DynamicVariable {
  private String name;
  private String type; // Supported types: "int", "double", "boolean", "string"
  private String value;
  private String description;

  public DynamicVariable(String name, String type, String inputValue, String description) {
    this.name = name;
    this.type = type.toLowerCase();
    this.description = description;
    this.value = inputValue;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String inputValue) {
    this.value = inputValue;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return name + " = " + value + " (" + type + ")";
  }
}