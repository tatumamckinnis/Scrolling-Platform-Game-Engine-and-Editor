package oogasalad.engine.model.object.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;
import oogasalad.engine.controller.api.GameExecutor;
import oogasalad.engine.model.animation.AnimationHandlerApi;
import oogasalad.engine.model.event.CollisionHandler;
import oogasalad.engine.model.event.ConditionChecker;
import oogasalad.engine.model.event.OutcomeExecutor;
import oogasalad.engine.model.event.condition.EventCondition.ConditionType;
import oogasalad.engine.model.event.outcome.EventOutcome;
import oogasalad.engine.model.event.outcome.EventOutcome.OutcomeType;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.Player;
import oogasalad.engine.model.object.event.ConditionCheckerTest.MockCollision;
import oogasalad.engine.model.object.event.ConditionCheckerTest.mockInput;
import oogasalad.engine.model.object.mapObject;
import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.fileparser.records.FrameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test suite that checks result of executing outcomes onto game objects
 *
 * @author Gage Garcia
 */
public class OutcomesTest {
  private OutcomeExecutor executor;
  private Player player;

  public class MockCollision implements CollisionHandler {

    @Override
    public void updateCollisions() {

    }

    @Override
    public List<GameObject> getCollisions(GameObject gameObject) {
      return List.of();
    }
  }

  public class MockExecutor implements GameExecutor {

    @Override
    public void destroyGameObject(GameObject gameObject) {

    }

    @Override
    public mapObject getMapObject() {
      return null;
    }

    @Override
    public GameObject getGameObjectByUUID(String id) {
      return null;
    }

    @Override
    public void endGame() {

    }

    @Override
    public void restartLevel()
        throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {

    }

    @Override
    public void selectLevel(String filePath)
        throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {

    }
  }

  public class MockAnimation implements AnimationHandlerApi {

    @Override
    public FrameData getCurrentFrameInAnimation(GameObject gameObject) {
      return null;
    }

    @Override
    public void goToBaseImage(GameObject gameObject) {

    }

    @Override
    public void addToAnimations(GameObject gameObject, String AnimationName) {

    }

    @Override
    public void clearAndAddToAnimationList(GameObject gameObject, String AnimationName) {

    }

    @Override
    public void setBaseImage(GameObject gameObject, String newBaseImage) {

    }
  }
  @BeforeEach
  void setUp() throws Exception {
    executor = new OutcomeExecutor(new MockCollision(), new MockExecutor(), new MockAnimation());
    player = new Player(null, null, 0, 0, 0, null, null, null, null, null, null);


  }

  @Test
  void MoveRightDefaultOutcome()
      throws LayerParseException, EventParseException, BlueprintParseException, IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, DataFormatException, LevelDataParseException, PropertyParsingException, SpriteParseException, HitBoxParseException, GameObjectParseException, ClassNotFoundException, InstantiationException {

  }

}
