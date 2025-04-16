package oogasalad.editor.view.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.view.EditorGameView;
import oogasalad.editor.view.resources.EditorResourceLoader;
import oogasalad.fileparser.records.BlueprintData;
import oogasalad.fileparser.records.FrameData;
import oogasalad.fileparser.records.SpriteData;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tool for placing selected prefabs (Blueprints) onto the editor grid.
 * Handles sprite resolution before delegating placement to the controller.
 *
 * @author Tatum McKinnis
 */
public class PrefabPlacementTool implements ObjectInteractionTool {

  private static final Logger LOG = LogManager.getLogger(PrefabPlacementTool.class);
  private static final String DEFAULT_RESOURCE_PACKAGE = "oogasalad.editor.view.resources.";
  private static final String UI_RESOURCES = "EditorUI";

  private final EditorGameView editorView;
  private final EditorController editorController;
  private final ResourceBundle uiResources;


  public PrefabPlacementTool(EditorGameView editorView, EditorController editorController) {
    this.editorView = Objects.requireNonNull(editorView, "EditorGameView cannot be null.");
    this.editorController = Objects.requireNonNull(editorController, "EditorController cannot be null.");
    this.uiResources = EditorResourceLoader.loadResourceBundle(DEFAULT_RESOURCE_PACKAGE + UI_RESOURCES);
    LOG.info("Created PrefabPlacementTool");
  }

  /**
   * Handles interaction (click) on the grid to place the currently selected prefab.
   */
  @Override
  public void interactObjectAt(double gridX, double gridY) {
    BlueprintData selectedPrefab = editorView.getSelectedPrefab();

    if (selectedPrefab == null) {
      LOG.warn("Prefab placement attempted but no prefab is selected.");
      editorController.notifyErrorOccurred(uiResources.getString("ErrorNoPrefabSelected"));
      return;
    }

    LOG.debug("Attempting placement of prefab: {}", selectedPrefab.type());

    BlueprintData resolvedPrefabData = resolveSpriteForPlacement(selectedPrefab);

    if (resolvedPrefabData == null) {
      LOG.info("Prefab placement cancelled during sprite resolution.");
      return;
    }

    editorController.requestPrefabPlacement(resolvedPrefabData, gridX, gridY);
    LOG.debug("Delegated prefab placement request to controller for type '{}' at ({}, {})",
        resolvedPrefabData.type(), gridX, gridY);
  }

  /**
   * Checks if the prefab's sprite exists in the current game context. Prompts user if not found.
   */
  private BlueprintData resolveSpriteForPlacement(BlueprintData originalPrefab) {
    SpriteData spriteData = originalPrefab.spriteData();

    if (spriteData == null || spriteData.spriteFile() == null || spriteData.spriteFile().getPath().isEmpty()) {
      return originalPrefab;
    }

    String currentGameDirectory = editorController.getEditorDataAPI().getCurrentGameDirectoryPath();
    if (currentGameDirectory == null || currentGameDirectory.isEmpty()) {
      currentGameDirectory = "data/gameData/unknown_game";
      LOG.error("CRITICAL: Method 'getCurrentGameDirectoryPath()' MUST be implemented in EditorDataAPI. Using placeholder: {}", currentGameDirectory);
      editorController.notifyErrorOccurred(uiResources.getString("ErrorCannotResolveSprite"));
      return null;
    }
    String currentGameSpritePath = Paths.get(currentGameDirectory, "sprites").toString();

    String relativePath = spriteData.spriteFile().getPath();
    Path targetPath = Paths.get(currentGameSpritePath, relativePath);

    if (Files.exists(targetPath) && Files.isRegularFile(targetPath)) {
      LOG.debug("Sprite '{}' for prefab '{}' found in current game assets.", relativePath, originalPrefab.type());
      return originalPrefab;
    }

    LOG.warn("Sprite '{}' for prefab '{}' not found at expected path: {}", relativePath, originalPrefab.type(), targetPath);

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(uiResources.getString("SpriteNotFoundTitle"));
    alert.setHeaderText(String.format(uiResources.getString("SpriteNotFoundHeader"), relativePath, originalPrefab.type()));
    alert.setContentText(uiResources.getString("SpriteNotFoundContent"));

    ButtonType locateButton = new ButtonType(uiResources.getString("LocateSpriteButton"));
    ButtonType placeWithoutButton = new ButtonType(uiResources.getString("PlaceWithoutSpriteButton"));
    ButtonType cancelButton = new ButtonType(uiResources.getString("CancelButton"), ButtonType.CANCEL.getButtonData());

    alert.getButtonTypes().setAll(locateButton, placeWithoutButton, cancelButton);

    Optional<ButtonType> result = alert.showAndWait();

    if (result.isPresent() && result.get() == locateButton) {
      return handleLocateSprite(originalPrefab, currentGameSpritePath);
    } else if (result.isPresent() && result.get() == placeWithoutButton) {
      LOG.info("User chose to place prefab '{}' without resolving sprite '{}'.", originalPrefab.type(), relativePath);
      SpriteData emptySpriteData = new SpriteData(spriteData.name(), new File(""), null, new ArrayList<>(), new ArrayList<>());
      return new BlueprintData(
          originalPrefab.blueprintId(), originalPrefab.velocityX(), originalPrefab.velocityY(), originalPrefab.rotation(),
          originalPrefab.gameName(), originalPrefab.group(), originalPrefab.type(),
          emptySpriteData,
          originalPrefab.hitBoxData(), originalPrefab.eventDataList(),
          originalPrefab.stringProperties(), originalPrefab.doubleProperties(), originalPrefab.displayedProperties()
      );
    } else {
      LOG.info("User cancelled prefab placement due to missing sprite.");
      return null;
    }
  }

  /**
   * Handles locating and copying the sprite file.
   */
  private BlueprintData handleLocateSprite(BlueprintData prefab, String currentGameSpritePath) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(uiResources.getString("LocateSpriteFileChooserTitle"));
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );

    File selectedFile = fileChooser.showOpenDialog(editorView.getScene().getWindow());

    if (selectedFile != null) {
      try {
        Path sourcePath = selectedFile.toPath();
        SpriteData oldSpriteData = prefab.spriteData();
        if (oldSpriteData == null || oldSpriteData.spriteFile() == null) {
          LOG.error("Cannot determine target filename: Original sprite data or file is null.");
          editorController.notifyErrorOccurred("Internal error processing sprite data.");
          return null;
        }

        String targetFileName = oldSpriteData.spriteFile().getName();
        if (targetFileName == null || targetFileName.isEmpty()) {
          targetFileName = selectedFile.getName();
        }

        Path targetDirectory = Paths.get(currentGameSpritePath);
        Path targetPath = targetDirectory.resolve(targetFileName);

        Files.createDirectories(targetDirectory);
        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        LOG.info("Copied selected sprite '{}' to '{}'", sourcePath, targetPath);

        String newRelativePath = targetFileName;

        FrameData baseFrame = oldSpriteData.baseFrame();

        List<FrameData> frames = (oldSpriteData.frames() != null) ? oldSpriteData.frames() : new ArrayList<>();
        List<oogasalad.fileparser.records.AnimationData> animations = (oldSpriteData.animations() != null) ? oldSpriteData.animations() : new ArrayList<>();


        SpriteData newSpriteData = new SpriteData(
            oldSpriteData.name(),
            new File(newRelativePath),
            baseFrame,
            frames,
            animations
        );

        return new BlueprintData(
            prefab.blueprintId(), prefab.velocityX(), prefab.velocityY(), prefab.rotation(),
            prefab.gameName(), prefab.group(), prefab.type(),
            newSpriteData,
            prefab.hitBoxData(),
            (prefab.eventDataList() != null) ? prefab.eventDataList() : new ArrayList<>(),
            prefab.stringProperties(), prefab.doubleProperties(), prefab.displayedProperties()
        );

      } catch (IOException e) {
        LOG.error("Failed to copy selected sprite file: {}", e.getMessage(), e);
        editorController.notifyErrorOccurred(uiResources.getString("SpriteCopyError") + ": " + e.getMessage());
        return null;
      } catch (Exception e) {
        LOG.error("Error accessing sprite data components while handling located sprite: {}", e.getMessage(), e);
        editorController.notifyErrorOccurred("Error processing sprite data.");
        return null;
      }
    } else {
      LOG.info("User cancelled sprite file selection.");
      return null;
    }
  }
}