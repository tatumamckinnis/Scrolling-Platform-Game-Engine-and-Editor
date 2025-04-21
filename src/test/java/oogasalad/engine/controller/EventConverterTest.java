package oogasalad.engine.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import oogasalad.engine.model.event.Event;
import oogasalad.engine.model.event.Event.EventType;
import oogasalad.engine.model.event.condition.EventCondition;
import oogasalad.engine.model.event.condition.EventCondition.ConditionType;
import oogasalad.engine.model.event.outcome.EventOutcome;
import oogasalad.engine.model.event.outcome.EventOutcome.OutcomeType;
import oogasalad.engine.model.object.GameObject;
import oogasalad.engine.model.object.HitBox;
import oogasalad.engine.model.object.Player;
import oogasalad.engine.model.object.Sprite;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.ConditionData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.OutcomeData;
import oogasalad.fileparser.records.SpriteData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class EventConverterTest {

  GameObjectData gameObjectData;
  GameObject expectedGameObject;
  Map<Integer, BlueprintData> blueprintDataMap;

  @BeforeEach
  void setUp() {

    List<EventData> eventDataList = new ArrayList<>();
    List<List<ConditionData>> conditionDataList = new ArrayList<>();
    List<ConditionData> conditionData1 = new ArrayList<>();
    conditionDataList.add(conditionData1);

    Map<String, String> stringProperties = new HashMap<>();
    Map<String, Double> doubleProperties = new HashMap<>();
    doubleProperties.put("Sensitivity", 0.5);
    conditionData1.add(new ConditionData("W_KEY_PRESSED", stringProperties, doubleProperties));

    List<OutcomeData> outcomeDataList = new ArrayList<>();
    Map<String, String> outcomeStringProperties2 = new HashMap<>();
    outcomeStringProperties2.put("jumpType", "Floaty");
    Map<String, Double> outcomeDoubleProperties2 = new HashMap<>();
    outcomeDoubleProperties2.put("JumpAmount", 20.0);
    outcomeDataList.add(new OutcomeData("JUMP", outcomeStringProperties2, outcomeDoubleProperties2));

    EventData eventData1 = new EventData("input", "1", conditionDataList, outcomeDataList);
    eventDataList.add(eventData1);

    SpriteData spriteData1 = new SpriteData("player.png", new File("src/test/resources/sprites/sprite1.png"),
        new FrameData("moving", 1, 1, 10, 20), new ArrayList<>(), new ArrayList<>());
    Sprite sprite = new Sprite(new HashMap<>(), new FrameData("moving", 1, 1, 10, 20), new HashMap<>(), 2, 4, new File("src/test/resources/sprites/sprite.png"), 90.0, true);
    HitBoxData hitBoxData1 = new HitBoxData("Mario", 1, 1, 2, 4);
    HitBox hitbox = new HitBox(1, 1, 1, 1);
    BlueprintData blueprintData1 = new BlueprintData(1, 0,0, 90, false,"Mario", "Player", "Player", spriteData1, hitBoxData1,
        eventDataList, new HashMap<>(), new HashMap<>(), new ArrayList<>());
    UUID uuid = UUID.fromString("e816f04c-3047-4e30-9e20-2e601a99dde8");
    gameObjectData = new GameObjectData(
        1,
        uuid,
        100,
        200,
        1,
        ""
    );

    expectedGameObject = new Player(uuid, "Player", 1, 0, 0, hitbox, sprite, new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
    List<Event> expectedEvents = new ArrayList<>();
    List<List<EventCondition>> expectedConditions = new ArrayList<>();
    List<EventCondition> expectedConditionsList = new ArrayList<>();
    expectedConditionsList.add(new EventCondition(ConditionType.W_KEY_PRESSED, stringProperties, doubleProperties)) ;
    expectedConditions.add(expectedConditionsList);

    List<EventOutcome> expectedOutcomes = new ArrayList<>();
    expectedOutcomes.add(new EventOutcome(OutcomeType.JUMP, outcomeStringProperties2,
        outcomeDoubleProperties2));
    expectedEvents.add(new Event(expectedGameObject, expectedConditions, expectedOutcomes,
        EventType.PHYSICS));
    expectedGameObject.setEvents(expectedEvents);

    blueprintDataMap = new HashMap<>();
    blueprintDataMap.put(1, blueprintData1);
  }

  @Test
  void convertEventData() {
    List<Event> actualEvents = EventConverter.convertEventData(gameObjectData, expectedGameObject, blueprintDataMap);
    assert(actualEvents.size() == expectedGameObject.getEvents().size());
    assertEquals(actualEvents.getFirst().getConditions(), expectedGameObject.getEvents().getFirst().getConditions());
    assertEquals(actualEvents.getFirst().getOutcomes(), expectedGameObject.getEvents().getFirst().getOutcomes());
  }
}