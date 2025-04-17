package oogasalad.editor.view;
@FunctionalInterface
interface ParameterUpdateHandler {

  void update(String paramName, String newValueText);
}
