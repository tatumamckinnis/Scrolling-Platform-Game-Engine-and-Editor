package oogasalad.editor.view;

/**
 * Functional interface for handling the addition of a new condition within the editor view,
 * typically used by {@link ConditionsSectionBuilder}.
 *
 * @author Tatum McKinnis
 */
@FunctionalInterface
public interface AddConditionHandler {

  /**
   * Handles the request to add a condition.
   *
   * @param groupIndex    The index of the condition group to add the condition to.
   * @param conditionType The string identifier of the condition type being added.
   */
  void handle(int groupIndex, String conditionType);
}
