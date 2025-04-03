package oogasalad.editor.model.data.object;

import java.util.UUID;
import oogasalad.editor.model.data.Layer;

public class IdentityData {
  private UUID id;
  private String name;
  private String group;
  private Layer layer;

  public IdentityData(UUID id, String name, String group, Layer layer) {
    this.id = id;
    this.name = name;
    this.group = group;
    this.layer = layer;
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

  public Layer getLayer() {
    return layer;
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

  public void setLayer(Layer layer) {
    this.layer = layer;
  }
}
