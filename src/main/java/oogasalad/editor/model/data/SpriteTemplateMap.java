package oogasalad.editor.model.data;

import java.util.HashMap;
import java.util.Map;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;

/**
 * Manages a collection of {@link SpriteTemplate}s available for editor objects.
 * <p>
 * Templates are stored in a map keyed by their name, allowing lookup, addition,
 * and removal of sprite templates.
 * </p>
 *
 * @author Jacob You
 */
public class SpriteTemplateMap {

  private Map<String, SpriteTemplate> spriteMap;

  /**
   * Constructs an empty SpriteTemplateMap.
   */
  public SpriteTemplateMap() {
    spriteMap = new HashMap<>();
  }

  /**
   * Adds a sprite template to the map.
   * <p>
   * The template is stored under its name; if a template with the same name
   * already exists, it will be overwritten.
   * </p>
   *
   * @param spriteTemplate the {@link SpriteTemplate} to add
   */
  public void addSpriteTemplate(SpriteTemplate spriteTemplate) {
    spriteMap.put(spriteTemplate.getName(), spriteTemplate);
  }

  /**
   * Retrieves the sprite template associated with the given name.
   *
   * @param name the name of the sprite template to retrieve
   * @return the corresponding {@link SpriteTemplate}, or {@code null} if none is found
   */
  public SpriteTemplate getSpriteData(String name) {
    return spriteMap.get(name);
  }

  /**
   * Returns the internal map of sprite templates.
   * <p>
   * Modifications to the returned map will affect this SpriteTemplateMap.
   * </p>
   *
   * @return the map of template names to {@link SpriteTemplate} instances
   */
  public Map<String, SpriteTemplate> getSpriteMap() {
    return spriteMap;
  }

  /**
   * Replaces the internal map of sprite templates with the provided map.
   *
   * @param spriteMap a map of names to {@link SpriteTemplate} instances
   */
  public void setSpriteMap(Map<String, SpriteTemplate> spriteMap) {
    this.spriteMap = spriteMap;
  }

  /**
   * Removes the sprite template with the given name from the map.
   * <p>
   * If no template exists under that name, this method has no effect.
   * </p>
   *
   * @param name the name of the sprite template to remove
   */
  public void removeSpriteData(String name) {
    spriteMap.remove(name);
  }
}
