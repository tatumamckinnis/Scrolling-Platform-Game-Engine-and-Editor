package oogasalad.fileparser.records;

import java.util.Map;

/**
 * Represents the outcome data parsed from an input source.
 * <p>
 * This record encapsulates the outcome's name along with maps for string and double properties
 * that provide additional configuration for the outcome.
 * </p>
 *
 * @param name             the name of the outcome.
 * @param stringProperties a map of custom string properties associated with the outcome.
 * @param doubleProperties a map of custom double properties associated with the outcome.
 *
 * @author Billy
 */
public record OutcomeData(String name,
                          Map<String, String> stringProperties,
                          Map<String, Double> doubleProperties) {

  /**
   * Returns the type/name of this outcome.
   * This is an alias for the record's name component.
   *
   * @return the outcome type as a string
   */
  public String outcomeType() {
    return name;
  }

  /**
   * Returns the map of string parameters for this outcome.
   * This is an alias for the record's stringProperties component.
   *
   * @return a map of string parameters
   */
  public Map<String, String> stringParams() {
    return stringProperties;
  }

  /**
   * Returns the map of numeric parameters for this outcome.
   * This is an alias for the record's doubleProperties component.
   *
   * @return a map of double parameters
   */
  public Map<String, Double> doubleParams() {
    return doubleProperties;
  }
}
