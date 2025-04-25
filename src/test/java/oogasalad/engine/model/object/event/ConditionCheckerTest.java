/**
 * Tests conditioner checking functionality
 */
package oogasalad.engine.model.object.event;


import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.scene.input.KeyCode;
import net.bytebuddy.build.ToStringPlugin.Enhance;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.event.ConditionChecker;
import oogasalad.engine.model.event.DefaultCollisionHandler;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.event.condition.Condition;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.condition.EventCondition.ConditionType;
import oogasalad.engine.model.object.Entity;
import oogasalad.engine.model.object.GameObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testing expected return values for conditions using a condition checker
 *
 * @author Gage Garcia
 */
public class ConditionCheckerTest {

  private ConditionChecker checker;
  private GameObject obj;

  public class mockInput implements InputProvider {

    @Override
    public boolean isKeyPressed(KeyCode keyCode) {
      return true;
    }

    @Override
    public boolean isKeyReleased(KeyCode keyCode) {
      return false;
    }

    @Override
    public Point getMousePosition() {
      return null;
    }

    @Override
    public void clearReleased() {

    }

  }

  public class MockCollision implements CollisionHandler {

    @Override
    public void updateCollisions() {

    }

    @Override
    public List<GameObject> getCollisions(GameObject gameObject) {
      return new ArrayList<>();
    }
  }

  @BeforeEach
  void setUp() throws Exception {
    checker = new ConditionChecker(new mockInput(), new MockCollision());

  }

  @Test
  void TrueCondition() {
    EventCondition ec = new EventCondition(ConditionType.TRUE, null, null);
    assertEquals(checker.checkCondition(ec, null), true);
  }

  @Test
  void InputCondition() {
    EventCondition ec = new EventCondition(ConditionType.KEY_PRESSED, null, null);
    assertEquals(checker.checkCondition(ec, null), true);
  }

  @Test
  void CollisionCondition() {
    EventCondition ec = new EventCondition(ConditionType.COLLIDED_WITH_GROUP, null, null);
    assertFalse(checker.checkCondition(ec, obj));
  }

  @Test
  void VariableThresholdCondition() {
    HashMap<String, String> stringParams = new HashMap<>();
    HashMap<String, Double> doubleParams = new HashMap<>();
    stringParams.put("variable", "score");
    doubleParams.put("score", 8.0);
    doubleParams.put("threshold", 10.0);
    EventCondition ec = new EventCondition(ConditionType.LESS_THAN_VARIABLE_THRESHOLD, stringParams, doubleParams);


    Entity gameObject = new Entity(null, null, 0, 0, 0, null, null, null, stringParams,
        doubleParams);
    assertTrue(checker.checkCondition(ec, gameObject));
  }

  @Test
  void VariableThresholdConditionFail() {
    HashMap<String, String> stringParams = new HashMap<>();
    HashMap<String, Double> doubleParams = new HashMap<>();
    stringParams.put("variable", "score");
    doubleParams.put("score", 15.0);
    doubleParams.put("threshold", 10.0);
    EventCondition ec = new EventCondition(ConditionType.LESS_THAN_VARIABLE_THRESHOLD, stringParams, doubleParams);


    Entity gameObject = new Entity(null, null, 0, 0, 0, null, null, null, stringParams,
        doubleParams);
    assertFalse(checker.checkCondition(ec, gameObject));
  }


}
