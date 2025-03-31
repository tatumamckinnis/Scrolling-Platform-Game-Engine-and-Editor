package oogasalad.engine.model.event;

public abstract class ParameterDefinition {
  private String name;
  private String type;
  private boolean dynamic;
  private final String value;

  protected ParameterDefinition(String value) {
    this.value = value;
  }
}
