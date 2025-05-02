package oogasalad.fileparser;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // Keep UUID import
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.fileparser.records.GameObjectData;
import org.apache.logging.log4j.LogManager; // Import LogManager
import org.apache.logging.log4j.Logger;     // Import Logger
import org.w3c.dom.Element;


/**
 * Responsible for parsing an XML element representing game objects into a list of
 * {@link GameObjectData} records.
 * <p>
 * This parser extracts blueprint IDs, coordinates, and UUIDs for each game object instance
 * described in the XML element. It attempts to parse standard UUIDs and uses a placeholder
 * for non-standard IDs.
 * </p>
 *
 * @author Billy McCune
 */
public class GameObjectDataParser {

  // Logger for warnings and debugging
  private static final Logger LOG = LogManager.getLogger(GameObjectDataParser.class);
  // Placeholder UUID for non-standard IDs (all zeros)
  private static final UUID PLACEHOLDER_UUID = new UUID(0L, 0L);
  // *** MODIFIED REGEX to allow whitespace ***
  private static final Pattern COORDINATE_PATTERN = Pattern.compile("\\(\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\)");


  /**
   * Parses a game object XML element and creates a list of {@link GameObjectData} records.
   * <p>
   * The method reads the blueprint ID, a list of UUIDs, and a coordinate string of the form
   * {@code "(x1,y1),(x2,y2),..."}. Each UUID is paired with one coordinate set. It attempts to
   * parse standard UUIDs, substituting a placeholder and logging a warning for invalid formats.
   * </p>
   *
   * @param gameObjectElement the XML element representing the game object
   * @param z                 the z-index layer of the game object
   * @return a list of {@link GameObjectData} objects created from the element
   * @throws GameObjectParseException if the input data is malformed or parsing fails critically
   */
  public List<GameObjectData> getGameObjectData(Element gameObjectElement, int z)
      throws GameObjectParseException {
    String blueprintIdStr = gameObjectElement.getAttribute("id"); // Get ID as string first for logging
    String coordinates = ""; // Initialize coordinates
    try {
      int blueprintId = Integer.parseInt(blueprintIdStr);

      String nameAttr = gameObjectElement.getAttribute("name"); // May be empty, handled as ""
      String uidAttr = gameObjectElement.getAttribute("uid");
      String[] uidArray = uidAttr.split(",");
      coordinates = gameObjectElement.getAttribute("coordinates"); // Assign here
      List<GameObjectData> gameObjectDataList = new ArrayList<>();

      LOG.debug("Processing object id='{}', uid='{}', coordinates='{}'", blueprintId, uidAttr, coordinates);

      // Use the pre-compiled pattern allowing whitespace
      Matcher matcher = COORDINATE_PATTERN.matcher(coordinates);

      int index = 0;
      while (matcher.find()) {
        LOG.debug("Found coordinate match group 1: '{}', group 2: '{}' at index {}", matcher.group(1), matcher.group(2), index);

        // Trim the matched groups in case the regex captures surrounding whitespace allowed by \s*
        int x = Integer.parseInt(matcher.group(1).trim());
        int y = Integer.parseInt(matcher.group(2).trim());

        if (index < uidArray.length) {
          String uidString = uidArray[index].trim();
          UUID uniqueIdToUse; // UUID to be stored

          if (uidString.isEmpty()) {
            throw new GameObjectParseException("Empty UID found at index " + index + " for object id " + blueprintId);
          }

          try {
            uniqueIdToUse = UUID.fromString(uidString);
            LOG.debug("Parsed UID '{}' successfully.", uidString); // Log success
          } catch (IllegalArgumentException e) {
            LOG.warn("Invalid UUID format '{}' for object id {}. Using placeholder UUID '{}'.",
                uidString, blueprintId, PLACEHOLDER_UUID);
            uniqueIdToUse = PLACEHOLDER_UUID; // Use placeholder
          }

          gameObjectDataList.add(new GameObjectData(nameAttr, blueprintId, uniqueIdToUse, x, y, z, ""));

        } else {
          throw new GameObjectParseException("Mismatch between number of coordinates and UIDs for object id " + blueprintId + ". Found coord #" + (index+1) + " but only " + uidArray.length + " UIDs.");
        }
        index++;
      }

      LOG.debug("Finished matching coordinates for object id '{}'. Total coordinates found: {}. Total UIDs: {}", blueprintId, index, uidArray.length);


      boolean uidIsEmptyPlaceholder = (uidArray.length == 1 && uidArray[0].trim().isEmpty());
      boolean coordinatesEmpty = coordinates.trim().isEmpty();

      // Allow completely empty object definitions (no coords, no uid)
      if (coordinatesEmpty && uidIsEmptyPlaceholder) {
        LOG.debug("Object id '{}' has no coordinates or UIDs, skipping count check.", blueprintId);
      }
      // Check if non-empty UID is present but coordinates are missing/empty
      else if (!uidIsEmptyPlaceholder && coordinatesEmpty) {
        throw new GameObjectParseException("Non-empty UID(s) found but coordinates attribute is empty for object id " + blueprintId);
      }
      // Check for mismatch if coordinates were expected
      else if (index != uidArray.length && !uidIsEmptyPlaceholder) {
        throw new GameObjectParseException("Number of parsed coordinates (" + index + ") does not match number of UIDs ("+ uidArray.length + ") for object id " + blueprintId);
      }

      // This check should now only trigger if coordinates had content but *none* matched the (more flexible) pattern
      if (gameObjectDataList.isEmpty() && !coordinatesEmpty) {
        throw new GameObjectParseException("No valid coordinate pairs found in non-empty coordinate string: '" + coordinates + "' for object id " + blueprintId);
      }

      return gameObjectDataList;

    } catch (NumberFormatException e) {
      throw new GameObjectParseException("Error parsing numeric value for object id '" + blueprintIdStr + "': " + e.getMessage(), e);
    } catch (Exception e) {
      // Added coordinates to the generic error message for more context
      throw new GameObjectParseException("Error parsing game object element for id '" + blueprintIdStr + "' with coordinates '" + coordinates + "': " + e.getMessage(), e);
    }
  }
}