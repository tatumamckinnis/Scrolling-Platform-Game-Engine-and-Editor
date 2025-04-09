package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;

public record BlueprintData(
    int blueprintId,
    double velocityX,
    double velocityY,
    String gameName,
    String group, //entities, blocks, backgrounds
    String type,
    SpriteData spriteData,
    HitBoxData hitBoxData,
    List<EventData> eventDataList,
    Map<String,String> stringProperties,
    Map<String,Double> doubleProperties,
    List<String> displayedProperties
) {}
