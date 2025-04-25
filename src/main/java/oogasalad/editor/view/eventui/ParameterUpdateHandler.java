package oogasalad.editor.view.eventui;
@FunctionalInterface
interface ParameterUpdateHandler {

  void update(String paramName, String newValueText);
}
