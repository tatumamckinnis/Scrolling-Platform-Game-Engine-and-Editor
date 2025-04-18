package oogasalad.editor.model.data;

import java.util.HashMap;
import java.util.Map;
import oogasalad.editor.model.data.object.sprite.SpriteData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;

/**
 * Holds a list of all of the currently available sprites for the current object.
 *
 * @author Jacob You
 */
public class SpriteTemplateMap {

  Map<String, SpriteTemplate> spriteMap;

  public SpriteTemplateMap() {
    spriteMap = new HashMap<>();
  }

  public void addSpriteTemplate(SpriteTemplate spriteData) {
    spriteMap.put(spriteData.getName(), spriteData);
  }

  public SpriteTemplate getSpriteData(String name) {
    return spriteMap.get(name);
  }

  public Map<String, SpriteTemplate> getSpriteMap() {
    return spriteMap;
  }

  public void setSpriteMap(Map<String, SpriteTemplate> spriteMap) {
    this.spriteMap = spriteMap;
  }

  public void removeSpriteData(String name) {
    spriteMap.remove(name);
  }
}
