package oogasalad.editor.model.data;

import java.util.ArrayList;
import java.util.List;

public class Layer {
  private String name;
  private List<Layer> interactingLayers;
  private int priority; // Higher priority -> Rendered on top of other layers

  public Layer(String name, int priority) {
    this.name = name;
    this.interactingLayers = new ArrayList<>();
    interactingLayers.add(this);
    this.priority = 0;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Layer> getInteractingLayers() {
    return interactingLayers;
  }

  public void addInteractingLayer(Layer layer) {
    this.interactingLayers.add(layer);
  }

  public void removeInteractingLayer(Layer layer) {
    if (layer != this) { // Prevent removing self
      this.interactingLayers.remove(layer); // Should not error even if layer does not exist
    }
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }
}
