/**
 * Tests conditioner checking functionality
 */
package oogasalad.engine.event;



import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.api.InputProvider;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.model.event.ConditionChecker;
import oogasalad.engine.model.event.DefaultCollisionHandler;
import oogasalad.engine.model.object.GameObject;
import org.junit.jupiter.api.BeforeEach;

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

    }

    @BeforeEach
    void setUp() throws Exception {

    }




}
