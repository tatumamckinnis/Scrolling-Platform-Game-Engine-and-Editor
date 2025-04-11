package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;

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
 * @param gameName            the name of the game to which the blueprint belongs.
 * @param group               the group category, such as entities, blocks, or backgrounds.
 * @param type                the type/category of the blueprint.
 * @param spriteData          the sprite data associated with the blueprint.
 * @param hitBoxData          the hitbox data associated with the blueprint.
 * @param eventDataList       the list of event data associated with the blueprint.
 * @param stringProperties    a map of custom string properties for additional configuration.
 * @param doubleProperties    a map of custom double properties for additional configuration.
 * @param displayedProperties a list of property names that are intended to be displayed.
 * @author Billy McCune
 */
public record BlueprintData(
    int blueprintId,
    double velocityX,
    double velocityY,
    double rotation,
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

}
