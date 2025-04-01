package oogasalad.editor.controller.api;

import java.util.UUID;

public interface IdentityDataAPIInterface {
  public String getName(UUID id);
  public String getGroup(UUID id);

  public void setName(UUID id, String name);
  public void setGroup(UUID id, String group);
}
