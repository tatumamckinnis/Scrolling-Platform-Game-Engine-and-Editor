package oogasalad.editor.view.tools;

import java.io.File;
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
import javafx.stage.Window;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tool for placing selected prefabs (Blueprints) onto the editor grid. Handles sprite resolution
 * before delegating placement to the controller.
 *
 * @author Tatum McKinnis
 */
public class PrefabPlacementTool implements ObjectInteractionTool {

  private static final Logger LOG = LogManager.getLogger(PrefabPlacementTool.class);
  private static final String UI_RESOURCES = "EditorUI";

  private final EditorGameView editorView;
  private final EditorController editorController;
  private final ResourceBundle uiResources;


  public PrefabPlacementTool(EditorGameView editorView, EditorController editorController) {
    this.editorView = Objects.requireNonNull(editorView, "EditorGameView cannot be null.");
    this.editorController = Objects.requireNonNull(editorController,
        "EditorController cannot be null.");
    this.uiResources = EditorResourceLoader.loadResourceBundle(UI_RESOURCES);
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

    Optional<BlueprintData> resolvedPrefabOpt = resolveSpriteForPlacement(selectedPrefab);

    resolvedPrefabOpt.ifPresentOrElse(
        resolvedPrefab -> {
          editorController.requestPrefabPlacement(resolvedPrefab, gridX, gridY);
          LOG.debug("Delegated prefab placement request for type '{}' at ({}, {})",
              resolvedPrefab.type(), gridX, gridY);
        },
        () -> LOG.info("Prefab placement cancelled or failed during sprite resolution.")
    );
  }

  /**
   * Checks if the prefab's sprite exists. If not, prompts the user to locate it or proceed
   * without. Returns an Optional containing the (potentially updated) BlueprintData or empty if
   * cancelled.
   */
  private Optional<BlueprintData> resolveSpriteForPlacement(BlueprintData originalPrefab) {
    SpriteData spriteData = originalPrefab.spriteData();

    if (spriteData == null || spriteData.spriteFile() == null || safeGetPath(
        spriteData.spriteFile()).isEmpty()) {
      LOG.trace("Prefab '{}' has no sprite data or path. Proceeding with original data.",
          originalPrefab.type());
      return Optional.of(originalPrefab);
    }

    String currentGameDirectory = getCurrentGameSpriteDirectory();
    if (currentGameDirectory == null) {
      return Optional.empty();
    }

    String relativePath = safeGetPath(spriteData.spriteFile());
    Path targetPath = Paths.get(currentGameDirectory, relativePath);

    if (Files.exists(targetPath) && Files.isRegularFile(targetPath)) {
      LOG.debug("Sprite '{}' for prefab '{}' found in current game assets.", relativePath,
          originalPrefab.type());
      return Optional.of(originalPrefab);
    }

    LOG.warn("Sprite '{}' for prefab '{}' not found at expected path: {}", relativePath,
        originalPrefab.type(), targetPath);
    return handleMissingSprite(originalPrefab, currentGameDirectory, relativePath);
  }

  /**
   * Gets the expected sprite directory path for the current game. Handles potential errors if the
   * game directory isn't set.
   */
  private String getCurrentGameSpriteDirectory() {
    String gameDir = editorController.getEditorDataAPI().getCurrentGameDirectoryPath();
    if (gameDir == null || gameDir.isEmpty()) {
      LOG.error(
          "Cannot resolve sprite path: Current game directory path is not set in EditorDataAPI.");
      editorController.notifyErrorOccurred(uiResources.getString("ErrorCannotResolveSprite"));
      return null;
    }
    return Paths.get(gameDir, "sprites").toString();
  }

  /**
   * Displays an alert to the user about the missing sprite and handles their choice (locate, place
   * without, cancel).
   */
  private Optional<BlueprintData> handleMissingSprite(BlueprintData originalPrefab,
      String currentGameSpriteDir, String relativePath) {
    Alert alert = createMissingSpriteAlert(relativePath, originalPrefab.type());

    Optional<ButtonType> result = alert.showAndWait();

    if (result.isPresent()) {
      ButtonType choice = result.get();
      if (choice.getText().equals(uiResources.getString("LocateSpriteButton"))) {
        return handleLocateSprite(originalPrefab, currentGameSpriteDir);
      } else if (choice.getText().equals(uiResources.getString("PlaceWithoutSpriteButton"))) {
        LOG.info("User chose to place prefab '{}' without resolving sprite '{}'.",
            originalPrefab.type(), relativePath);
        return Optional.of(createPrefabWithEmptySprite(originalPrefab));
      }
    }

    LOG.info("User cancelled prefab placement due to missing sprite.");
    return Optional.empty();
  }

  /**
   * Creates the Alert dialog for missing sprites.
   */
  private Alert createMissingSpriteAlert(String relativePath, String prefabType) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle(uiResources.getString("SpriteNotFoundTitle"));
    alert.setHeaderText(
        String.format(uiResources.getString("SpriteNotFoundHeader"), relativePath, prefabType));
    alert.setContentText(uiResources.getString("SpriteNotFoundContent"));

    ButtonType locateButton = new ButtonType(uiResources.getString("LocateSpriteButton"));
    ButtonType placeWithoutButton = new ButtonType(
        uiResources.getString("PlaceWithoutSpriteButton"));
    ButtonType cancelButton = new ButtonType(uiResources.getString("CancelButton"),
        ButtonType.CANCEL.getButtonData());

    alert.getButtonTypes().setAll(locateButton, placeWithoutButton, cancelButton);
    return alert;
  }

  /**
   * Creates a copy of the BlueprintData but with an empty/null SpriteData record.
   */
  private BlueprintData createPrefabWithEmptySprite(BlueprintData prefab) {
    SpriteData emptySpriteData = new SpriteData(
        prefab.spriteData() != null ? prefab.spriteData().name() : "Unknown",
        new File(""),
        null,
        new ArrayList<>(),
        new ArrayList<>()
    );

    return new BlueprintData(
        prefab.blueprintId(), prefab.velocityX(), prefab.velocityY(), prefab.rotation(),
        prefab.gameName(), prefab.group(), prefab.type(),
        emptySpriteData,
        prefab.hitBoxData(),
        safeGetList(prefab.eventDataList()),
        prefab.stringProperties(), prefab.doubleProperties(), prefab.displayedProperties()
    );
  }

  /**
   * Handles the process of letting the user locate the sprite file using a FileChooser and updates
   * the BlueprintData if successful.
   */
  private Optional<BlueprintData> handleLocateSprite(BlueprintData prefab,
      String currentGameSpriteDir) {
    Optional<File> selectedFileOpt = showLocateSpriteFileChooser();

    if (selectedFileOpt.isPresent()) {
      File selectedFile = selectedFileOpt.get();
      return copySpriteAndUpdatePrefab(prefab, selectedFile, currentGameSpriteDir);
    } else {
      LOG.info("User cancelled sprite file selection.");
      return Optional.empty();
    }
  }

  /**
   * Shows the FileChooser dialog for the user to select the sprite file.
   */
  private Optional<File> showLocateSpriteFileChooser() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(uiResources.getString("LocateSpriteFileChooserTitle"));
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"),
        new FileChooser.ExtensionFilter("All Files", "*.*")
    );

    Window ownerWindow = (editorView != null && editorView.getScene() != null) ? editorView
        .getScene().getWindow() : null;
    return Optional.ofNullable(fileChooser.showOpenDialog(ownerWindow));
  }

  /**
   * Copies the selected sprite file to the game's sprite directory and creates updated
   * BlueprintData.
   */
  private Optional<BlueprintData> copySpriteAndUpdatePrefab(BlueprintData prefab,
      File selectedFile, String currentGameSpriteDir) {
    try {
      Path sourcePath = selectedFile.toPath();
      String targetFileName = determineTargetFileName(prefab.spriteData(), selectedFile);

      Path targetDirectory = Paths.get(currentGameSpriteDir);
      Path targetPath = targetDirectory.resolve(targetFileName);

      Files.createDirectories(targetDirectory);
      Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
      LOG.info("Copied selected sprite '{}' to '{}'", sourcePath, targetPath);

      String newRelativePath = targetFileName;
      return Optional.of(createUpdatedBlueprintData(prefab, newRelativePath));

    } catch (IOException e) {
      LOG.error("Failed to copy selected sprite file: {}", e.getMessage(), e);
      editorController.notifyErrorOccurred(
          uiResources.getString("SpriteCopyError") + ": " + e.getMessage());
      return Optional.empty();
    } catch (Exception e) {
      LOG.error("Error accessing sprite data components while copying sprite: {}", e.getMessage(),
          e);
      editorController.notifyErrorOccurred("Error processing sprite data.");
      return Optional.empty();
    }
  }

  /**
   * Determines the target filename for the copied sprite. Uses the original filename if possible,
   * otherwise falls back to the selected file's name.
   */
  private String determineTargetFileName(SpriteData oldSpriteData, File selectedFile) {
    if (oldSpriteData != null && oldSpriteData.spriteFile() != null) {
      String originalName = oldSpriteData.spriteFile().getName();
      if (originalName != null && !originalName.isEmpty()) {
        return originalName;
      }
    }
    return selectedFile.getName();
  }

  /**
   * Creates a new BlueprintData instance with the updated sprite file path.
   */
  private BlueprintData createUpdatedBlueprintData(BlueprintData prefab, String newRelativePath) {
    SpriteData oldSpriteData = prefab.spriteData();

    String name = (oldSpriteData != null) ? oldSpriteData.name() : "Unknown";
    FrameData baseFrame = (oldSpriteData != null) ? oldSpriteData.baseFrame() : null;
    List<FrameData> frames = safeGetList(oldSpriteData != null ? oldSpriteData.frames() : null);
    List<oogasalad.fileparser.records.AnimationData> animations = safeGetList(
        oldSpriteData != null ? oldSpriteData.animations() : null);

    SpriteData newSpriteData = new SpriteData(
        name,
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
        safeGetList(prefab.eventDataList()),
        prefab.stringProperties(), prefab.doubleProperties(), prefab.displayedProperties()
    );
  }

  private String safeGetPath(File file) {
    return (file != null) ? file.getPath() : "";
  }

  private <T> List<T> safeGetList(List<T> list) {
    return (list != null) ? list : new ArrayList<>();
  }
}