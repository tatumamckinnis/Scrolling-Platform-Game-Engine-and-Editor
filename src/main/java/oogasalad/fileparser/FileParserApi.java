package oogasalad.fileparser;

import oogasalad.exceptions.BlueprintParseException;
import oogasalad.exceptions.EventParseException;
import oogasalad.exceptions.GameObjectParseException;
import oogasalad.exceptions.HitBoxParseException;
import oogasalad.exceptions.LayerParseException;
import oogasalad.exceptions.LevelDataParseException;
import oogasalad.exceptions.PropertyParsingException;
import oogasalad.exceptions.SpriteParseException;
import oogasalad.exceptions.SpriteSheetLoadException;
import oogasalad.fileparser.records.LevelData;
import oogasalad.fileparser.records.SpriteSheetData;

/**
 * API interface for parsing game-related files.
 * <p>
 * This interface defines the contract for parsing a level file into a LevelData record. It supports
 * the parsing of various aspects of the level, including blueprints, sprites, hitboxes, game
 * objects, level data, properties, events, and layers.
 * </p>
 *
 * @see BlueprintParseException
 * @see SpriteParseException
 * @see HitBoxParseException
 * @see GameObjectParseException
 * @see LevelDataParseException
 * @see PropertyParsingException
 * @see EventParseException
 * @see LayerParseException
 *
 * @author Billy McCune, Jacob You
 */
public interface FileParserApi {

  /**
   * Parses the specified level file and returns its level data.
   *
   * @param filePath the path to the level file to be parsed.
   * @return a {@link LevelData} record representing the parsed level data.
   * @throws BlueprintParseException  if an error occurs while parsing the blueprint data.
   * @throws SpriteParseException     if an error occurs while parsing sprite data.
   * @throws HitBoxParseException     if an error occurs while parsing hitbox data.
   * @throws GameObjectParseException if an error occurs while parsing game object data.
   * @throws LevelDataParseException  if an error occurs while parsing level data.
   * @throws PropertyParsingException if an error occurs while parsing properties.
   * @throws EventParseException      if an error occurs while parsing event data.
   * @throws LayerParseException      if an error occurs while parsing layer data.
   */
  public LevelData parseLevelFile(String filePath)
      throws BlueprintParseException, SpriteParseException,
      HitBoxParseException, GameObjectParseException, LevelDataParseException,
      PropertyParsingException, EventParseException, LayerParseException;

  /**
   * Parse the specified sprite sheet file and return the sprite data.
   *
   * @param filePath the path to the sprite sheet to be parsed
   * @return the {@link SpriteSheetData} record representing the parsed sprite sheet data
   * @throws SpriteSheetLoadException if an error occurs while parsing the sprite sheet
   */
  public SpriteSheetData parseSpriteSheet(String filePath) throws SpriteSheetLoadException;

}
