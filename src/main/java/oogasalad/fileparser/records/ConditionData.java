package oogasalad.fileparser.records;

import java.util.Map;

/**
 * Represents the condition data parsed from an input source.
 * <p>
 * This record encapsulates the condition name along with maps for string and double properties.
 * </p>
 *
 * @param name the condition's name.
 * @param stringProperties a map of string properties associated with the condition.
 * @param doubleProperties a map of double properties associated with the condition.
 *
 * @author Billy McCune
 */
public record ConditionData(String name,
                            Map<String, String> stringProperties,
                            Map<String, Double> doubleProperties) {

  /**
   * Returns the type/name of this condition.
   * This is an alias for the record's name component.
   *
   * @return the condition type as a string
   */
  public String conditionType() {
    return name;
  }

  /**
   * Returns the map of string parameters for this condition.
   * This is an alias for the record's stringProperties component.
   *
   * @return a map of string parameters
   */
  public Map<String, String> stringParams() {
    return stringProperties;
  }

  /**
   * Returns the map of numeric parameters for this condition.
   * This is an alias for the record's doubleProperties component.
   *
   * @return a map of double parameters
   */
  public Map<String, Double> doubleParams() {
    return doubleProperties;
  }
}
