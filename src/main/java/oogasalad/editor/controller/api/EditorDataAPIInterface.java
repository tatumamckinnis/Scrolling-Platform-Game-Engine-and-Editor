package oogasalad.editor.controller.api;

import oogasalad.editor.controller.CollisionDataAPI;
import oogasalad.editor.controller.HitboxDataAPI;
import oogasalad.editor.controller.IdentityDataAPI;
import oogasalad.editor.controller.InputDataAPI;
import oogasalad.editor.controller.PhysicsDataAPI;
import oogasalad.editor.controller.SpriteDataAPI;

public interface EditorDataAPIInterface {
  public IdentityDataAPI getIdentityDataAPI();
  public HitboxDataAPI getHitboxDataAPI();
  public InputDataAPI getInputDataAPI();
  public PhysicsDataAPI getPhysicsDataAPI();
  public CollisionDataAPI getCollisionDataAPI();
  public SpriteDataAPI getSpriteDataAPI();
}
