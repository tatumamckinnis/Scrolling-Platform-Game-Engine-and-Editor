package oogasalad.engine.model.event;

import oogasalad.engine.model.object.GameObject;

public interface EventAPI {

  public void executeEventChain(String chainID, GameObject target);
}
