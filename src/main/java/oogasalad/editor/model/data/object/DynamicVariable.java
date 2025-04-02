package old_editor_example;

public class DynamicVariable {
  private String name;
  private String type; // Supported types: "int", "double", "boolean", "string"
  private Object value;
  private String description;

  public DynamicVariable(String name, String type, String inputValue, String description) {
    this.name = name;
    this.type = type.toLowerCase();
    this.description = description;
    this.value = parseValue(inputValue);
  }

  private Object parseValue(String inputValue) {
    switch(type) {
      case "int":
        try {
          return Integer.parseInt(inputValue);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid integer: " + inputValue);
        }
      case "double":
        try {
          return Double.parseDouble(inputValue);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid double: " + inputValue);
        }
      case "boolean":
        if(inputValue.equalsIgnoreCase("true") || inputValue.equalsIgnoreCase("false")){
          return Boolean.parseBoolean(inputValue);
        } else {
          throw new IllegalArgumentException("Invalid boolean: " + inputValue);
        }
      case "string":
        return inputValue;
      default:
        throw new IllegalArgumentException("Unsupported type: " + type);
    }
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(String inputValue) {
    this.value = parseValue(inputValue);
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