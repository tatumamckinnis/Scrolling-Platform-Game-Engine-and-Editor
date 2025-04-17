package oogasalad.editor.view;

import java.util.Objects;
import oogasalad.editor.model.data.object.event.ExecutorData;

/**
 * Represents an item displayed in the conditions ListView within {@link ConditionsSectionBuilder}.
 * Encapsulates the group index, condition index within the group, the underlying
 * {@link ExecutorData}, and formats a display text string.
 *
 * @author Tatum McKinnis
 */
class ConditionDisplayItem {

  private final int groupIndex;
  private final int conditionIndex;
  private final String displayText;
  private final ExecutorData data;

  /**
   * Constructs a ConditionDisplayItem.
   *
   * @param groupIndex     The index of the group this condition belongs to.
   * @param conditionIndex The index of this condition within its group.
   * @param data           The non-null ExecutorData associated with this condition.
   * @throws NullPointerException if data is null.
   */
  ConditionDisplayItem(int groupIndex, int conditionIndex, ExecutorData data) {
    this.groupIndex = groupIndex;
    this.conditionIndex = conditionIndex;
    this.data = Objects.requireNonNull(data,
        "ExecutorData cannot be null for ConditionDisplayItem");
    this.displayText = String.format("Group %d [%d]: %s", groupIndex, conditionIndex,
        data.getExecutorName());
  }

  /**
   * Returns the group index of this condition.
   *
   * @return The group index.
   */
  int getGroupIndex() {
    return groupIndex;
  }

  /**
   * Returns the index of this condition within its group.
   *
   * @return The condition index.
   */
  int getConditionIndex() {
    return conditionIndex;
  }

  /**
   * Returns the underlying ExecutorData for this condition.
   *
   * @return The ExecutorData.
   */
  ExecutorData getData() {
    return data;
  }

  /**
   * Returns the formatted display text used for the ListView representation. Example: "Group 0 [1]:
   * KEY_PRESSED"
   *
   * @return The string representation of this item for display.
   */
  @Override
  public String toString() {
    return displayText;
  }

  /**
   * Checks equality based on group index, condition index, and contained ExecutorData.
   *
   * @param o The object to compare against.
   * @return true if the objects represent the same condition item, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConditionDisplayItem that = (ConditionDisplayItem) o;
    return groupIndex == that.groupIndex &&
        conditionIndex == that.conditionIndex &&
        Objects.equals(data, that.data);
  }

  /**
   * Generates a hash code based on group index, condition index, and ExecutorData.
   *
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return Objects.hash(groupIndex, conditionIndex, data);
  }
}
