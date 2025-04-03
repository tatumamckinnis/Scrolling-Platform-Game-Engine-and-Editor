package oogasalad.editor.model.data.object_data;

import java.util.UUID;

public class IdentityData {
  private UUID id;
  private String name;
  private String group;

  public IdentityData(UUID id, String name, String group) {
    this.id = id;
    this.name = name;
    this.group = group;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getGroup() {
    return group;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setGroup(String group) {
    this.group = group;
  }
}
