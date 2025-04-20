/**
 * Tests conditioner checking functionality
 */
package oogasalad.engine.model.object.event;



import java.util.ArrayList;
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
import oogasalad.engine.model.object.GameObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConditionCheckerTest {
    private InputProvider inputProvider;
    private DefaultCollisionHandler defaultCollisionHandler;
    private ConditionChecker checker;
    private GameControllerAPI gameController;
    private GameObject obj;

    public class mockInput implements InputProvider {
        @Override
        public boolean isKeyPressed(KeyCode keyCode) {
            return false;
        }

        @Override
        public boolean isKeyReleased(KeyCode keyCode) {
            return false;
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

        assertEquals(checker.checkCondition(ConditionType.TRUE, null), true);

    }




}
