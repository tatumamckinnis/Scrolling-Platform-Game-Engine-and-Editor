/**
 * Tests conditioner checking functionality
 */
package oogasalad.engine.event;



import javafx.scene.input.KeyCode;
import oogasalad.engine.controller.DefaultGameController;
import oogasalad.engine.controller.DefaultGameManager;
import oogasalad.engine.controller.InputProvider;
import oogasalad.engine.controller.api.GameControllerAPI;
import oogasalad.engine.event.condition.Condition;
import oogasalad.engine.event.condition.EventCondition;
import oogasalad.engine.event.outcome.EventOutcome;
import oogasalad.engine.model.object.DefaultGameObject;
import oogasalad.engine.model.object.GameObject;
import oogasalad.fileparser.records.AnimationData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConditionCheckerTest {
    private InputProvider inputProvider;
    private CollisionHandler collisionHandler;
    private ConditionChecker checker;
    private GameControllerAPI gameController;
    private GameObject obj;

    public class mockInput implements InputProvider {
        @Override
        public boolean isKeyPressed(KeyCode keyCode) {
            return true;
        }

    }

    @BeforeEach
    void setUp() throws Exception {
        inputProvider = new mockInput();
        gameController = new DefaultGameController(inputProvider);
        collisionHandler = new CollisionHandler(gameController);
        checker = new ConditionChecker(inputProvider, collisionHandler);
        UUID uuid = UUID.randomUUID();
        int blueprintID = 101;
        String type = "Player";
        int hitBoxX = 50, hitBoxY = 100, hitBoxWidth = 20, hitBoxHeight = 40;
        int layer = 1;
        String name = "Hero";
        String group = "Characters";

        // Mock Data for Dependencies
        SpriteData spriteData = null;
        FrameData currentFrame = null;
        Map<String, FrameData> frameMap = new HashMap<>();
        Map<String, AnimationData> animationMap = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        List<Event> events = new ArrayList<>();
        HitBoxData hitBoxData = null;

        // Create Mock GameObject
        obj = new DefaultGameObject(uuid, blueprintID, type, hitBoxX, hitBoxY, hitBoxWidth, hitBoxHeight,
                layer, name, group, spriteData, currentFrame, frameMap, animationMap, params, events, hitBoxData);
    }

    @Test
    void checkTrue() {
        boolean res = checker.checkCondition(EventCondition.ConditionType.TRUE, obj);
        assertEquals(res, true);
    }


}
