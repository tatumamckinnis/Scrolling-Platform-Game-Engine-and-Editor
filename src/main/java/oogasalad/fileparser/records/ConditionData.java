
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

  public String conditionType() {
    return name;
  }

  public Map<String, String> stringParams() {
    return stringProperties;
  }

  public Map<String, Double> doubleParams() {
    return doubleProperties;
  }
}
