package oogasalad.editor.model.data;

/**
 * Represents a rendering layer in the editor. Each layer has a name and a priority, where higher
 * priority layers are rendered on top of lower priority ones. Additionally, a layer maintains a
 * list of layers it interacts with, including itself by default.
 *
 * @author Jacob You
 */
public class Layer {

  private String name;
  private int priority; // Higher priority -> Rendered on top of other layers

  /**
   * Constructs a new Layer with the specified name and priority. The interacting layers list is
   * automatically initialized with this layer included.
   *
   * @param name     the name of the layer
   * @param priority the rendering priority of the layer
   */
  public Layer(String name, int priority) {
    this.name = name;
    this.priority = priority;
  }

  /**
   * Retrieves the name of this layer.
   *
   * @return the name of the layer
   */
  public String getName() {
    return name;
  }

  /**
   * Sets a new name for this layer.
   *
   * @param name the new name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Retrieves the rendering priority of this layer.
   *
   * @return the priority of the layer
   */
  public int getPriority() {
    return priority;
  }

  /**
   * Sets a new rendering priority for this layer.
   *
   * @param priority the new priority to set
   */
  public void setPriority(int priority) {
    this.priority = priority;
  }
}