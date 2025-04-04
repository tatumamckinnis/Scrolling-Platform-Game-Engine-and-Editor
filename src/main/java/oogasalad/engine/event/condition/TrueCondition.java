package oogasalad.engine.event.condition;

import oogasalad.engine.model.object.GameObject;

public class TrueCondition implements Condition {

    @Override
    public boolean isMet(GameObject gameObject) {
        return true;
    }
}