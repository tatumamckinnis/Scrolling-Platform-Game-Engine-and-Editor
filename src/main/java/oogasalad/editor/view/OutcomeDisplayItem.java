package oogasalad.editor.view;

import oogasalad.editor.model.data.object.event.ExecutorData;
import java.util.Objects;

/**
 * Represents an item displayed in the outcomes ListView within {@link OutcomesSectionBuilder}.
 * Encapsulates the outcome index within the event's list, the underlying {@link ExecutorData},
 * and formats a display text string.
 */
class OutcomeDisplayItem {
  private final int index;
  private final String displayText;
  private final ExecutorData data;

  /**
   * Constructs an OutcomeDisplayItem.
   * @param index The index of this outcome in the event's outcome list.
   * @param data The non-null ExecutorData associated with this outcome.
   * @throws NullPointerException if data is null.
   */
  OutcomeDisplayItem(int index, ExecutorData data) {
    this.index = index;
    this.data = Objects.requireNonNull(data, "ExecutorData cannot be null for OutcomeDisplayItem");
    this.displayText = String.format("[%d]: %s", index, data.getExecutorName());
  }

  /**
   * Returns the index of this outcome.
   * @return The outcome index.
   */
  int getIndex() {
    return index;
  }

  /**
   * Returns the underlying ExecutorData.
   * @return The ExecutorData.
   */
  ExecutorData getData() {
    return data;
  }

  /**
   * Returns the formatted display text used for the ListView representation.
   * Example: "[0]: MOVE_LEFT"
   * @return The string representation of this item for display.
   */
  @Override
  public String toString() {
    return displayText;
  }

  /**
   * Checks equality based on index and contained ExecutorData.
   * @param o The object to compare against.
   * @return true if the objects represent the same outcome item, false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    OutcomeDisplayItem that = (OutcomeDisplayItem) o;
    return index == that.index && Objects.equals(data, that.data);
  }

  /**
   * Generates a hash code based on index and ExecutorData.
   * @return The hash code.
   */
  @Override
  public int hashCode() {
    return Objects.hash(index, data);
  }
}
