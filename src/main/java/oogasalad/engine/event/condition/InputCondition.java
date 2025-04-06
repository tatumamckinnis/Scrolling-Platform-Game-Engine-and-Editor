package oogasalad.engine.event.condition;

import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.InputProvider;
import oogasalad.engine.model.object.GameObject;

public class InputCondition implements Condition {
    private final InputProvider inputProvider;
    private final KeyCode keyCode;

    public InputCondition(InputProvider inputProvider, KeyCode keyCode) {
        this.inputProvider = inputProvider;
        this.keyCode = keyCode;
    }

    @Override
    public boolean isMet(GameObject gameObject) {
        return inputProvider.isKeyPressed(keyCode);
    }
}
