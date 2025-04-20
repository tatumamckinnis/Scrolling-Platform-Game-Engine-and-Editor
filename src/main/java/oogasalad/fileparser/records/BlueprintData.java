package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the blueprint data used to initialize game objects.
 * <p>
 * This record encapsulates various properties of a blueprint including its identifier, velocity,
 * game-related categorizations (game name, group, and type), associated sprite and hitbox data,
 * events, and other custom string and double properties as well as the properties to be displayed.
 * </p>
 *
 * @param blueprintId         the unique identifier for the blueprint.
 * @param velocityX           the horizontal velocity associated with the blueprint.
 * @param velocityY           the vertical velocity associated with the blueprint.
 * @param rotation            the rotation of the object (in degrees).
 * @param isFlipped           whether the object should be flipped about its center.
 * @param gameName            the name of the game to which the blueprint belongs.
 * @param type                the type/category of the blueprint.
 * @param spriteData          the sprite data associated with the blueprint.
 * @param hitBoxData          the hitbox data associated with the blueprint.
 * @param eventDataList       the list of event data associated with the blueprint.
 * @param stringProperties    a map of custom string properties for additional configuration.
 * @param doubleProperties    a map of custom double properties for additional configuration.
 * @param displayedProperties a list of property names that are intended to be displayed.
 * @author Billy McCune, Jacob You
 */
public record BlueprintData(
    int blueprintId,
    double velocityX,
    double velocityY,
    double rotation,
    boolean isFlipped,
    String gameName,
    String group, //entities, blocks, backgrounds
    String type,
    SpriteData spriteData,
    HitBoxData hitBoxData,
    List<EventData> eventDataList,
    Map<String, String> stringProperties,
    Map<String, Double> doubleProperties,
    List<String> displayedProperties
) {

  /**
   * Implements custom equality logic that does not include blueprintID. Used for checking and
   * saving only unique blueprints.
   *
   * @param o the reference object with which to compare.
   * @return whether the objects are equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BlueprintData other)) {
      return false;
    }

    return Double.compare(other.velocityX, velocityX) == 0
        && Double.compare(other.velocityY, velocityY) == 0
        && Double.compare(other.rotation, rotation) == 0
        && Objects.equals(gameName, other.gameName)
        && Objects.equals(type, other.type)
        && Objects.equals(spriteData, other.spriteData)
        && Objects.equals(hitBoxData, other.hitBoxData)
        && Objects.equals(eventDataList, other.eventDataList)
        && Objects.equals(stringProperties, other.stringProperties)
        && Objects.equals(doubleProperties, other.doubleProperties)
        && Objects.equals(displayedProperties, other.displayedProperties);
  }

  /**
   * Implements custom hashing logic that does not include blueprintID. Used for checking and saving
   * only unique blueprints.
   *
   * @return the hashCode of the blueprint, excluding the ID.
   */
  @Override
  public int hashCode() {
    return Objects.hash(
        velocityX,
        velocityY,
        rotation,
        gameName,
        type,
        spriteData,
        hitBoxData,
        eventDataList,
        stringProperties,
        doubleProperties,
        displayedProperties
    );
  }

  public BlueprintData withId(int newId) {
    return new BlueprintData(
        newId,
        velocityX, velocityY, rotation, isFlipped,
        gameName, group, type,
        spriteData, hitBoxData, eventDataList,
        stringProperties, doubleProperties, displayedProperties
    );
  }
}
