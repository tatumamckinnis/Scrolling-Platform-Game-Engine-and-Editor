package oogasalad.fileparser.records;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author Billy McCune
 */
public record GameObjectData(
    int blueprintId,
    UUID uniqueId,
    int x,
    int y,
    int layer //z-layer or draw layer for background/foreground ordering
) {}