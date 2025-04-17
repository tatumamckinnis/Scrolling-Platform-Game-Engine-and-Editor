package oogasalad.editor.view;

/**
 * Functional interface for handling the removal of an existing condition within the editor view,
 * typically used by {@link ConditionsSectionBuilder}.
 *
 * @author Tatum McKinnis
 */
@FunctionalInterface
public interface RemoveConditionHandler {

  /**
   * Handles the request to remove a condition.
   *
   * @param groupIndex     The index of the condition group from which to remove.
   * @param conditionIndex The index of the condition within the specified group to remove.
   */
  void handle(int groupIndex, int conditionIndex);
}
