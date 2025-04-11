package oogasalad.engine.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import oogasalad.engine.event.condition.Condition;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.EventData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.GameObjectData;
import oogasalad.fileparser.records.HitBoxData;
import oogasalad.fileparser.records.SpriteData;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventConverterTest {

  EventConverter eventConverter = new EventConverter();

  @BeforeEach
  void setUp() {
    SpriteData spriteData1 = new SpriteData("player.png", new File("src/test/resources/sprites/sprite1.png"),
        new FrameData("moving", 1, 1, 10, 20), new ArrayList<>(), new ArrayList<>());
    List<EventData> eventDataList1 = new ArrayList<>();
    //EventData eventData1 = new EventData("input", 20, );
    //EventData eventData2 = new EventData();
    HitBoxData hitBoxData1 = new HitBoxData("Mario", 1, 1, 2, 4);
    BlueprintData blueprintData1 = new BlueprintData(1, 0,0, 90,"Mario", "Player", "Player", spriteData1, hitBoxData1, eventDataList1, new HashMap<>(), new HashMap<>(), new ArrayList<>());
    GameObjectData gameObject1 = new GameObjectData(
        1,
        new UUID(4, 1),
        100,
        200,
        1
    );
  }

  @Test
  void convertEventData() {

  }
}