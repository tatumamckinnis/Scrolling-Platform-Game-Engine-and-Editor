package old_editor_example;

import java.util.UUID;

public class IdentityData {
  private final UUID id;  // Generated on startup and not editable
  private String name;    // Editable
  private String group;   // Editable

  public IdentityData(String name, String group) {
    this.id = UUID.randomUUID();
    this.name = name;
    this.group = group;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }
}
