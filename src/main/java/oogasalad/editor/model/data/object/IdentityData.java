package oogasalad.editor.model.data.object;

import java.util.UUID;
import oogasalad.editor.model.data.Layer;

/**
 * Represents the identity data for an editor object. This class holds primary identifying
 * information for an editor object, including a unique identifier, name, group, and the layer to
 * which the object belongs.
 *
 * @author Jacob You
 */
public class IdentityData {

  private UUID id;
  private String name;
  private String group;
  private Layer layer;

  /**
   * Constructs a new IdentityData instance with the specified id, name, group, and layer.
   *
   * @param id    the unique identifier for the editor object
   * @param name  the name of the editor object
   * @param group the group to which the editor object belongs
   * @param layer the layer associated with the editor object
   */
  public IdentityData(UUID id, String name, String group, Layer layer) {
    this.id = id;
    this.name = name;
    this.group = group;
    this.layer = layer;
  }

  /**
   * Returns the unique identifier (UUID) of the editor object.
   *
   * @return the UUID of the editor object
   */
  public UUID getId() {
    return id;
  }

  /**
   * Returns the name of the editor object.
   *
   * @return the name of the editor object
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the group of the editor object.
   *
   * @return the group of the editor object
   */
  public String getGroup() {
    return group;
  }

  /**
   * Returns the layer associated with the editor object.
   *
   * @return the layer of the editor object
   */
  public Layer getLayer() {
    return layer;
  }

  /**
   * Sets the unique identifier (UUID) for the editor object.
   *
   * @param id the new UUID to set
   */
  public void setId(UUID id) {
    this.id = id;
  }

  /**
   * Sets the name of the editor object.
   *
   * @param name the new name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets the group of the editor object.
   *
   * @param group the new group to set
   */
  public void setGroup(String group) {
    this.group = group;
  }

  /**
   * Sets the layer associated with the editor object.
   *
   * @param layer the new layer to set
   */
  public void setLayer(Layer layer) {
    this.layer = layer;
  }
}
